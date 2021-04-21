package balancer.objects

import balancer.State
import balancer.grid.Coord

import scala.collection.mutable.ArrayBuffer

/**
 * sealed Container trait only have the two class Scale and Stack:
 * they can store weights, have owners, mass and graphical
 * property such as height and coords. They also have position(pos)
 * relative to their parent scale, similar to Weight.
 */
sealed trait Container
  extends GameObject with Owner with Mass with Renderable {

  /**
   * The position on the parent scale of this Container
   *
   * Where negative pos means on the left arm and positive pos means
   * on the right arm
   * E.g:
   * -2 : On the left arm, 2 unit distance away from the center
   * 3 : On the right arm, 3 unit distance away from the center
   */
  val pos: Int
}

/**
 * A scale is a Container that can store other Scale and Stack.
 * Note: Scale can not store Weight individually. A stack will
 * be created when a weight is placed on an empty slot on the scale.
 *
 * @param parentScale the scale which this scale is placed upon
 * @param pos         the position on the parentScale
 * @param radius      the radius of the scale
 * @param code        the scale's code
 * @param state       the game's state
 */
case class Scale(val parentScale: Scale,
                 val pos: Int,
                 val radius: Int,
                 val code: Char,
                 /**
                  * The scale's code is alway lower-cased, compared to
                  * the player's code, which is always upper-cased
                  */
                 protected val state: State)
  extends Container with Iterable[Option[Container]] {

  /**
   * The level of the scale. (recursive)
   */
  val level: Int =
    if (parentScale == null) {
      0
    } else {
      parentScale.level + 1
    }
  /**
   * The board represent the two arm of the scale.
   * The left most position is indexed at 0. Conversely, the
   * right most position is indexed at 2*radius. The center is
   * always None.
   */
  private val board = ArrayBuffer.fill[Option[Container]](2 * radius + 1)(None)

  /**
   * @return A read-only Vector of the board
   */
  def boardVector = board.toVector

  /**
   * @return A read-only Vector of all the scales on the board
   */
  def scalesVector = board.flatMap {
    case Some(s: Scale) => Some(s)
    case _ => None
  }.toVector

  /**
   * @return A read-only Vector of all the stacks on the board
   */
  def openPos = board.zipWithIndex.filter(p => p._1.isEmpty && p._2 != radius).map(_._2 - radius).toVector

  /**
   * The total score of the player up until this scale (recursive)
   *
   * @param player the player
   * @return the player's score
   */
  override def score(player: Player) = {
    var score = 0
    for ((w, i) <- board.zipWithIndex) {
      w match {
        case Some(p) =>
          score += scala.math.abs(i - radius) * p.score(player)
        case None =>
      }
    }
    owner match {
      case Some(p) if p eq player => score *= 2
      case _ =>
    }
    score
  }

  /**
   * The owner of the scale. The scale is captured when one
   * player has more than |radius| weights than the second
   * most player.
   *
   * @return the player that own this scale
   */
  override def owner = {
    val top2 = state.players.map(p => (p, count(p))).sortBy(_._2).takeRight(2)
    if (top2.length > 1 && top2(1)._2 - top2(0)._2 > radius)
      Some(top2(1)._1)
    else
      None
  }

  /**
   * The number of weights belong to the player (recursive)
   *
   * @param player the player
   * @return the player's number of weights
   */
  override def count(player: Player) = board.flatMap {
    case Some(stack: Stack) => Some(stack)
    case _ => None
  }.foldLeft(0)((acc, stack) => acc + stack.count(player))

  /**
   * The scale is balanced if the difference between the left torque
   * and the right torque is less than the radius (i.e assume that
   * a weight is placed at the end of each arm).
   *
   * @return boolean value indicate whether this scale is balanced.
   */
  def isBalanced = scala.math.abs(leftTorque - rightTorque) <= radius

  /**
   * The total torque that all the weight on the left arm.
   *
   * @return the left total torque
   */
  def leftTorque =
    Range(-radius, 0).flatMap(pos => board(pos + radius)).foldLeft(0)(
      (acc, p) => acc + (-p.pos) * p.mass)

  /**
   * The total torque that all the weight on the right arm.
   *
   * @return the right total torque
   */
  def rightTorque =
    Range(1, radius + 1).flatMap(pos => board(pos + radius)).foldLeft(0)(
      (acc, p) => acc + (p.pos) * p.mass)

  // Iterable
  def iterator = board.iterator

  def remove(pos: Int) = {
    board(pos + radius) = None
  }

  def apply(pos: Int) = board(pos + radius)

  def update(pos: Int, p: Option[Container]) = {
    board(pos + radius) = p
  }

  def update(pos: Int, scale: Scale) = {
    board(pos + radius) = Some(scale)
  }

  def update(pos: Int, stack: Stack) = {
    board(pos + radius) = Some(stack)
  }

  /**
   * The total height of the scale.
   *
   * @return the total height
   */
  override def height = upperHeight + lowerHeight

  /**
   * The lower height is the height from the feet of the
   * fulcrum to the board (inclusive)
   *
   * @return the lower height
   */
  def lowerHeight = {
    if (parentScale == null) {
      4
    } else {
      Math.max(
        state.scalesAtLevel(level - 1)
          .map(_.upperHeight).max + 2, 4)
    }
  }

  /**
   * The upperHeight is the height of the highest
   * stack on the scale.
   *
   * @return the upper height
   */
  def upperHeight = stacksVector.map(_.height).maxOption match {
    case Some(i: Int) => i
    case None => 0
  }

  /**
   * @return A read-only Vector of all the stacks on the board
   */
  def stacksVector = board.flatMap {
    case Some(s: Stack) => Some(s)
    case _ => None
  }.toVector

  /**
   * The coordinate of the feet of the scale
   *
   * @return the scale's coordinate
   */
  override def coord =
    if (parentScale == null)
      Coord(0, 0)
    else
      parentScale.coord + Coord(2 * pos, parentScale.lowerHeight)

  /**
   * The coordinate of the center of the board
   *
   * @return
   */
  def boardCenter = coord + Coord(0, lowerHeight - 1)

  /**
   * The x coordinate of the left most position on the scale
   *
   * @return the left-most x
   */
  def minX = coord.x - (2 * radius + 1)

  /**
   * The x coordinate of the right most position on the scale
   *
   * @return the right-most x
   */
  def maxX = coord.x + (2 * radius + 1)

  /**
   * Determine if one scale is overlapping with another scale
   *
   * @param other the other scale
   * @return weather the two scale overlap
   */
  def isOverLapWith(other: Scale) =
    this.level == other.level && this.maxX >= other.minX && this.minX <= other.maxX

  override def toString: String = s"<$code,$mass>"

  override def mass = board.flatten.map(_.mass).sum
}

