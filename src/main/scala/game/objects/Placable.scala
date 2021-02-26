package game.objects

import game.Factory
import scala.collection.mutable.Buffer

sealed abstract class Placable
  extends GameObject with Owner with Mass with Height {
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

  def place_at(pos: Int, it: Placable) = { board(pos+radius) = Some(it)}

  def remove_at(pos: Int) = { board(pos+radius) = None }

  def is_empty_at(pos: Int) = board(pos+radius).isEmpty

  def object_at(pos: Int) = board(pos+radius)

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

  def scaleWithCode(code: Char): Option[Scale] =
    if(code == scale_code)
      Some(this)
    else
      scales.find(_.scale_code == code)


  // RENDERING
  private var fulcrum_height = 0

  override def height = board.flatten.map(_.height).max + fulcrum_height + 1

  override def toString: String = s"<$scale_code,$mass>"
}

case class Stack(val parent_scale: Scale, val pos: Int, protected val factory: Factory)
  extends Placable {

  private var stack = Buffer[Weight]()

  def stack_vec = stack.toVector

  override def mass: Int = stack.map(_.mass).sum

  override def score(player: Player): Int = stack.map(_.score(player)).sum

  override def count(player: Player): Int = stack.map(_.score(player)).sum

  override def height: Int = stack.length

  override def owner: Option[Player] = {
    if(stack.isEmpty) None else {
      stack.map(_.owner).groupBy(identity).view.mapValues(_.size).maxBy(_._2)._1
    }
  }

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

  override def toString: String = "|" + stack.mkString(",") + "|"
}