package game.objects

import game.Game

sealed abstract class Player
  extends GameObject {
  val name: String
  def player_code: Char = name(0)
  def score: Int = game.baseScale.score_of(this)
  var roundWon = 0
}

case class Bot(val name: String, val game: Game)
  extends Player {
  def place_weight(): Unit = {
    val place_pos = 0
    val parent_scale = null

    // TODO: Implement the algorithm

    game.factory.build_weight(place_pos, parent_scale, Some(this))
  }
}

case class Human(val name: String, val game: Game) extends Player