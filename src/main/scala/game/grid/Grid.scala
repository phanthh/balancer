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
    grid.update_offset()
    grid
  }
}

class Grid(private val game: Game){
  private var _width = 0
  private var _height = 0
  private var _minX = 0
  private var _maxX = 0
  private var _grid: Array[Array[Char]] = Array.empty[Array[Char]]

  def width = _width
  def height = _height

  def update_offset() = {
    _height = game.scales.map(s => s.coord.y + s.height).max
    _maxX = game.scales.map(_.span._2.x).max
    _minX = game.scales.map(_.span._1.x).min
    _width = _maxX - _minX
    if(_width % 2 == 0) _width += 1
    _grid = Array.tabulate(_height, _width)((_,_) => EMPTY)
  }

  def update_grid() = _register(game.baseScale)

  def update() = { update_offset(); update_grid() }

  def at(coord: Coord) = _grid(_height-coord.y-1)(coord.x-_minX)

  def apply(i: Int, j: Int) = _grid(i)(j)

  def put(coord: Coord, marker: Char) =
    _grid(_height-coord.y-1)(coord.x-_minX) = marker

  def _register(scale: Scale): Unit = {
    // Register the scale and it child stacks
    // Rendering fulcrum
    val fulcrum_height = scale.lo_height - 1
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
    for(stack <- scale.stacks){
      for(i <- 0 until stack.height){
        put(stack.coord + Coord(0, i), stack.at(i).code)
      }
    }

    // Recursive
    scale.scales.foreach(_register)
  }
}
