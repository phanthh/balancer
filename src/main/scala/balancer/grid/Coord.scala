package balancer.grid


/**
 * A basic 2D vector storing x and y coordinates
 * @param x the x coordinate
 * @param y the y coordinate
 */
class Coord(val x: Int, val y: Int) {
  /**
   * Adding two vector together
   * @param other the other vector
   * @return a new coord object
   */
  def +(other: Coord) = Coord(x+other.x, y+other.y)

  /**
   * Substracting one vector by another
   * @param other the other vector
   * @return a new coord object
   */
  def -(other: Coord) = Coord(x-other.x, y-other.y)

  override def toString: String = s"($x,$y)"
}

/**
 * Companion object for easier creation of new Coord objects
 */
object Coord {
  def apply(x: Int, y: Int): Coord = new Coord(x, y)
}

