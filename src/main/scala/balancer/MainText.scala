package balancer

import balancer.objects.Command.placeWeight
import balancer.objects.{Bot, Human, Scale}
import balancer.utils.{InvalidInput, OccupiedPosition}

import java.io.IOException
import scala.io.StdIn.{readChar, readInt, readLine}
import scala.util.Random


object MainText extends App {
  val game = new Game()
  def state = game.state

  val filename = if(args.nonEmpty) args(0) else null
  if(filename == null)
    promptInput()
  else
    game.fileManager.loadGame(filename)

  // Start
  println("Welcome to Balancer !!")
  run()

  def run(): Unit = {
    while(state.currentRound <= game.numRounds){
      state.weightLeftOfRound = game.weightsPerRound
      state.currentTurnIdx = 0
      println(f"============ ROUND ${state.currentRound}%2s ============")
      while(state.weightLeftOfRound > 0) {
        state.players(state.currentTurnIdx) match {
          case human: Human => {
            printGameState()
            println(f">>>>>>>>> ${state.currentTurn.name.toUpperCase}%-5s TURN <<<<<<<<<")
            var parentScale = promptScale()
            var pos = promptPos(parentScale)
            try {
              state.execute(placeWeight(human, pos, parentScale, state))
            } catch {
              case e: ArrayIndexOutOfBoundsException =>
                println(s"Invalid position, must be between -${parentScale.radius} and ${parentScale.radius}"); pos = 0
              case e: OccupiedPosition =>
                println("Position has already been occupied")
            }
          }
          case bot: Bot =>{
            if(state.humans.isEmpty){
              printGameState()
            }
            if(Random.nextFloat() > game.botDifficulty) {
              bot.random()
            } else {
              bot.bestMove()
            }
          }
        }
        state.weightLeftOfRound -= 1
        state.currentTurnIdx += 1
        if(state.currentTurnIdx >= state.players.length){
          state.currentTurnIdx = 0
        }
        state.deleteFlippedScale()
        if(game.over) return
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
  }

  private def printGameState() = {
    printScoreBoard()
//    state.scales.foreach(printScale)
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

  private def promptScale() = {
    var scale: Scale = null
    while(scale == null){
      try {
        print(s"Which scale ? (${state.scales.map(_.code).mkString(",")}): ")
        scale = state.scaleWithCode(readChar()).getOrElse(
          throw new InvalidInput(s"Invalid scale code must be: ${state.scales.map(_.code).mkString(",")}")
        )
      } catch {
        case e: IOException =>
          println(e.getMessage); scale = null
        case e: InvalidInput =>
          println(e.getMessage); scale = null
      }
    }
    scale
  }

  private def promptPos(scale: Scale) = {
    var pos = 0
    while(pos == 0){
      try {
        print(s"Position ? [-${scale.radius},${scale.radius}]: ")
        pos = readInt()
        if(pos == 0) throw new InvalidInput("Position cannot be 0")
      } catch {
        case e: IOException =>
          println(e.getMessage); pos = 0
        case e: InvalidInput =>
          println(e.getMessage); pos = 0
      }
    }
    pos
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

