package game.objects

import game.State
import game.grid.Coord

sealed trait Placable
  extends GameObject with Owner with Mass with Renderable {
  val pos: Int
}

case class Scale(val parent_scale: Scale, val pos: Int, val radius: Int, val scale_code: Char,
                 protected val state: State)
  extends Placable with Iterable[Option[Placable]]{

  private var board = Array.fill[Option[Placable]](2*radius+1)(None)

  def boardVector = board.toVector

  def scalesVector = board.flatMap {
    case Some(s: Scale) => Some(s)
    case _ => None
  }.toVector

  def stacksVector = board.flatMap {
    case Some(s: Stack) => Some(s)
    case _ => None
  }.toVector


  override def mass = board.flatten.map(_.mass).sum

  override def score(player: Player) = {
    var score = 0
    for((w, i) <- board.zipWithIndex) {
      w match {
        case Some(p) =>
          score += scala.math.abs(i-radius)*p.score(player)
        case None =>
      }
    }
    owner match {
      case Some(p) if p eq player => score *= 2
      case _ =>
    }
    score
  }

  override def count(player: Player) = {
    var count = 0
    for((w, i) <- board.zipWithIndex) {
      w match {
        case Some(p) =>
          count += p.count(player)
        case None =>
      }
    }
    count
  }

  override def owner: Option[Player] = {
    val top2 = state.players.map(p => (p, count(p))).sortBy(_._2).takeRight(2)
    if(top2(1)._2 - top2(0)._2 > radius) Some(top2(1)._1) else None
  }

  def leftTorque = {
    var torque = 0
    for(pos <- -radius to -1) {
      board(pos+radius) match {
        case Some(p) => torque += scala.math.abs(pos)*p.mass
        case None =>
      }
    }
    torque
  }

  def rightTorque = {
    var torque = 0
    for(pos <- 1 to radius) {
      board(pos+radius) match {
        case Some(p) => torque += pos*p.mass
        case None =>
      }
    }
    torque
  }

  def isBalanced: Boolean = scala.math.abs(leftTorque-rightTorque) <= radius

  // ITERATOR
  def iterator: Iterator[Option[Placable]] = board.iterator
  def remove(pos: Int) = { board(pos+radius) = None }
  def apply(pos: Int) = board(pos+radius)
  def update(pos: Int, p: Option[Placable]) = { board(pos+radius) = p }
  def update(pos: Int, scale: Scale) = { board(pos+radius) = Some(scale) }
  def update(pos: Int, stack: Stack) = { board(pos+radius) = Some(stack) }

  // RENDERING

  def uHeight = stacksVector.map(_.height).maxOption match {
    case Some(i: Int) => i
    case None => 0
  }

  def lHeight = if(parent_scale == null) 4 else scala.math.max(parent_scale.uHeight + 2, 4)
  override def height = uHeight + lHeight

  override def coord: Coord =
    if(parent_scale == null)
      Coord(0,0)
    else
      parent_scale.coord + Coord(2*pos, parent_scale.lHeight)


  def span = (coord - Coord(2*radius+1,0), coord + Coord(2*radius+1,0))

  override def toString: String = s"<$scale_code,$mass>"
}

case class Stack(val parentScale: Scale, val pos: Int, protected val state: State)
  extends Placable with Iterable[Weight]{

  private var stack = scala.collection.mutable.Stack[Weight]()

  def weightsVector = stack.toVector

  override def mass: Int = stack.map(_.mass).sum

  override def score(player: Player): Int = stack.map(_.score(player)).sum

  override def count(player: Player): Int = stack.map(_.score(player)).sum

  override def owner: Option[Player] = { stack.last.owner }

  def updateOwner() = {
    val prevOwnerStack = stack.map(_.owner).clone().toArray
    stack.last.owner match {
      case Some(newOwner: Player) =>
        parentScale.owner match {
          case Some(scaleOwner: Player) =>
            stack.foreach(weight =>
              if(weight.owner != Some(scaleOwner))
                weight.owner = Some(newOwner)
            )
          case None => stack.foreach(_.owner = Some(newOwner))
        }
      case None =>
    }
    prevOwnerStack
  }

  // ITERATOR
  def iterator = stack.iterator
  def apply(pos: Int) = stack(pos)
  def update(pos: Int, weight: Weight) = { stack(pos) = weight }
  def pop() = stack.pop()
  def append(it: Weight) = stack.append(it)

  // RENDERING
  override def height: Int = stack.length

  override def coord: Coord = parentScale.coord + Coord(2*pos, parentScale.lHeight)

  override def toString: String = "|" + stack.mkString(",") + "|"
}