package game

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
  override def run(): Unit = {
    println("Welcome to Balancer !!")
  }
  override def round_loop(): Unit = ???
}
