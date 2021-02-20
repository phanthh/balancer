package game.objects

sealed abstract class Player {
  val name: String
  val player_code: Char
  def score: Int
  def place_weight(): Unit
}

case class Bot(val name: String, val player_code: Char) extends Player {

  override def score: Int = ???

  override def place_weight(): Unit = ???
}

case class Human(val name: String, val player_code: Char) extends Player {

  override def score: Int = ???

  override def place_weight(): Unit = ???
}
