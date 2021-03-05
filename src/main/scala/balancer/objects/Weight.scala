package balancer.objects

import balancer.State

class Weight(val stack: Stack, val state: State, var owner: Option[Player] = None)
  extends GameObject with Owner with Mass {
  override val mass: Int = 1

  override def score(player: Player) = {
    owner match {
      case Some(p) if(p eq player) => 1
      case _ => 0
    }
  }
  override def count(player: Player) = score(player)

  def code: Char = owner match {
    case Some(p) => p.playerCode
    case None => '?'
  }

  override def toString: String = code.toString
}

