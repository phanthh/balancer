package game.grid

class Coord(val x: Int, val y: Int) {
  def +(other: Coord) = Coord(x+other.x, y+other.y)
  def -(other: Coord) = Coord(x-other.x, y-other.y)
  override def toString: String = s"($x,$y)"
}

object Coord {
  def apply(x: Int, y: Int): Coord = new Coord(x, y)
}

