package game.objects

import game.Factory
import game.grid.Coord

import scala.collection.mutable.Buffer

sealed abstract class Placable
  extends GameObject with Owner with Mass with Renderable {
  val pos: Int
}

case class Scale(val parent_scale: Scale, val pos: Int, val radius: Int, val scale_code: Char,
                 protected val factory: Factory)
  extends Placable {

  private var board = Array.fill[Option[Placable]](2*radius+1)(None)

  def board_vec = board.toVector

  def scales = board.flatMap {
    case Some(s: Scale) => Some(s)
    case _ => None
  }.toVector

  def stacks = board.flatMap {
    case Some(s: Stack) => Some(s)
    case _ => None
  }.toVector

  def put(pos: Int, it: Placable) = { board(pos+radius) = Some(it)}

  def remove_at(pos: Int) = { board(pos+radius) = None }

  def is_empty_at(pos: Int) = board(pos+radius).isEmpty

  def at(pos: Int) = board(pos+radius)

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
    // TODO: Inefficient since multiple call of count and count is recursive
    val top2 = factory.players.sortBy(count).takeRight(2)
    if(count(top2(1)) - count(top2(0)) > radius) Some(top2(1)) else None
  }

  def left_torque = {
    var torque = 0
    for(pos <- -radius to -1) {
      board(pos+radius) match {
        case Some(p) => torque += scala.math.abs(pos)*p.mass
        case None =>
      }
    }
    torque
  }

  def right_torque = {
    var torque = 0
    for(pos <- 1 to radius) {
      board(pos+radius) match {
        case Some(p) => torque += pos*p.mass
        case None =>
      }
    }
    torque
  }

  def isBalanced: Boolean = scala.math.abs(left_torque-right_torque) <= radius

  // RENDERING

  def up_height = stacks.map(_.height).maxOption match {
    case Some(i: Int) => i
    case None => 0
  }

  def lo_height = if(parent_scale == null) 4 else scala.math.max(parent_scale.up_height + 2, 4)
  override def height = up_height + lo_height

  override def coord: Coord =
    if(parent_scale == null)
      Coord(0,0)
    else
      parent_scale.coord + Coord(2*pos, parent_scale.lo_height)


  def span = (coord - Coord(2*radius+1,0), coord + Coord(2*radius+1,0))

  override def toString: String = s"<$scale_code,$mass>"
}

case class Stack(val parent_scale: Scale, val pos: Int, protected val factory: Factory)
  extends Placable {

  private var stack = Buffer[Weight]()

  def stack_vec = stack.toVector

  override def mass: Int = stack.map(_.mass).sum

  override def score(player: Player): Int = stack.map(_.score(player)).sum

  override def count(player: Player): Int = stack.map(_.score(player)).sum

  override def owner: Option[Player] = { stack.last.owner }
//    if(stack.isEmpty) None else {
//      stack.map(_.owner).groupBy(identity).view.mapValues(_.size).maxBy(_._2)._1
//    }

  def at(idx: Int) = stack(idx)

  def soft_append(it: Weight) = stack.append(it)
  def soft_pop() = stack.dropRightInPlace(1)

  def append(it: Weight) = {
    it.owner match {
      case Some(o: Player) =>
        parent_scale.owner match {
          case Some(p: Player) => stack.foreach(w =>
            if(w.owner != Some(p)) w.owner = Some(o)
          )
          case None => stack.foreach(_.owner = Some(o))
        }
      case None =>
    }
    stack.append(it)
  }

  // RENDERING
  override def height: Int = stack.length

  override def coord: Coord = parent_scale.coord + Coord(2*pos, parent_scale.lo_height)

  override def toString: String = "|" + stack.mkString(",") + "|"
}