package game.objects

import game.{Game, GameObject}

import scala.collection.mutable.Buffer

sealed abstract class Placable(id: String, game: Game)
  extends GameObject(id, game) with Owner with Mass with Scorable with Height {

  val pos: Int
}

case class Scale(val scale: Scale, val pos: Int, val radius: Int, val scale_code: Char, _id: String, _game: Game)
  extends Placable(_id, _game) {

  private var board = Array.fill[Option[Placable]](2*radius+1)(None)

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
    val pair = board.flatten.map(_.owner).groupBy(identity).view.mapValues(_.length).maxBy(_._2)
    val owner = pair._1
    val num_weight_owned = pair._2
    if(num_weight_owned >= radius) owner else None
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
        case Some(p) => torque += p.mass
        case None =>
      }
    }
    torque
  }

  def isBalanced: Boolean = scala.math.abs(left_torque-right_torque) <= radius

  def isBuffed: Boolean = owner.isDefined

  // RENDERING

  private var fulcrum_height = 0

  override def height = board.flatten.map(_.height).max + fulcrum_height + 1

  override def toString: String = {
    "[" + board.map {
      case Some(s: Scale) => "s"
      case Some(t: Stack) => "t"
      case None => "="
    }.mkString + s"]$isBalanced\n"
  }
}

case class Stack(val scale: Scale, val pos: Int, _id: String, _game: Game, bottom_weight: Option[Weight] = None)
  extends Placable(_id, _game) {

  private var stack: Buffer[Weight] = Buffer[Weight]()
  bottom_weight match {
    case Some(w) => stack.append(w)
    case None =>
  }

  override def mass: Int = stack.map(_.mass).sum

  override def score_of(player: Player): Int = stack.map(_.score_of(player)).sum

  override def height: Int = stack.length

  override def owner: Option[Player] =
    stack.map(_.owner).groupBy(identity).view.mapValues(_.size).maxBy(_._2)._1

  def append(it: Weight) = stack.append(it)
}