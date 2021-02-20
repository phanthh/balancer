package game

import game.grid.Grid
import game.objects.{Factory, Scale}

import scala.collection.mutable.Map

class Game {
  def register(pair: (String, GameObject)) = gameObjects += pair

  val grid = new Grid(20,40)
  val factory = Factory(this)
  val gameObjects = Map[String, GameObject]()

  def baseScale = gameObjects("s1").asInstanceOf[Scale]
  def scales = gameObjects.filter(_._1.startsWith(Factory.SCALE)).values.map(_.asInstanceOf[Scale])

  register(factory.build_first_scale(radius=5))

  // add_scale(parent_scale = ground_scale, radius = 3, pos = -5)
}
