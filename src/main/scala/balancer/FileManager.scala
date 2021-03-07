package balancer

import balancer.objects.{Scale, Stack}
import balancer.utils.Constants.{DefaultFile, Version}
import balancer.utils.ParseError

import java.io.{BufferedReader, BufferedWriter, FileNotFoundException, FileReader, FileWriter}
import scala.collection.mutable.{Buffer, Map}


class FileManager(private val game: Game) {
  private val blocks = Array[String]("meta", "scale", "setting")
  private def state = game.state

  /*
    Load default map and settings
   */
  def loadDefault() = loadGame(DefaultFile)

  /*
    Save the game state into file
   */
  def saveGame(filePath: String): Unit = {
    val lw = try {
      new BufferedWriter(new FileWriter(filePath))
    } catch {
      case e: FileNotFoundException =>
        println(s"'$filePath': File not found"); return
    }

    lw.write(s"BALANCER $Version SAVE FILE\n")

    lw.write("# Setting\n")
    lw.write("WeightPerRound: " + game.weightsPerRound + "\n")
    lw.write("NumberOfRound: " + game.numRounds + "\n")
    lw.write("BotDifficulty: " + game.botDifficulty + "\n")

    lw.write("# Meta\n")

    lw.write("Human: ")
    if (state.humans.nonEmpty) {
      lw.write(state.humans.map(_.name).mkString(","))
    }
    lw.write("\n")

    lw.write("Bot: ")
    if (state.bots.nonEmpty) {
      lw.write(state.bots.map(_.name).mkString(",") + "\n")
    }
    lw.write("Round: " + state.currentRound + "\n")
    lw.write("Turn: " + state.players(state.currentTurnIdx).name + "\n")

    lw.write("# Scale\n")
    for (scale <- state.scales.sortBy(_.code)) {
      if (scale == state.baseScale) {
        lw.write("_,0")
      } else {
        lw.write(s"${scale.parentScale.code},${scale.pos}")
      }

      lw.write(s",${scale.radius},${scale.code} : ")

      val buf = Buffer[String]()
      scale.boardVector.flatten.foreach {
        case stack: Stack =>
          buf.append(stack.weightsVector.flatMap(_.owner).map(_.playerCode)
             .prepended(stack.pos.toString)
             .mkString(","))
        case scale: Scale =>
      }
      lw.write(buf.mkString(" | "))
      lw.write("\n")
    }

    lw.write("END\n")
    lw.close()
  }

