package game.grid

import scala.collection.mutable.Buffer

abstract class GridObject {
  val occupy: Buffer[GridCell]
  val anchor: GridCell
}
