package game.objects

import game.{Game, GameObject}

class Weight(val stack: Stack, id: String, game: Game, val owner: Option[Player] = None)
  extends GameObject(id, game) with Owner with Mass with Scorable {
  override val mass: Int = 1
  override def score_of(player: Player): Int = {
    owner match {
      case Some(p) if(p eq player) => 1
      case _ => 0
    }
  }
}

