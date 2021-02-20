package game

import game.grid.Grid
import game.objects.{Player, Scale, Weight}

import scala.collection.mutable.Map

class Game {
  val grid = new Grid(20,40)
  val gameObject = Map[String, GameObject]()

  private var _idCounter = 0
  val ground_scale = add_first_scale(5)

  // add_scale(parent_scale = ground_scale, radius = 3, pos = -5)

  def add_weight(pos: Int, parent_scale: Scale, owner: Option[Player] = None): Weight = {
    val newID = "w" + _idCounter.toString
    _idCounter += 1
    val newWeight = new Weight(Some(parent_scale), newID, this)
    newWeight.set_owner(owner)

    gameObject += (newID -> newWeight)
    newWeight
  }

  private def add_first_scale(radius: Int): Scale = {
    val newID = "s" + _idCounter.toString
    _idCounter += 1
    val newScale = new Scale(None, radius, newID, this)
    gameObject += (newID -> newScale)
    newScale
  }

  def add_scale(pos: Int, radius: Int, parent_scale: Scale): Scale = {
    val newID = "s" + _idCounter.toString
    _idCounter += 1
    val newScale = new Scale(Some(parent_scale), radius, newID, this)
    parent_scale.place_at(pos, newScale)
    gameObject += (newID -> newScale)
    newScale
  }
}
