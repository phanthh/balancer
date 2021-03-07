package balancer.gui

import balancer.Game
import balancer.gui.Constants.{CellHeight, CellWidth, Height, Width}
import balancer.gui.MainGUI._
import balancer.objects.Command.placeWeight
import balancer.objects.{Scale, Stack}
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.SplitPane
import scalafx.scene.input.MouseButton

class MidSplitPane(private val game: Game) extends SplitPane {
  def state = game.state

  val canvas = new Canvas(Width, Height)
  val gc = canvas.graphicsContext2D
  var infoPane = new InfoPane(game)
  var clickedIPos = 0
  var clickedJPos = 0

  val scrollPane = new ZoomableScrollPane(canvas)

  items.addAll(scrollPane, infoPane)
  setDividerPosition(0, 0.75)

  // WHEN CLICK ON GRID TO PLACE WEIGHT
  canvas.onMouseClicked = e => {
    // DISABLE ACTION WHEN GAME IS OVER
    if(!(game.over)){
      if(MouseButton(e.getButton) == MouseButton.Primary){
        clickedIPos = (e.getY/CellHeight).toInt
        clickedJPos = (e.getX/CellWidth).toInt
        executeTurn()
      }
    }
  }

  // WHEN CLICK ON GRID TO PLACE WEIGHT
  def executeTurn(): Unit = {
    val chosenCoord = grid.gridToCoord(clickedIPos, clickedJPos)
    var chosenScale: Option[Scale] = None
    var chosenPos = 0

    // HELPER FUNCTION
    def findStack(): Unit = {
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
    findStack()
    chosenScale match {
      case Some(scale: Scale) =>
        state.execute(
          placeWeight(
            state.currentTurn,
            chosenPos,
            scale,
            state
          )
        )
        gameLoopLogic()
        infoPane.updateGUI()
        draw()
      case None =>
    }
  }
}

