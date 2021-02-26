package game

import game.objects.Scale

import java.io.{BufferedReader, FileNotFoundException, FileReader}
import scala.collection.mutable.Map

final case class ParseError(private val message: String = "",
                            private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

class FileManager(private val game: Game) {

  def save_game(filePath: String) = ??? // TODO: File Managing, save / load from file

  def load_game(filePath: String): Unit = {
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
      var newFactory: Factory = null

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

            if(trimmedLine.length != 2)
              throw new ParseError(line + "\n=> Must be 'key:value' pair")

            val key = trimmedLine(0).toLowerCase
            val value = trimmedLine(1)

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

                  val splittedValue = value.split('|').map(_.trim)

                  // Special case for base scale (parent scale is just "_")
                  // This is where the factory is initialized (in a file, there should only be one time this is called)
                  if(parent_scale_code == '_') {
                    newFactory = Factory(game, scale_radius)

                    // Adding players
                    human_names.foreach(newFactory.build_human)
                    bot_names.foreach(newFactory.build_bot)

                    // TODO: Settings file to initialize factory or Included in the Saved File as well?
                    for(stackString <- splittedValue){
                      val stackStringSplitted = stackString.split(',').map(_.trim)
                      val pos = stackStringSplitted(0).toIntOption.getOrElse(
                        throw new ParseError(line + "\n=> Stack position must be an integer")
                      )

                      stackStringSplitted.drop(1).map(_(0)).foreach(w_code => {
                        newFactory.build_weight(pos, newFactory.baseScale, newFactory.players.find(_.player_code==w_code))
                      })
                    }
                  } else {
                    newFactory.scaleWithCode(parent_scale_code) match {
                      case Some(parent_scale: Scale) =>
                        val newScale = newFactory.build_scale(pos_on_parent_scale, scale_radius, parent_scale, Some(scale_code))

                        for(stackString <- splittedValue){
                          val stackStringSplitted = stackString.split(',').map(_.trim)
                          val pos = stackStringSplitted(0).toIntOption.getOrElse(
                            throw new ParseError(line + "\n=> Stack position must be an integer")
                          )

                          stackStringSplitted.drop(1).map(_(0)).foreach(w_code => {
                            newFactory.build_weight(pos, newScale, newFactory.players.find(_.player_code==w_code))
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
