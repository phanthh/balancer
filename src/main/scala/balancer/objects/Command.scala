package balancer.objects

import balancer.State
import balancer.utils.OccupiedPosition

sealed trait Command {
  def execute(): Command
  def undo(): Command
}

object Command {
  def placeWeight(player: Player, pos: Int, parentScale: Scale, state: State) = {
    new PlaceWeightCommand(player, pos, parentScale, state)
  }
}

case class PlaceWeightCommand(val player: Player, val pos: Int, val parentScale: Scale, val state: State)
  extends Command with GameObject {

  private var affectedStack: Stack = _
  private var undoOwnerList: Array[Option[Player]] = _

  override def execute() = {
    parentScale(pos) match {
      case Some(scale: Scale) => throw new OccupiedPosition
      case _ =>
        affectedStack = state.buildWeight(pos, parentScale, Some(player))
        undoOwnerList = affectedStack.updateOwner()
    }
    this
  }

  override def undo() = {
    affectedStack.pop()
    if(affectedStack.isEmpty)
      parentScale.remove(pos)
    else
      affectedStack.zipWithIndex.foreach(p => p._1.owner = undoOwnerList(p._2)) // Restore
    this
  }
}
