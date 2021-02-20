package game.objects

import game.{Game, GameObject}

sealed abstract class Player(val name: String, _id: String, _game: Game)
  extends GameObject(_id, _game){
  def player_code: Char = name(0)
  def score: Int = game.baseScale.score_of(this)
  def place_weight(): Unit
}

case class Bot(_name: String , _id: String, _game: Game)
  extends Player(_name, _id, _game) {

  override def place_weight(): Unit = ???
}

case class Human(_name: String, _id: String, _game: Game)
  extends Player(_name, _id, _game) {

  override def place_weight(): Unit = ???
}

// TODO: add algorithm for Bot
