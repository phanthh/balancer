package game

import game.objects.{Scale, Stack}

import java.io.{BufferedReader, BufferedWriter, FileNotFoundException, FileReader, FileWriter}
import scala.collection.mutable.{Map, Buffer}

final case class ParseError(private val message: String = "",
                            private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

class FileManager(private val game: Game) {

  def saveGame(filePath: String) = {
    val lw = new BufferedWriter(new FileWriter(filePath))

    lw.write("BALANCER 1.3 SAVE FILE\n")
    lw.write("# Meta\n")
    lw.write("Human: " + game.players.map(_.name).mkString(",") + "\n")
    lw.write("Round: " + game.currentRound + "\n")
    lw.write("Turn: " + game.currentTurn + "\n")
    lw.write("# Scale\n")
    for(scale <- game.scales.sortBy(_.scale_code)){
      if(scale == game.baseScale)
        lw.write("_,0")
      else
        lw.write(s"${scale.parent_scale.scale_code},${scale.pos}")
      lw.write(s",${scale.radius},${scale.scale_code} : ")
      val buf = Buffer[String]()
      scale.boardVector.flatten.foreach {
        case stack: Stack =>
          buf.append(stack.weightsVector.flatMap(_.owner).map(_.player_code).prepended(stack.pos.toString).mkString(","))
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
    try {

      var line = lineReader.readLine().trim.toLowerCase

      if (!((line.startsWith("balancer")) && (line.endsWith("save file")))) {
        throw new ParseError("Unknown file type / identifiers ")
      }

      // Meta data
      var round: Int = 0
      var turn: String = null
      var human_names = Array[String]()
      var bot_names = Array[String]()

      // We will try to build a new factory from file
      var newFactory: Store = null

      val blocksToProcess =
        Map[String, Boolean]("meta" -> false, "scale" -> false)

      var block = ""

      do{
        line = lineReader.readLine().trim
        if(line.nonEmpty) {
          if(line == "END")
            line = null
          else if(line(0) == '#')
            block = line.substring(1).trim.toLowerCase
          else if (blocksToProcess.contains(block)){
            if(!blocksToProcess(block)) blocksToProcess(block) = true

            val trimmedLine = line.split(':').map(_.trim)

            if(trimmedLine.length > 2 || trimmedLine.length == 0)
              throw new ParseError(line + "\n=> Must be 'key:value' pair")

            var key: String = trimmedLine(0).toLowerCase
            var value: String = if(trimmedLine.length == 1) "" else trimmedLine(1)

            block match {
              case "meta" =>
                key match {
                  case "human" =>
                    human_names = value.split(",").map(_.trim)
                  case "bot" =>
                    bot_names = value.split(",").map(_.trim)
                  case "round" =>
                    round = value.toIntOption.getOrElse(
                      throw new ParseError(line + "\n=> Round number must be an integer")
                    )
                  case "turn" =>
                    turn = value
                  case _ =>
                }
              case "scale" =>
                  val splittedKey = key.split(',').map(_.trim)

                  if(splittedKey.length != 4)
                    throw new ParseError(line + "\n=> Must be 4 characters: 'parent_scale_code, position_on_parent_scale, radius, code'")
                  var parent_scale_code = splittedKey(0)(0)
                  val pos_on_parent_scale = splittedKey(1).toIntOption.getOrElse(
                    throw new ParseError(line+ "\n=> Position must be an interger")
                  )
                  val scale_radius = splittedKey(2).toIntOption.getOrElse(
                    throw new ParseError(line + "\n=> Scale radius must be an interger")
                  )

                  val scale_code = splittedKey(3)(0)

                  val splittedValue = if(value != "") value.split('|').map(_.trim) else Array.empty[String]

                  // Special case for base scale (parent scale is just "_")
                  // This is where the factory is initialized (in a file, there should only be one time this is called)
                  if(parent_scale_code == '_') {
                    newFactory = Store(game, scale_radius)

                    // Adding players
                    human_names.foreach(newFactory.buildHuman)
                    bot_names.foreach(newFactory.buildBot)

                    // TODO: Settings file to initialize factory or Included in the Saved File as well?
                    for(stackString <- splittedValue){
                      val stackStringSplitted = stackString.split(',').map(_.trim)
                      val pos = stackStringSplitted(0).toIntOption.getOrElse(
                        throw new ParseError(line + "\n=> Stack position must be an integer")
                      )

                      stackStringSplitted.drop(1).map(_(0)).foreach(w_code => {
                        newFactory.buildWeight(pos, newFactory.baseScale, newFactory.players.find(_.player_code==w_code), soft_append = true)
                      })
                    }
                  } else {
                    newFactory.scaleWithCode(parent_scale_code) match {
                      case Some(parent_scale: Scale) =>
                        val newScale = newFactory.buildScale(pos_on_parent_scale, scale_radius, parent_scale, Some(scale_code))

                        for(stackString <- splittedValue){
                          val stackStringSplitted = stackString.split(',').map(_.trim)
                          val pos = stackStringSplitted(0).toIntOption.getOrElse(
                            throw new ParseError(line + "\n=> Stack position must be an integer")
                          )

                          stackStringSplitted.drop(1).map(_(0)).foreach(w_code => {
                            newFactory.buildWeight(pos, newScale, newFactory.players.find(_.player_code==w_code), soft_append = true)
                          })
                        }
                      case None => throw new ParseError(line + "\n=> Invalid parent scale code")
                    }
                  }
              case _ =>
            }
          }
        }
      } while (line != null)

      lineReader.close()

      if(blocksToProcess.valuesIterator.contains(false))
        throw new ParseError("Missing blocks entry in file: " + blocksToProcess.filter(!_._2).keys.mkString(","))
      // END PARSING
      println(s"Successfully load '$filePath'")

      // Here the saved file should be successfully parsed and ready
      game.factory = newFactory
      game.currentRound = round
      game.currentTurn = turn

    } catch {
      case e: ParseError => println(e.getMessage)
    }

  }
}
