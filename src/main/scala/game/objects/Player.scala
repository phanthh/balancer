package game.objects

import game.Factory

sealed abstract class Player
  extends GameObject {
  val name: String
  def player_code: Char = name(0)
  def score: Int = factory.baseScale.score(this)
  var roundWon = 0
}

case class Bot(val name: String, val factory: Factory)
  extends Player {
  def place_weight(): Unit = {
    val place_pos = 0
    val parent_scale = null

    // TODO: Implement the algorithm

    factory.build_weight(place_pos, parent_scale, Some(this))
  }
}

case class Human(val name: String, val factory: Factory) extends Player