import game.{Game, GraphicManager}
import scalafx.application.JFXApp

object Main extends JFXApp {
  val game = new Game(2)
  val graphic = new GraphicManager(game)
  graphic.run()
}