  def loadGame(filePath: String): Unit = {
    val lr = try {
      new BufferedReader(new FileReader(filePath))
    } catch {
      case e: FileNotFoundException =>
        println(s"'$filePath': File not found"); return
    }
    // Begin parsing

    // Keep a backup of current state (in case parsing gone wrong)
    var baseScaleRadiusBak = game.baseScaleRadius

    try {
      var line = lr.readLine().trim.toLowerCase

      if (!((line.startsWith("balancer")) && (line.endsWith("save file")))) {
        throw new ParseError("Unknown file type or wrong identifiers ")
      }

      // Initializing defaults
      // Setting data
      var weightPerRound = game.weightsPerRound
      var numRounds = game.weightsPerRound
      var botDifficulty = game.botDifficulty

      // Meta data
      var round: Int = 0
      var turn: String = ""
      var humanNames = Array[String]()
      var botNames = Array[String]()

      // State to parse from file
      var newState: State = null

      // Blocks to parse
      val blocksToProcess = Map.from(blocks.map((_,false)))
      var block = ""

      do {
        line = lr.readLine().trim
        if (line.nonEmpty) {
          if (line == "END") {
            line = null
          } else if (line(0) == '#') {
            block = line.substring(1).trim.toLowerCase
            if (blocks.contains(block) && !blocksToProcess(block)) {
              blocksToProcess(block) = true
            }
          } else if (blocks.contains(block)) {
            // Ex:        WeightPerRound: 20
            val keyValuePair = line.split(':').map(_.trim)

            if (keyValuePair.length != 1 && keyValuePair.length != 2) {
              throw new ParseError(line + "\n=> Must be 'key:value' pair or 'key:  '")
            }

            // Extract
            var key = keyValuePair(0).toLowerCase
            var value = if (keyValuePair.length == 1) "" else keyValuePair(1)

            // Parse the block
            block match {
              case "setting" if(value != "") =>
                key match {
                  case "weightperround" =>
                    weightPerRound = value.toIntOption.getOrElse(
                      throw new ParseError(line + "\n=> Number of weights must be an integer"))
                  case "numberofround" =>
                    numRounds = value.toIntOption.getOrElse(
                      throw new ParseError(line + "\n=> Number of Round must be an integer"))
                  case "botdifficulty" =>
                    botDifficulty = value.toDoubleOption.getOrElse(
                      throw new ParseError(line + "\n=> Bot Difficulty must be between 0 and 1")
                    )
                    if(botDifficulty < 0 || botDifficulty > 1){
                      throw new ParseError(line + "\n=> Bot Difficulty must be between 0 and 1")
                    }
                  case _ =>
                }
              case "meta" if(value != "") =>
                key match {
                  case "human" =>
                    humanNames = value.split(",").map(_.trim)
                  case "bot" =>
                    botNames =  value.split(",").map(_.trim)
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
                // Extract
                var parentScaleCode = splittedKey(0)(0)
                val posOnParentScale = splittedKey(1).toIntOption.getOrElse(
                  throw new ParseError(line + "\n=> Position must be an interger")
                )
                val scaleRadius = splittedKey(2).toIntOption.getOrElse(
                  throw new ParseError(line + "\n=> Scale radius must be an interger")
                )
                val scaleCode = splittedKey(3)(0)
                val splittedValue = if (value != "") value.split('|').map(_.trim) else Array[String]()

                // Helper function to parse the stacks of the scale
                def parseStacks(parentScale: Scale) = {
                  for (stackString <- splittedValue) {
                    val stackStringSplitted = stackString.split(',').map(_.trim)

                    val pos = stackStringSplitted(0).toIntOption.getOrElse(
                      throw new ParseError(line + "\n=> Stack position must be an integer")
                    )

                    stackStringSplitted.drop(1).map(_ (0)).foreach(w_code => {
                      newState.buildWeight(pos, parentScale, newState.players.find(_.playerCode == w_code))
                    })
                  }
                }

                // Base scale (parent scale is just "_")
                if (parentScaleCode == '_') {
                  // Apply setting
                  game.baseScaleRadius = scaleRadius

                  newState = new State(game)

                  humanNames.foreach(newState.buildHuman)
                  botNames.foreach(newState.buildBot)

                  parseStacks(state.baseScale)
                } else {
                  newState.scaleWithCode(parentScaleCode) match {
                    case Some(parentScale: Scale) =>
                      val newScale = newState.buildScale(posOnParentScale, scaleRadius, parentScale, Some(scaleCode))
                      parseStacks(newScale)
                    case None =>
                      throw new ParseError(line + "\n=> Invalid parent scale code")
                  }
                }
              case _ =>
            }
          }
        }
      } while (line != null)

      lr.close()

      if (blocksToProcess.valuesIterator.contains(false)) {
        throw new ParseError("Missing blocks entry in file: " + blocksToProcess.filter(!_._2).keys.mkString(","))
      }

      // End parsing
      println(s"Successfully load '$filePath'")

      newState.currentRound = round
      newState.currentTurnIdx = if (turn == "") 0 else newState.players.indexWhere(_.name == turn)
      game.botDifficulty = botDifficulty
      game.weightsPerRound = weightPerRound
      game.numRounds = numRounds

      // Finally, update state
      game.state = newState

    } catch {
      case e: ParseError =>
        println(e.getMessage)
        // Restore when failed
        game.baseScaleRadius = baseScaleRadiusBak
    }

  }
}

