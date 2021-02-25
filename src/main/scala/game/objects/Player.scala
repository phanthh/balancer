package game.objects

import game.{Game, GameObject}

sealed abstract class Player(val name: String, _id: String, _game: Game)
  extends GameObject(_id, _game){
  def player_code: Char = name(0)
  def score: Int = game.baseScale.score_of(this)
  var roundWon = 0
}

case class Bot(_name: String , _id: String, _game: Game)
  extends Player(_name, _id, _game) {
  def place_weight(): Unit = {
    val place_pos = 0
    val parent_scale = null

    // TODO: Implement the algorithm

    game.factory.build_weight(place_pos, parent_scale, Some(this))
  }
}

case class Human(_name: String, _id: String, _game: Game)
  extends Player(_name, _id, _game) {
  def place_weight(): Unit = {

  }
}

// TODO: add algorithm for Bot
