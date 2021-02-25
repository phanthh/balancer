package game.objects

import game.Game

class Weight(val stack: Stack, val id: String, val game: Game, var owner: Option[Player] = None)
  extends GameObject with Owner with Mass with Scorable {
  override val mass: Int = 1
  override def score_of(player: Player): Int = {
    owner match {
      case Some(p) if(p eq player) => 1
      case _ => 0
    }
  }

  override def toString: String = owner match {
    case Some(p) => p.player_code.toString
    case None => "x"
  }
}