case class Stack(val parentScale: Scale, val pos: Int, protected val state: State)
  extends Container with Iterable[Weight] {

  private var stack = scala.collection.mutable.Buffer[Weight]()

  /**
   * Read-only vector of all the weights
   *
   * @return the weight vector of the stack
   */
  def weightsVector = stack.toVector

  /**
   * The mass of the stack. i.e the total mass of all
   * its weights
   *
   * @return the mass of the stack
   */
  override def mass: Int = stack.map(_.mass).sum

  /**
   * The score of a player in this stack
   *
   * @param player the player with score to be calculated
   * @return the score of the player
   */
  override def score(player: Player) = stack.map(_.score(player)).sum

  /**
   * Count the number of weights belong to a player
   *
   * @param player the player
   * @return the number of weights belong to the player
   */
  override def count(player: Player) = stack.map(_.score(player)).sum

  /**
   * The owner of the stack. The owner of a stack is the player
   * placed the top-most weight.
   *
   * @return the owner
   */
  override def owner = {
    stack.last.owner
  }

  /**
   * This function is called when a new weight is placed on top
   * of the stack and all the weights underneath will have their
   * owner adjusted accordingly
   *
   * @return a copy of the old stack owners before the adjustment.
   */
  def updateOwner() = {
    val prevOwnerStack = stack.map(_.owner).clone().toArray

    stack.last.owner match {
      case Some(newOwner: Player) =>

        /**
         * If the scale is own by someone else, his/her weights is
         * "resistant" and will not be captured, theirs owner
         * will not need to be updated.
         */
        parentScale.owner match {
          case Some(scaleOwner: Player) =>
            stack.foreach(weight => {
              if (weight.owner != Some(scaleOwner)) {
                weight.owner = Some(newOwner)
              }
            })
          case None => stack.foreach(_.owner = Some(newOwner))
        }
      case None =>
    }
    prevOwnerStack
  }

  // Iterable methods
  def iterator = stack.iterator

  def apply(pos: Int) = stack(pos)

  def update(pos: Int, weight: Weight) = {
    stack(pos) = weight
  }

  /**
   * Note that the stack is "reversed", i.e appending and popping is
   * done at the end, rather than the beginning like a normal stack.
   */
  def pop() = {
    val last = stack.last
    stack.dropRightInPlace(1)
    last
  }

  def append(it: Weight) = stack.append(it)

  /**
   * Remove all weights belong to a player
   *
   * @param player the player
   */
  def removePlayer(player: Player) = {
    stack = stack.filterNot(_.owner == Some(player))
  }

  override def height = stack.length

  /**
   * The coordinate of the bottom-most weight in the stack
   * @return
   */
  override def coord = parentScale.coord + Coord(2 * pos, parentScale.lowerHeight)

  override def toString: String = "|" + stack.mkString(",") + "|"
}