import game.{Game, GraphicManager}
import scalafx.application.JFXApp

object Main extends JFXApp {
  val game = new Game(2)
  (new GraphicManager(game)).run()
}