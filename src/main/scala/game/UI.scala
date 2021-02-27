package game
import game.objects.Bot.{BESTMOVE, RANDOM, RANDOMBESTMOVE}
import game.objects.{Bot, Human, Player, Scale}

import java.io.IOException
import scala.io.StdIn._

object UI {
  val CONSOLE = 0
  val GRAPHIC = 1
}

sealed abstract class UI { val game: Game }

case class GraphicManager(val game: Game) extends UI {
  def run(): Unit = ??? // TODO: Graphical Interface
}

case class ConsoleManager(val game: Game) extends UI {
  def input_prompt() = {
//    var numHumans = -1
//    var numBots = -1
//    while(numHumans == -1){
//      try {
//        print("Enter the number of Human players: ")
//        numHumans = readInt()
//      } catch {
//        case e: IOException => println(e.getMessage)
//        case e: Exception => println("Invalid Input" + e.getMessage)
//      }
//    }
//
//    while(numHumans == -1){
//      try {
//        print("Enter the number of Bots: ")
//        numBots = readInt()
//      } catch {
//        case e: IOException => println(e.getMessage)
//        case e: Exception => println("Invalid Input" + e.getMessage)
//      }
//    }
//
//    for(i <- 1 to numHumans){
//      print(f"Enter the #$i human's name: ")
//      game.factory.build_human(readLine())
//    }
//
//    for(i <- 1 to numBots){
//      print(f"Enter the #$i bots's name: ")
//      game.factory.build_bot(readLine())
//    }

    game.factory.build_human("Hau") // TODO: Delete this, this is for debug
    game.factory.build_bot("Jack")
  }

  def run(filename: String = null): Unit = {
    println("Welcome to Balancer !!")
    if(filename == null)
      input_prompt()
    else
      game.fileManager.load_game(filename)

    while(game.currentRound <= game.numRounds){
      var weightsLeft = game.weightsPerRound
      var players = game.players
      var idx = 0
      println(f"============ ROUND ${game.currentRound}%2s ============")
      while(weightsLeft > 0) {
        players(idx) match {
          case h: Human =>
            // TODO: Refracting, Exception handling
            print_game_state()
            println(f">>>>>>>>> ${players(idx).name.toUpperCase}%-5s TURN <<<<<<<<<")

            var parent_scale: Scale = null
            var pos: Int = 0

            while(parent_scale == null){
              try {
                print(s"Which scale ? (${game.scales.map(_.scale_code).mkString(",")}): ")
                parent_scale = game.scaleWithCode(readChar()).getOrElse(
                  throw new InvalidInput(s"Invalid scale code must be: ${game.scales.map(_.scale_code).mkString(",")}")
                )
              } catch {
                case e: IOException =>
                  println(e.getMessage); parent_scale = null
                case e: InvalidInput =>
                  println(e.getMessage); parent_scale = null
                case e: Exception =>
                  println("Invalid Input" + e.getMessage); parent_scale = null
              }
            }

            while(pos == 0){
              try {
                print(s"Position ? [-${parent_scale.radius},${parent_scale.radius}]: ")
                pos = readInt()
                if(pos == 0) throw new InvalidInput("Position cannot be 0")

                //// GAME STATE CHANGED HERE
                game.factory.build_weight(pos, parent_scale, Some(h))
              } catch {
                case e: IOException =>
                  println(e.getMessage); pos = 0
                case e: InvalidInput =>
                  println(e.getMessage); pos = 0
                case e: ArrayIndexOutOfBoundsException =>
                  println(s"Invalid position, must be between -${parent_scale.radius} and ${parent_scale.radius}"); pos = 0
                case e: Exception =>
                  println("Invalid Input: " + e.getMessage); pos = 0
              }
            }

          case b: Bot =>
            print_game_state() // TODO: Delete this if there is human players
            b.place_weight(RANDOMBESTMOVE)
        }
        weightsLeft -= 1
        idx += 1
        if(idx >= players.length) idx = 0
      }
      println("End of round !!")
      println(s"The winner of this rounds is: ${game.winner}")
      game.winner.roundWon += 1
      game.currentRound += 1
    }
    println("================================================")
    println(s"The winner of the game is: ${game.finalWinner}")
    println("================================================")
    println("========== !!!! Congratulation !!!! ============")
    println("================================================")
  }

  private def print_game_state() = {
    print_score_board()
    print_grid()
//    game.scales.foreach(print_scale)
  }

  private def print_score_board() = {
    val score_board = game.players.map(p =>
      f"| ${p.name}%-5s (${p.player_code}) : ${p.score}%2s points (${if(p.isInstanceOf[Human]) "human" else "bot"}%5s)  |"
    ).mkString("\n")
    println("__________________________________")
    println(score_board)
    println("|________________________________|")
  }

  private def print_scale(scale: Scale) = {
    println()
    println(s"Scale [${scale.scale_code},${scale.radius}] Coord: ${scale.coord}")
    println(s"up_height: ${scale.up_height}, lo_height: ${scale.lo_height}") // TODO: THis is for debug only
    println(s"Span ${scale.span}") // TODO: THis is for debug only
    println("----------------------------------")
    val rep = scale.board_vec.zipWithIndex.map {
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

  private def print_grid() = {
    game.grid.update()
    for(i <- 0 until game.grid.height){
      for(j <- 0 until game.grid.width)
        print(game.grid(i,j))
      println()
    }
  }
}

final case class InvalidInput(private val message: String = "",
                            private val cause: Throwable = None.orNull)
  extends Exception(message, cause)
