package game.gui

import game.Game
import game.grid.Coord
import game.grid.Grid.{EMPTY, LEFT}
import game.gui.Constants.{CellHeight, CellWidth, Height, Width}
import game.gui.MainGUI.{draw, gameLoopLogic, grid, setup}
import game.objects.Command.placeWeight
import game.objects.{Scale, Stack}
import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.SplitPane
import scalafx.scene.input.MouseButton

class MidSplitPlane(val game: Game) extends SplitPane {

  def state = game.state
  val canvas = new Canvas(Width, Height)
  val gc = canvas.graphicsContext2D
  val infoPane = new InfoPane(gc, game)
  var i = 0
  var j = 0
  setup(gc)
  draw(gc)

  val scrollPane = new ZoomableScrollPane(canvas)
  canvas.onMouseClicked = e => {
    // DISABLE ACTION WHEN GAME IS OVER
    if(!(game.over)){
      if(MouseButton(e.getButton) == MouseButton.Primary){
        i = (e.getY/CellHeight).toInt
        j = (e.getX/CellWidth).toInt
        executeTurn()
      }
    }
  }

  items.addAll(scrollPane, infoPane)
  setDividerPosition(0, 0.75)

  def executeTurn(): Unit = {
    val chosenCoord = grid.gridToCoord(i, j)
    var chosenScale: Option[Scale] = None
    var chosenPos = 0

    def findScale(): Unit = {
      for (scale <- state.scales) {
        val offset = chosenCoord - scale.boardCenter
        if (offset.x >= -scale.radius * 2 && offset.x <= scale.radius * 2) {
          if (offset.x % 2 == 0 && offset.x != 0) {
            val pos = offset.x / 2
            val height = offset.y
            scale(pos) match {
              case Some(stack: Stack) =>{
                if (height >= 0 && height <= stack.height + 1) {
                  chosenScale = Some(scale)
                  chosenPos = pos
                  return
                }
              }
              case None => {
                if (height == 0 || height == 1) {
                  chosenScale = Some(scale)
                  chosenPos = pos
                  return
                }
              }
              case Some(scale: Scale) =>
            }
          }
        }
      }
    }
    findScale()
    chosenScale match {
      case Some(scale: Scale) =>
        state.undoStack.append(
          placeWeight(
            state.currentTurn,
            chosenPos,
            scale,
            state
          ).execute()
        )
        gameLoopLogic()
        infoPane.updateProperty()
        draw(gc)
      case None =>
    }
  }
}

