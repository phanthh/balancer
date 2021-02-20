import game.{ConsoleManager, Game}

object MainText extends App {
  val game = new Game(2)
  val console = new ConsoleManager(game)
  console.run()
}
