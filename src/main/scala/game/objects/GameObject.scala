package game.objects

import game.Factory
import game.grid.Coord

trait Owner {
  def owner: Option[Player]
  def score(player: Player): Int
  def count(player: Player): Int
}

trait Mass {
  def mass: Int
}

trait Renderable {
  def height: Int
  def coord: Coord
}

abstract class GameObject {
  protected val factory: Factory
}



