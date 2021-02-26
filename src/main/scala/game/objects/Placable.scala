package game.objects

import game.Game

sealed abstract class Placable
  extends GameObject with Owner with Mass with Scorable with Height {
  val pos: Int
}

case class Scale(val scale: Scale, val pos: Int, val radius: Int, val scale_code: Char,
                 protected val game: Game)
  extends Placable {

  private var board = Array.fill[Option[Placable]](2*radius+1)(None)

  def board_vec = board.toVector

  def place_at(pos: Int, it: Placable) = { board(pos+radius) = Some(it)}

  def is_empty_at(pos: Int) = board(pos+radius).isEmpty

  def object_at(pos: Int) = board(pos+radius)

  override def mass = board.flatten.map(_.mass).sum

  override def score_of(player: Player): Int = {
    var count = 0
    for((w, i) <- board.zipWithIndex) {
      w match {
        case Some(p) =>
          count += scala.math.abs(i-radius)*p.score_of(player)
        case None =>
      }
    }
    count
  }

  override def owner: Option[Player] = {
    if(board.forall(_.isEmpty)) None
    else {
      val pair = board.flatten.map(_.owner).groupBy(identity).view.mapValues(_.length).maxBy(_._2)
      val owner = pair._1
      val num_weight_owned = pair._2
      if(num_weight_owned >= radius) owner else None
    }
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

  def scales: Vector[Scale] = board.flatMap {
    case Some(s: Scale) => Some(s)
    case _ => None
  }.toVector

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

case class Stack(val scale: Scale, val pos: Int, protected val game: Game)
  extends Placable {

  private var stack = scala.collection.mutable.Stack[Weight]()

  override def mass: Int = stack.map(_.mass).sum

  override def score_of(player: Player): Int = stack.map(_.score_of(player)).sum

  override def height: Int = stack.length

  override def owner: Option[Player] = {
    if(stack.isEmpty) None else {
      stack.map(_.owner).groupBy(identity).view.mapValues(_.size).maxBy(_._2)._1
    }
  }

  def updateWeight() = {

  }

  def append(it: Weight) = {
    it.owner match {
      case Some(o: Player) =>
        scale.owner match {
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