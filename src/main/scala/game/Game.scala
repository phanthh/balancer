package game

import game.grid.Grid
import game.objects.{Scale, Weight}

import scala.collection.mutable.Map

class Game {
  val grid = new Grid(20,40)
  val gameObject = Map[String, GameObject]()

  private var _idCounter = 0
  val ground_scale = add_scale(None, 5, 0)

  // add_scale(parent_scale = ground_scale, radius = 3, pos = -5)

  def add_weight(scale: Scale, pos: Int): Weight = ???
  def add_scale(parent_scale: Option[Scale], radius: Int, pos: Int): Scale = {
    val newID = "s" + _idCounter.toString
    _idCounter += 1
    val newScale = new Scale(parent_scale, radius, newID, this)
    if(parent_scale.nonEmpty) {
      parent_scale.
    }
    gameObject += (newID -> newScale)
    newScale
  }
}
