package game.objects

import game.{Game, GameObject}

import scala.collection.mutable.Buffer

sealed class Weight(val parent: Option[Weight], id: String, game: Game)
  extends GameObject(id, game){

  private var _owner: Option[Player] = None

  def mass: Int = 1

  def height: Int = 1

  def owner: Option[Player] = _owner

  final def set_owner(player: Option[Player]): Unit = { _owner = player }

  def score_of(player: Player): Int = ???
}

case class Scale(parent_scale: Option[Scale], val radius: Int, _id: String, _game: Game)
  extends Weight(parent_scale, _id, _game) {

  private var board: Array[Weight] = Array.ofDim[Weight](2*radius+1)

  def place_at(pos: Int, it: Weight) = { board(pos+radius) = it }

  def isEmptyAt(pos: Int) = board(pos+radius) != null

  def objectAt(pos: Int) = board(pos+radius)

  override def mass: Int = board.map(_.mass).sum

  override def score_of(player: Player): Int = ???

  override def height: Int = board.map(_.height).max

  override def owner: Option[Player] = {
    val pair = board.map(_.owner).groupBy(identity).view.mapValues(_.length).maxBy(_._2)
    val owner = pair._1
    val num_weight_owned = pair._2
    if(num_weight_owned >= radius) owner else None
  }

  def isBalanced: Boolean = ???

  def isBuffed: Boolean = owner.isDefined
}

case class Stack(parent_scale: Scale, val bottom_weight: Weight, _id: String, _game: Game)
  extends Weight(Some(parent_scale), _id, _game) {
  private var stack: Buffer[Weight] = Buffer[Weight](bottom_weight)

  override def mass: Int = stack.map(_.mass).sum

  override def score_of(player: Player): Int = ???

  override def height: Int = stack.length

  override def owner: Option[Player] =
    stack.map(_.owner).groupBy(identity).view.mapValues(_.size).maxBy(_._2)._1

  def append(it: Weight) = stack.append(it)
}

/*TODO: Add factory methods/companion objects to each objects*/
