package game.objects

trait Owner {
  def owner: Option[Player]
}

trait Mass {
  def mass: Int
}

trait Scorable {
  def score_of(player: Player): Int
}

trait Height {
  def height: Int
}