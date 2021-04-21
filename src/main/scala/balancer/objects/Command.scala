package balancer.objects

import balancer.State
import balancer.utils.OccupiedPosition

/**
 * A Command is a player action that affect the game state,
 * The class act as a holder for user's in-game moves (only
 * placing weights for now)
 */
sealed trait Command {
  def execute(): Command
  def undo(): Command
}

object Command {
  def placeWeight(player: Player, pos: Int, parentScale: Scale, state: State) = {
    new PlaceWeightCommand(player, pos, parentScale, state)
  }
}

/**
 * A Command that contains information about the user'move,
 * i.e placing a weight
 * @param player the player did the move
 * @param pos the position on the scale where the weight is placed
 * @param parentScale the scale which the weight is placed upon
 * @param state the game state
 */
case class PlaceWeightCommand(val player: Player, val pos: Int, val parentScale: Scale, val state: State)
  extends Command with GameObject {

  /**
   * A reference to the stack which had the new weight added
   * on top
   */
  private var affectedStack: Stack = _

  /**
   * An array storing the previous owner of each weights on the
   * affectedStack, i.e before the new weight is added and
   * changing their owners.
   */
  private var undoOwnerList: Array[Option[Player]] = _

  /**
   * Execute the command, changing the game's state
   * @return the command itself
   */
  override def execute() = {
    parentScale(pos) match {
      case Some(scale: Scale) => throw new OccupiedPosition
      case _ =>
        affectedStack = state.buildWeight(pos, parentScale, Some(player))
        undoOwnerList = affectedStack.updateOwner()
    }
    this
  }

  /**
   * Undo the command, revert all changes to the game state,
   * @return the command itself
   */
  override def undo() = {
    if (state.scalesVector.contains(affectedStack.parentScale)) {
      affectedStack.pop()
      if (affectedStack.isEmpty) {
        parentScale.remove(pos)
      } else {
        // Restore the owner of each weights on the affectedStack
        affectedStack.zipWithIndex.foreach(p => p._1.owner = undoOwnerList(p._2))
      }
    }
    this
  }
}

