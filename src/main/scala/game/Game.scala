package game

import game.grid.Grid
import game.objects.{Factory, Player, Scale, Stack, Weight}

import scala.collection.mutable.Map

class Game {
  def register(pair: (String, GameObject)) = gameObjects += pair

  val grid = new Grid(20,40)
  val factory = Factory(this)
  val gameObjects = Map[String, GameObject]()

  register(factory.build_first_scale(radius=5))



  // add_scale(parent_scale = ground_scale, radius = 3, pos = -5)

}
