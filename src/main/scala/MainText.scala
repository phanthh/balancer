import balancer.Game
import balancer.objects.Command.placeWeight
import balancer.objects.{Bot, Human, Scale}

import java.io.IOException
import scala.io.StdIn.{readChar, readInt, readLine}
import scala.util.Random

final case class InvalidInput(private val message: String = "",
                              private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

object MainText extends App {
  val game = new Game()
  def state = game.state

  val filename = if(args.nonEmpty) args(0) else null
  if(filename == null)
    promptInput()
  else
    game.fileManager.loadGame(filename)

  println("Welcome to Balancer !!")

  while(state.currentRound <= game.numRounds && !(game.over)){
    var players = state.players
    state.weightLeftOfRound = game.weightsPerRound
    state.currentTurnIdx = 0
    println(f"============ ROUND ${state.currentRound}%2s ============")
    while(state.weightLeftOfRound > 0 && !(game.over)) {
      players(state.currentTurnIdx) match {
        case h: Human =>
          // TODO: Refracting, Exception handling
          printGameState()
          println(f">>>>>>>>> ${players(state.currentTurnIdx).name.toUpperCase}%-5s TURN <<<<<<<<<")

          var parentScale: Scale = null
          var pos: Int = 0

          while(parentScale == null){
            try {
              print(s"Which scale ? (${state.scales.map(_.code).mkString(",")}): ")
              parentScale = state.scaleWithCode(readChar()).getOrElse(
                throw new InvalidInput(s"Invalid scale code must be: ${state.scales.map(_.code).mkString(",")}")
              )
            } catch {
              case e: IOException =>
                println(e.getMessage); parentScale = null
              case e: InvalidInput =>
                println(e.getMessage); parentScale = null
              case e: Exception =>
                println("Invalid Input" + e.getMessage); parentScale = null
            }
          }

          while(pos == 0){
            try {
              print(s"Position ? [-${parentScale.radius},${parentScale.radius}]: ")
              pos = readInt()
              if(pos == 0) throw new InvalidInput("Position cannot be 0")

              //// GAME STATE CHANGED HERE
              state.undoStack.append(placeWeight(h, pos, parentScale, state).execute())
            } catch {
              case e: IOException =>
                println(e.getMessage); pos = 0
              case e: InvalidInput =>
                println(e.getMessage); pos = 0
              case e: ArrayIndexOutOfBoundsException =>
                println(s"Invalid position, must be between -${parentScale.radius} and ${parentScale.radius}"); pos = 0
              case e: Exception =>
                println("Invalid Input: " + e.getMessage); pos = 0
            }
          }

        case b: Bot =>
          printGameState() // TODO: Delete this if there is human players
          if(Random.nextFloat() > game.botDiffiiculty) {
            b.random()
          } else {
            b.bestMove()
          }
      }
      state.weightLeftOfRound -= 1
      state.currentTurnIdx += 1
      if(state.currentTurnIdx >= players.length) state.currentTurnIdx = 0
      state.deleteFlippedScale()
    }
    println("End of round !!")
    println(s"The winner of this rounds is: ${game.winner}")
    game.winner.incRoundWon()
    state.currentRound += 1
  }
  println("================================================")
  println(s"The winner of the balancer is: ${game.finalWinner}")
  println("================================================")
  println("========== !!!! Congratulation !!!! ============")
  println("================================================")
  printGameState()

  private def printGameState() = {
    printScoreBoard()
    state.scales.foreach(printScale)
    printGrid()
  }

  private def printScoreBoard() = {
    val scoreBoard = state.players.map(p =>
      f"| ${p.name}%-5s (${p.playerCode}, ${p.roundWon}) : ${p.score}%5s points (${if(p.isInstanceOf[Human]) "human" else "bot"}%5s)  |"
    ).mkString("\n")
    println("_"*(scoreBoard.length/state.players.length))
    println(scoreBoard)
    println("|" + "_"*(scoreBoard.length/state.players.length-2) + "|")
  }

  private def printScale(scale: Scale) = {
    println()
    println(s"Scale [${scale.code},${scale.radius}] Coord: ${scale.coord}")
    println(s"uHeight: ${scale.uHeight}, lHeight: ${scale.lHeight}") // TODO: THis is for debug only
    println(s"Span ${scale.span}") // TODO: THis is for debug only
    println("----------------------------------")
    val rep = scale.boardVector.zipWithIndex.map {
      case (Some(p), i) => p.toString
      case (None, i) if i==scale.radius => s"[${scale.code}]"
      case (None, i) => "-"
    }.mkString
    println(s"$scale: " + "<" + rep + ">")
    println(s"TORQUE: [${scale.leftTorque}--${scale.mass}--${scale.rightTorque}]")
    println("STATUS: " + (if(scale.isBalanced) "Balanced" else "Flipped"))
    println(s"OWNER: ${scale.owner match { case Some(p) => p.name case None => "No one"}}")
    println()
  }

  private def printGrid() = {
    game.grid.update()
    for(i <- 0 until game.grid.height){
      for(j <- 0 until game.grid.width)
        print(game.grid(i,j))
      println()
    }
  }


  private def promptInput() = {
    var numHumans = -1
    var numBots = -1
    while(numHumans == -1){
      try {
        print("Enter the number of Human players: ")
        numHumans = readInt()
      } catch {
        case e: IOException => println(e.getMessage)
        case e: Exception => println("Invalid Input" + e.getMessage)
      }
    }

    while(numHumans == -1){
      try {
        print("Enter the number of Bots: ")
        numBots = readInt()
      } catch {
        case e: IOException => println(e.getMessage)
        case e: Exception => println("Invalid Input" + e.getMessage)
      }
    }

    for(i <- 1 to numHumans){
      print(f"Enter the #$i human's name: ")
      state.buildHuman(readLine())
    }

    for(i <- 1 to numBots){
      print(f"Enter the #$i bots's name: ")
      state.buildBot(readLine())
    }
  }
}

