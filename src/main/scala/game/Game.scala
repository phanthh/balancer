package game

import game.grid.Grid
import game.objects.{Scale, Weight}
import game.ui.ConsoleManager

import scala.collection.Map

class Game {
  val grid = new Grid(20,40)
  val gameObject = Map[String, GameObject]()
//  val ground = add_scale(None, 0)

  def add_weight(scale: Scale, pos: Int): Weight = ???
  def add_scale(scale: Option[Scale], pos: Int): Scale = {
    ???
  }
}
