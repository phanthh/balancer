package game.objects

import game.{Game, GameObject}

sealed abstract class Player(_id: String, _game: Game)
  extends GameObject(_id, _game){
  val name: String
  val player_code: Char
  def score: Int
  def place_weight(): Unit
}

case class Bot(val name: String, val player_code: Char, _id: String, _game: Game)
  extends Player(_id, _game) {

  override def score: Int = ???

  override def place_weight(): Unit = ???
}

case class Human(val name: String, val player_code: Char, _id: String, _game: Game)
  extends Player(_id, _game) {

  override def score: Int = ???

  override def place_weight(): Unit = ???
}
