package game.objects

import game.Factory

class Weight(val stack: Stack, val factory: Factory, var owner: Option[Player] = None)
  extends GameObject with Owner with Mass {
  override val mass: Int = 1

  override def score(player: Player): Int = {
    owner match {
      case Some(p) if(p eq player) => 1
      case _ => 0
    }
  }
  override def count(player: Player): Int = score(player)

  def code: Char = owner match {
    case Some(p) => p.player_code
    case None => '?'
  }

  override def toString: String = code.toString
}

