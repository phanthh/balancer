import game.{Game, UI}
import game.UI.{CONSOLE, GRAPHIC}
import scalafx.application.JFXApp

object Main extends JFXApp {
  val game = new Game(2)
  UI(game, GRAPHIC).run()
}