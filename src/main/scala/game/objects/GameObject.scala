package game.objects

import game.Factory

trait Owner {
  def owner: Option[Player]
  def score(player: Player): Int
  def count(player: Player): Int
}

trait Mass {
  def mass: Int
}

trait Height {
  def height: Int
}

abstract class GameObject {
  protected val factory: Factory
}



