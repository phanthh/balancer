package game.objects

import game.State

sealed trait Command {
  def execute(): Command
  def undo(): Command
  def redo(): Command
}

object Command {
  def placeWeight(player: Player, pos: Int, parentScale: Scale, state: State) = {
    new PlaceWeightCommand(player, pos, parentScale, state)
  }
}

case class PlaceWeightCommand(val player: Player, val pos: Int, val parentScale: Scale, val state: State)
  extends Command with GameObject {

  private var affectedStack: Stack = _
  private var prevOwnerList: Array[Option[Player]] = _

  override def execute() = {
    parentScale(pos) match {
      case Some(scale: Scale) => throw new InvalidPosition(pos.toString)
      case _ =>
        affectedStack = state.buildWeight(pos, parentScale, Some(player))._2
        prevOwnerList = affectedStack.updateOwner()
    }
    this
  }

  override def undo() = {
    affectedStack.pop()
    if(affectedStack.isEmpty)
      parentScale.remove(pos)
    else
      affectedStack.zipWithIndex.foreach(p => p._1.owner = prevOwnerList(p._2)) // Restore
    this
  }

  override def redo() = {
    execute()
    this
  }
}

final case class InvalidPosition(private val message: String = "",
                              private val cause: Throwable = None.orNull)
  extends Exception(message, cause)
