package game.grid

import game.Game
import game.grid.Grid.{EMPTY, FULCRUM, LEFT, PADDER, RIGHT}
import game.objects.{Player, Scale}

object Grid {
  val PADDER = '='
  val FULCRUM = '*'
  val LEFT = '<'
  val RIGHT = '>'
  val WILD = '?'
  val GROUND = 'X'
  val EMPTY = ' '

  def apply(game: Game) = {
    val grid = new Grid(game)
    grid.updateOffset()
    grid
  }
}

private class Grid(private val game: Game){
  private var _width: Int = _
  private var _height: Int = _
  private var _minX: Int = _
  private var _maxX: Int = _
  private var _grid: Array[Array[Char]] = _

  def width = _width
  def height = _height

  def updateOffset() = {
    _height = game.scales.map(s => s.coord.y + s.height).max
    _maxX = game.scales.map(_.span._2.x).max
    _minX = game.scales.map(_.span._1.x).min
    _width = _maxX - _minX
    if(_width % 2 == 0) _width += 1
    _grid = Array.tabulate(_height, _width)((_,_) => EMPTY)
  }

  def updateGrid() = _register(game.baseScale)

  def update() = { updateOffset(); updateGrid() }

  def at(coord: Coord) = _grid(_height-coord.y-1)(coord.x-_minX)

  def apply(i: Int, j: Int) = _grid(i)(j)

  def put(coord: Coord, marker: Char) =
    _grid(_height-coord.y-1)(coord.x-_minX) = marker

  def _register(scale: Scale): Unit = {
    // Register the scale and it child stacksVector
    // Rendering fulcrum
    val fulcrum_height = scale.lHeight - 1
    for(i <- 0 until fulcrum_height-1){
      put(scale.coord + Coord(0, i), FULCRUM)
    }
    put(scale.coord + Coord(0, fulcrum_height-1), scale.scale_code)

    // Render board
    val board_center = scale.coord + Coord(0, fulcrum_height)
    put(board_center, scale.owner match {
      case Some(p: Player) => p.player_code.toUpper
      case None => '?'
    })
    for(i <- 1 to scale.radius){
      put(board_center + Coord(2*i-1, 0), PADDER)
      put(board_center + Coord(2*i, 0), i.toString.charAt(0))

      put(board_center + Coord(-2*i+1, 0), PADDER)
      put(board_center + Coord(-2*i, 0), i.toString.charAt(0))
    }

    put(board_center + Coord(2*scale.radius+1,0), RIGHT)
    put(board_center + Coord(-2*scale.radius-1,0), LEFT)

    // Render stack on the scale
    for(stack <- scale.stacksVector){
      for(i <- 0 until stack.height){
        put(stack.coord + Coord(0, i), stack.at(i).code)
      }
    }

    // Recursive
    scale.scalesVector.foreach(_register)
  }
}
