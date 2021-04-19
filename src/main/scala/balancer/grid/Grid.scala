package balancer.grid

import balancer.Game
import balancer.grid.Grid.{EMPTY, FULCRUM, GROUND, LEFT, PADDER, RIGHT, WILD}
import balancer.objects.{Player, Scale}

/**
 * An enum object, storing possible value/representation
 * of each cell on the grid.
 */
object Grid {
  val PADDER = '='
  val FULCRUM = '*'
  val LEFT = '<'
  val RIGHT = '>'
  val WILD = '?'
  val GROUND = 'X'
  val EMPTY = ' '
}

/**
 * The game "screen" will be represented by a m x n grid,
 * each entry store a Char denoting what should be
 * rendered/displayed at that position.
 *
 * @param game ref to the game object
 */
class Grid(private val game: Game){
  private var _width: Int = _
  private var _height: Int = _
  private var _minX: Int = _
  private var _maxX: Int = _
  private var _grid: Array[Array[Char]] = _
  private def state = game.state

  /**
   * The width and height of the grid will be calculate to fit as tightly
   * as possible to the scales. _padding determines how much cell will be
   * padded
   */
  private var _padding: Int = 3

  private var _groundHeight: Int = 1

  /**
   * Read-only getter for width and height, padding
   * @return
   */
  def width = _width
  def height = _height
  def padding = _padding
  def groundHeight = _groundHeight

  /**
   * Dynamically adjust the width, height, the farthest to the right (left)
   * of all the scales _maxX (_minX)
   */
  def updateOffset() = {
    _height = state.scalesVector.map(s => s.coord.y + s.height).max + _padding
    _maxX = state.scalesVector.map(_.span._2.x).max + _padding
    _minX = state.scalesVector.map(_.span._1.x).min - _padding
    _width = _maxX - _minX

    if(_width % 2 == 0) _width += 1

    // Initialized all entry to EMPTY
    _height += _groundHeight
    _grid = Array.tabulate(_height, _width)((_,_) => EMPTY)
    for(i <- _height-_groundHeight until _height){
      Range(0, _width).foreach(_grid(i)(_) = GROUND)
    }
  }


  updateOffset()

  /**
   * Take values from the game state and representing them on the grid
   */
  def updateGrid() = state.scalesVector.foreach(register)

  def update() = { updateOffset(); updateGrid() }

  /**
   * Access and set value on the grid at a particular cooridinate or directly
   * (ith row and jth column)
   *
   * Noted: The Coord object assume the origin is at the "foot" of the
   * bottom-most scale (the basic scale) while the grid(i)(j) assume the
   * origin at the top left of the grid.
   * @param coord the coordinate
   * @return the character represent the grid cell
   */
  def apply(coord: Coord) = _grid(_height-coord.y-1)(coord.x-_minX)
  def apply(i: Int, j: Int) = _grid(i)(j)
  def put(coord: Coord, value: Char) = _grid(_height-coord.y-1)(coord.x-_minX) = value
  def put(i: Int, j: Int, value: Char) = _grid(i)(j) = value

  /**
   * @param coord the Coord object
   * @return a pair (i,j) (ith row and jth column of the grid)
   */
  def coordToGrid(coord: Coord): (Int, Int) = (_height-coord.y-1, coord.x-_minX)

  /**
   * @param i ith column of the grid
   * @param j jth row of the grid
   * @return
   */
  def gridToCoord(i: Int, j: Int) = Coord(j+_minX, _height-1-i)

  /**
   * "putting" the scale and its weights on the grid by setting each cell
   * values appropriately.
   * @param scale the scale to be "rendered" onto the grid
   */
  private def register(scale: Scale) = {
    // Rendering the fulcrum
    val fulcrumHeight = scale.lHeight - 1
    for(i <- 0 until fulcrumHeight-1){
      put(scale.coord + Coord(0, i), FULCRUM)
    }
    put(scale.coord + Coord(0, fulcrumHeight-1), scale.code)

    // Rendering board (where all the weights are placed upon)
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

    // Render stacks of weights on the scale
    for(stack <- scale.stacksVector){
      for(i <- 0 until stack.height){
        put(stack.coord + Coord(0, i), stack(i).code)
      }
    }
  }

}
