package game.grid

class Grid(val width: Int, val height: Int){
  private val grid: Array[Array[GridCell]] = Array.ofDim[GridCell](height, width)

  for(i <- 0 until height; j <- 0 until width){
    grid(i)(j) = new GridCell(j,i)
  }

  def object_at(gridCell: GridCell): Option[GridObject] = gridCell.gridObject

  def cell_at(i: Int, j: Int): GridCell = grid(i)(j)
}
