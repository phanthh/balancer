import game.{ConsoleManager, Game}

object MainText extends App {
  val game = new Game
  val console = new ConsoleManager(game)
  console.run()
}
