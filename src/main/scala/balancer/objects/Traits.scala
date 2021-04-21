package balancer.objects

import balancer.State
import balancer.grid.Coord

/**
 * Primitive traits to better differentiate
 * between Stack, Weight and Scale
 */

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

trait GameObject {
  protected val state: State
}



