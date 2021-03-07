package balancer.objects

import balancer.State
import balancer.grid.Coord

sealed trait Placable
  extends GameObject with Owner with Mass with Renderable {
  val pos: Int
}

case class Scale(val parentScale: Scale, val pos: Int, val radius: Int, val code: Char,
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

  override def count(player: Player) = board.flatMap{
    case Some(stack: Stack) => Some(stack)
    case _ => None
  }.foldLeft(0)((acc, stack) => acc + stack.count(player))

  override def owner = {
    val top2 = state.players.map(p => (p, count(p))).sortBy(_._2).takeRight(2)
    if(top2.length == 1)
      None
    else if(top2(1)._2 - top2(0)._2 > radius)
      Some(top2(1)._1)
    else
      None
  }

  def leftTorque = {
    var torque = 0
    for(pos <- -radius to -1) {
      board(pos+radius) match {
        case Some(p) => torque += (-pos)*p.mass
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

  def isBalanced = scala.math.abs(leftTorque-rightTorque) <= radius

  // ITERATOR
  def iterator = board.iterator
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

  def lHeight = if(parentScale == null) 4 else scala.math.max(parentScale.uHeight + 2, 4)
  override def height = uHeight + lHeight

  override def coord =
    if(parentScale == null)
      Coord(0,0)
    else
      parentScale.coord + Coord(2*pos, parentScale.lHeight)

  def boardCenter = coord + Coord(0, lHeight-1)

  def span = (coord - Coord(2*radius+1,0), coord + Coord(2*radius+1,0))

  override def toString: String = s"<$code,$mass>"
}

case class Stack(val parentScale: Scale, val pos: Int, protected val state: State)
  extends Placable with Iterable[Weight]{

  private var stack = scala.collection.mutable.Buffer[Weight]()

  def weightsVector = stack.toVector

  override def mass: Int = stack.map(_.mass).sum

  override def score(player: Player) = stack.map(_.score(player)).sum

  override def count(player: Player) = stack.map(_.score(player)).sum

  override def owner = { stack.last.owner }

  def updateOwner() = {
    val prevOwnerStack = stack.map(_.owner).clone().toArray
    owner match {
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
  def pop() = {
    val last = stack.last
    stack.dropRightInPlace(1)
    last
  }
  def append(it: Weight) = stack.append(it)
  def filterOut(player: Player) = {
    stack = stack.filterNot(_.owner == Some(player))
  }

  // RENDERING
  override def height = stack.length

  override def coord = parentScale.coord + Coord(2*pos, parentScale.lHeight)

  override def toString: String = "|" + stack.mkString(",") + "|"
}