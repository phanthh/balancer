import game.{Game, UI}
import game.UI.{GRAPHIC, CONSOLE}

object MainText extends App {
  val game = new Game(2)
  UI(game, CONSOLE).run()
}
