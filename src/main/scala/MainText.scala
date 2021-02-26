import game.{ConsoleManager, Game, UI}
import game.UI.{CONSOLE, GRAPHIC}

object MainText extends App {
  val game = new Game(2)
  val savedfile = if(args.nonEmpty) args(0) else null
  (new ConsoleManager(game)).run(savedfile)
}
