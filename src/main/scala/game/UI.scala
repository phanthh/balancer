package game
import game.objects.{Bot, Player, Scale}

import scala.io.StdIn._

object UI {
  val CONSOLE = 0
  val GRAPHIC = 1
}

sealed abstract class UI {
  val game: Game
  def run(): Unit
}

case class GraphicManager(val game: Game) extends UI {
  override def run(): Unit = ???
}

case class ConsoleManager(val game: Game) extends UI {
  override def run(): Unit = {
    println("Welcome to Balancer !!")
    println("Enter the number of Human players: ")
//    val numHumans = readInt()
    val numHumans = 2 // TESTING ONLY
//    println("Enter the number of Bots: ")
//    val numBots = readInt()

    for(i <- 1 to numHumans){
      println(f"Enter the #$i human's name: ")
      game.factory.build_human(readLine())
    }

//    for(i <- 1 to numBots){
//      println(f"Enter the #$i bots's name: ")
//      game.register(factory.build_bot(readLine()))
//    }

    for(i <- 1 to game.numRounds){
      var weightsLeft = game.weightsPerRound
      var players = game.players.toList
      var idx = 0
      println(s"=============== ROUND NUMBER $i ================")
      while(weightsLeft > 0) {
        print_game_state()
        println(s" ${players(idx).name} Turn !!!!")
        players(idx) match {
          case p: Player =>
            // TODO: Refractor, add better prompt, and refactor exception
            // TODO: Custom Exception
            println("Please enter the scale code: ")
            val parent_scale = game.scaleWithCode(readChar()).getOrElse(throw new Exception("Scale code invalid"))
            println("Please enter the pos: ")
            val pos = readInt()
            game.factory.build_weight(pos, parent_scale, Some(p))

          case b: Bot =>
            b.place_weight()
        }
        weightsLeft -= 1
        idx += 1
        if(idx >= players.length) idx = 0
      }
      println("End of round !!")
      println(s"The winner of this rounds is: ${game.winner}")
      game.winner.roundWon += 1
    }
    println("================================================")
    println(s"The winner of the game is: ${game.finalWinner}")
    println("========== Congratulation !!!! =================")
  }

  // TODO: Implement print_game_state()
  private def print_game_state() = {
    println(game.scales.map(_.toString).mkString) // In developement only
  }

  private def updateGrid() = {
  }

  private def drawGrid() = {
  }

  private def print_scale(scale: Scale) = {
  }
}
