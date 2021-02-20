package game

import game.grid.Grid
import game.objects.{Factory, Player, Scale, Stack, Weight}

import scala.collection.mutable.Map

class Game {
  val grid = new Grid(20,40)
  val gameObject = Map[String, GameObject]()
  val factory = new Factory(this)

  val ground_scale = factory.add_first_scale(5)


  // add_scale(parent_scale = ground_scale, radius = 3, pos = -5)

}
