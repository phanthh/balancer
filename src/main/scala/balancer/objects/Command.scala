package balancer.objects

import balancer.State

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

  private var undoStack: Stack = _
  private var undoOwnerList: Array[Option[Player]] = _

  override def execute() = {
    parentScale(pos) match {
      case Some(scale: Scale) => throw new InvalidPosition(pos.toString)
      case _ =>
        undoStack = state.buildWeight(pos, parentScale, Some(player))._2
        undoOwnerList = undoStack.updateOwner()
    }
//    state.update() // TODO: SCALE DISAPPEAR WHEN FLIPPED
    this
  }

  override def undo() = {
    undoStack.pop()
    if(undoStack.isEmpty)
      parentScale.remove(pos)
    else
      undoStack.zipWithIndex.foreach(p => p._1.owner = undoOwnerList(p._2)) // Restore
    this
  }
}

final case class InvalidPosition(private val message: String = "",
                              private val cause: Throwable = None.orNull)
  extends Exception(message, cause)
