import game.{Game, GraphicManager}
import scalafx.application.ApplicationIncludes

object MainGUI extends App {
  val game = new Game(numRounds=3, weightsPerRound=20)

}