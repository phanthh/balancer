package balancer.grid

import balancer.Game
import balancer.grid.Grid.{EMPTY, FULCRUM, LEFT, PADDER, RIGHT, WILD}
import balancer.objects.{Player, Scale}

object Grid {
  val PADDER = '='
  val FULCRUM = '*'
  val LEFT = '<'
  val RIGHT = '>'
  val WILD = '?'
  val GROUND = 'X'
  val EMPTY = ' '
}

class Grid(private val game: Game){
  private var _width: Int = _
  private var _height: Int = _
  private var _minX: Int = _
  private var _maxX: Int = _
  private var _grid: Array[Array[Char]] = _
  private def state = game.state

  private var _padding = 3

  def width = _width
  def height = _height

  updateOffset()

  def updateOffset() = {
    _height = state.scales.map(s => s.coord.y + s.height).max + _padding
    _maxX = state.scales.map(_.span._2.x).max + _padding
    _minX = state.scales.map(_.span._1.x).min - _padding
    _width = _maxX - _minX
    if(_width % 2 == 0) _width += 1
    _grid = Array.tabulate(_height, _width)((_,_) => EMPTY)
  }

  def updateGrid() = _register(state.baseScale)

  def update() = { updateOffset(); updateGrid() }

  def apply(coord: Coord) = _grid(_height-coord.y-1)(coord.x-_minX)

  def apply(i: Int, j: Int) = _grid(i)(j)

  def put(coord: Coord, marker: Char) =
    _grid(_height-coord.y-1)(coord.x-_minX) = marker

  def put(i: Int, j: Int, marker: Char) =
    _grid(i)(j) = marker

  def _register(scale: Scale): Unit = {
    // Register the scale and it child stacksVector
    // Rendering fulcrum
    val fulcrumHeight = scale.lHeight - 1
    for(i <- 0 until fulcrumHeight-1){
      put(scale.coord + Coord(0, i), FULCRUM)
    }
    put(scale.coord + Coord(0, fulcrumHeight-1), scale.code)

    // Render board
    val boardCenter = scale.boardCenter
    put(boardCenter, scale.owner match {
      case Some(p: Player) => p.playerCode
      case None => WILD
    })
    for(i <- 1 to scale.radius){
      put(boardCenter + Coord(2*i-1, 0), PADDER)
      put(boardCenter + Coord(2*i, 0), i.toString.charAt(0))

      put(boardCenter + Coord(-2*i+1, 0), PADDER)
      put(boardCenter + Coord(-2*i, 0), i.toString.charAt(0))
    }

    put(boardCenter + Coord(2*scale.radius+1,0), RIGHT)
    put(boardCenter + Coord(-2*scale.radius-1,0), LEFT)

    // Render stack on the scale
    for(stack <- scale.stacksVector){
      for(i <- 0 until stack.height){
        put(stack.coord + Coord(0, i), stack(i).code)
      }
    }

    // Recursive
    scale.scalesVector.foreach(_register)
  }

  def coordToGrid(coord: Coord): (Int, Int) = (_height-coord.y-1, coord.x-_minX)

  def gridToCoord(i: Int, j: Int) = Coord(j+_minX, _height-1-i)

}
