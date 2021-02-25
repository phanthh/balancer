package game
import game.objects.{Bot, Human, Player, Scale}

import scala.io.StdIn._

object UI {
  val CONSOLE = 0
  val GRAPHIC = 1

  def apply(game: Game, types: Int = CONSOLE) = {
    types match {
      case GRAPHIC => new GraphicManager(game)
      case _ => new ConsoleManager(game)
    }
  }
}

sealed abstract class UI {
  val game: Game
  def run(): Unit
}

case class GraphicManager(val game: Game) extends UI {
  override def run(): Unit = ??? // TODO: Graphical Interface
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
      println(f"========== ROUND $i%2s ==========")
      while(weightsLeft > 0) {
        print_game_state()
        println(f">>>>>>>>> ${players(idx).name.toUpperCase}%-5s TURN <<<<<<<<<")
        players(idx) match {
          case p: Player =>
            // TODO: Refracting, Exception handling
            println(s"Which scale ? (${game.scales.map(_.scale_code).mkString(",")}): ")
            val parent_scale = game.scaleWithCode(readChar()).getOrElse(throw new Exception("Scale code invalid"))
            println(s"Position ? [-${parent_scale.radius},${parent_scale.radius}]: ")
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

  private def print_game_state() = {
    print_score_board()
    game.scales.foreach(print_scale)
  }

  private def print_score_board() = {
    val score_board = game.players.map(p =>
      f"| ${p.player_code}: ${p.score}%2s points (${if(p.isInstanceOf[Human]) "human" else "bot"}%5s)  |"
    ).mkString("\n")
    println("_________________________")
    println(score_board)
    println("|_______________________|")
  }

  private def print_scale(scale: Scale) = {
    println()
    println(s"Scale [${scale.scale_code},${scale.radius}]")
    println("----------------------------")
    val rep = scale.board.zipWithIndex.map {
      case (Some(p), i) => p.toString
      case (None, i) if i==scale.radius => s"[${scale.scale_code}]"
      case (None, i) => "-"
    }.mkString
    println(s"$scale: " + "<" + rep + ">")
    println(s"TORQUE: [${scale.left_torque}--${scale.mass}--${scale.right_torque}]")
    println("STATUS: " + (if(scale.isBalanced) "Balanced" else "Flipped"))
    println(s"OWNER: ${scale.owner match { case Some(p) => p.name case None => "No one"}}")
    println()
  }

  private def updateGrid() = ??? // TODO: Grid implementation for console screen
  private def drawGrid() = ???
}
