package game
import game.objects.{Factory, Scale}

import scala.io.StdIn._

sealed abstract class UI {
  val game: Game
  def run(): Unit
  def round_loop(): Unit
}

case class GraphicManager(val game: Game) extends UI {
  override def run(): Unit = ???
  override def round_loop(): Unit = ???

}

case class ConsoleManager(val game: Game) extends UI {
  def factory = game.factory
  override def run(): Unit = {
    println("Welcome to Balancer !!")
    println("Enter the number of Human players: ")
    val numHumans = readInt()
    println("Enter the number of Bots: ")
    val numBots = readInt()
    for(i <- 1 to numHumans){
      println(f"Enter the #$i human's name: ")
      game.register(factory.build_human(readLine()))
    }

    for(i <- 1 to numBots){
      println(f"Enter the #$i bots's name: ")
      game.register(factory.build_bot(readLine()))
    }

    for(i <- 1 to game.numRounds){
      round_loop()
    }
  }

  def print_game_state() = ???

  override def round_loop(): Unit = {
    print_game_state()

    println("Enter the parent scale: ")
    val parentScale = game.scale_with_id(Factory.


  }
}

// TODO: Implement print_game_state()