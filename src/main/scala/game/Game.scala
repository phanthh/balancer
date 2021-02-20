package game

import game.grid.Grid
import game.objects.{Factory, Scale}
import game.UI._

import scala.collection.mutable.Map

class Game (interface: Int , val numRounds: Int = 5, val weightsPerRound: Int = 10) {
  def register(pair: (String, GameObject)) = gameObjects += pair

  val grid = new Grid(20,40)
  val factory = Factory(this)
  val gameObjects = Map[String, GameObject]()
  val ui: UI = interface match {
    case CONSOLE => new ConsoleManager(this)
    case GRAPHIC => new GraphicManager(this)
    case _ => new ConsoleManager(this)
  }

  private def scales = gameObjects.filter(_._1.startsWith(Factory.SCALE)).values.map(_.asInstanceOf[Scale])
  def scaleWithCode(code: Char): Option[Scale] = scales.find(_.scale_code == code)
  def baseScale: Scale = scaleWithCode('a').getOrElse(???) // TODO: Exception handling

  register(factory.build_first_scale(radius=5))
  register(factory.build_bot("Steven"))
}
