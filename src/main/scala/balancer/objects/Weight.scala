package balancer.objects

import balancer.State
import balancer.grid.Grid.WILD

/**
 * A weight represent an object with a mass and an owner.
 *
 * @param stack the stack its belong to. All weight must be in a stack
 * @param state the game's state
 * @param owner the default owner, could be None
 */
class Weight(val stack: Stack, protected val state: State, var owner: Option[Player] = None)
  extends GameObject with Owner with Mass {
  override val mass: Int = 1

  /**
   * For weights, the score is the same as the count.
   * i.e the weight count as 1 for the owner and 0 for others.
   *
   * @return
   */
  override def count(player: Player) = score(player)

  /**
   * The weight give 1 score for the owner and 0 others
   */
  override def score(player: Player) = {
    owner match {
      case Some(p) if (p eq player) => 1
      case _ => 0
    }
  }

  override def toString: String = code.toString

  /**
   * The weight code is its owner code. if the weight has no
   * owner, return the WILD code ('?').
   *
   * @return
   */
  def code: Char = owner match {
    case Some(p) => p.playerCode
    case None => WILD
  }
}

