package balancer

import balancer.objects.{Bot, Human, Scale, Stack}

import java.io.{BufferedReader, BufferedWriter, FileNotFoundException, FileReader, FileWriter}
import scala.collection.mutable.{Buffer, Map}

final case class ParseError(private val message: String = "",
                            private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

class FileManager(private val game: Game) {
  private def state = game.state

  def saveGame(filePath: String): Unit = {

    val lw = new BufferedWriter(new FileWriter(filePath))

    lw.write("BALANCER 1.3 SAVE FILE\n")

    lw.write("# Setting\n")
    lw.write("WeightPerRound: " + game.weightsPerRound + "\n")
    lw.write("NumberOfRound: " + game.numRounds + "\n")

    val humans = state.players.flatMap { case p: Human => Some(p) case _ => None }
    val bots = state.players.flatMap { case p: Bot => Some(p) case _ => None }


    lw.write("# Meta\n")
    if(humans.nonEmpty) lw.write("Human: " + humans.map(_.name).mkString(",") + "\n")
    if(bots.nonEmpty) lw.write("Bot: " + bots.map(_.name).mkString(",") + "\n")
    lw.write("Round: " + state.currentRound + "\n")
    lw.write("Turn: " + state.players(state.currentIdx).name + "\n")

    lw.write("# Scale\n")

    for (scale <- state.scales.sortBy(_.code)) {
      if (scale == state.baseScale)
        lw.write("_,0")
      else
        lw.write(s"${scale.parentScale.code},${scale.pos}")
      lw.write(s",${scale.radius},${scale.code} : ")
      val buf = Buffer[String]()
      scale.boardVector.flatten.foreach {
        case stack: Stack =>
          buf.append(stack.weightsVector.flatMap(_.owner).map(_.playerCode).prepended(stack.pos.toString).mkString(","))
        case scale: Scale =>
      }
      lw.write(buf.mkString(" | "))
      lw.write("\n")
    }

    lw.write("END\n")
    lw.close()
  }

  def loadGame(filePath: String): Unit = {

    val fileReader = try {
      new FileReader(filePath)
    } catch {
      case e: FileNotFoundException =>
        println(s"'$filePath': File not found"); return
    }

    val lineReader = new BufferedReader(fileReader)

    // BEGIN PARSING

    // KEEP A BACKUP OF GAME SETTING
    var numRoundsBak: Int = game.numRounds
    var weightPerRoundBak: Int = game.weightsPerRound
    var baseScaleRadiusBak: Int = game.baseScaleRadius

    try {
      var line = lineReader.readLine().trim.toLowerCase

      if (!((line.startsWith("balancer")) && (line.endsWith("save file")))) {
        throw new ParseError("Unknown file type / identifiers ")
      }

      // SETTING DATA
      var weightPerRound: Int = 0
      var numRounds: Int = 0

      // META DATA
      var round: Int = 0
      var turn: String = ""
      var humanNames = Array[String]()
      var botNames = Array[String]()

      var newState: State = null

      // BLOCKS TO PARSE
      val blocksToProcess = Map[String, Boolean]("meta" -> false, "scale" -> false, "setting" -> false)
      var block = ""

      do {
        line = lineReader.readLine().trim
        if (line.nonEmpty) {
          if (line == "END")
            line = null
          else if (line(0) == '#') {
            block = line.substring(1).trim.toLowerCase
            if (blocksToProcess.contains(block) && !blocksToProcess(block))
              blocksToProcess(block) = true
          } else if (blocksToProcess.contains(block)) {
            val trimmedLine = line.split(':').map(_.trim)
            if (trimmedLine.length > 2 || trimmedLine.length == 0) {
              throw new ParseError(line + "\n=> Must be 'key:value' pair")
            }
            var key: String = trimmedLine(0).toLowerCase
            var value: String = if (trimmedLine.length == 1) "" else trimmedLine(1)
            block match {
              case "setting" =>
                key match {
                  case "weightperround" =>
                    weightPerRound = value.toIntOption.getOrElse(
                      throw new ParseError(line + "\n=> Number of weights must be an integer"))
                  case "numberofround" =>
                    numRounds = value.toIntOption.getOrElse(
                      throw new ParseError(line + "\n=> Number of Round must be an integer"))
                  case _ =>
                }
              case "meta" =>
                key match {
                  case "human" =>
                    humanNames = value.split(",").map(_.trim)
                  case "bot" =>
                    botNames = value.split(",").map(_.trim)
                  case "round" =>
                    round = value.toIntOption.getOrElse(
                      throw new ParseError(line + "\n=> Round number must be an integer"))
                  case "turn" =>
                    turn = value
                  case _ =>
                }
              case "scale" =>
                val splittedKey = key.split(',').map(_.trim)
                if (splittedKey.length != 4) {
                  throw new ParseError(line + "\n=> Must be 4 characters: 'parentScaleCode, positionOnParentScale, radius, code'")
                }
                var parentScaleCode = splittedKey(0)(0)
                val posOnParentScale = splittedKey(1).toIntOption.getOrElse(
                  throw new ParseError(line + "\n=> Position must be an interger")
                )
                val scaleRadius = splittedKey(2).toIntOption.getOrElse(
                  throw new ParseError(line + "\n=> Scale radius must be an interger")
                )
                val scaleCode = splittedKey(3)(0)
                val splittedValue = if (value != "") value.split('|').map(_.trim) else Array.empty[String]

                //// HELPER FUNCTION
                def parseStacks(parentScale: Scale) = {
                  for (stackString <- splittedValue) {
                    val stackStringSplitted = stackString.split(',').map(_.trim)
                    val pos = stackStringSplitted(0).toIntOption.getOrElse(
                      throw new ParseError(line + "\n=> Stack position must be an integer")
                    )

                    stackStringSplitted.drop(1).map(_ (0)).foreach(w_code => {
                      val refs = newState.buildWeight(pos, parentScale, newState.players.find(_.playerCode == w_code))
                      refs._2.updateOwner()
                    })
                  }
                }

                // BASE SCALE (PARENT SCALE IS JUST "_")
                if (parentScaleCode == '_') {
                  // APPLY SETTING
                  game.numRounds = numRounds
                  game.weightsPerRound = weightPerRound
                  game.baseScaleRadius = scaleRadius

                  newState = new State(game)

                  // ADDING PLAYERS
                  humanNames.foreach(newState.buildHuman)
                  botNames.foreach(newState.buildBot)

                  parseStacks(state.baseScale)
                } else {
                  newState.scaleWithCode(parentScaleCode) match {
                    case Some(parentScale: Scale) =>
                      val newScale = newState.buildScale(posOnParentScale, scaleRadius, parentScale, Some(scaleCode))
                      parseStacks(newScale)
                    case None => throw new ParseError(line + "\n=> Invalid parent scale code")
                  }
                }
              case _ =>
            }
          }
        }
      } while (line != null)

      lineReader.close()

      if (blocksToProcess.valuesIterator.contains(false)) {
        throw new ParseError("Missing blocks entry in file: " + blocksToProcess.filter(!_._2).keys.mkString(","))
      }

      // END PARSING
      println(s"Successfully load '$filePath'")

      // APPLY CURRENT ROUND AND TURN
      newState.currentRound = round
      newState.currentIdx = newState.players.indexWhere(_.name == turn)

      // FINALLY UPDATE STATE
      game.state = newState

    } catch {
      case e: ParseError =>
        println(e.getMessage)
        // RESTORE WHEN FAILED
        game.numRounds = numRoundsBak
        game.weightsPerRound = weightPerRoundBak
        game.baseScaleRadius = baseScaleRadiusBak
    }

  }
}
