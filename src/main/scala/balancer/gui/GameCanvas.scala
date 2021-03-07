package balancer.gui

import balancer.Game
import MainGUI.{draw, endTurn, grid}
import balancer.objects.Command.placeWeight
import balancer.objects.{Scale, Stack}
import balancer.utils.Constants._
import scalafx.geometry.Point2D
import scalafx.geometry.Pos
import scalafx.scene.canvas.Canvas
import scalafx.scene.Group
import scalafx.scene.control.ScrollPane
import scalafx.scene.input.MouseButton
import scalafx.scene.layout.VBox


class GameCanvas(private val parentPane: MidSplitPane, private val game: Game) extends ScrollPane {
  private def state = game.state

  private val canvas = new Canvas(ScreenWidth, ScreenHeight)
  val gc = canvas.graphicsContext2D

  private var scaleValue = 0.8
  private val zoomIntensity = 0.02
  private val zoomNode = new Group(canvas)

  // When click to place weight on the canvas
  canvas.onMouseClicked = e => {
    if (!(game.over) && MouseButton(e.getButton) == MouseButton.Primary) {
      executeTurn((e.getY / CellHeight).toInt, (e.getX / CellWidth).toInt)
    }
  }

  // Centering
  private val viewPortContent = new VBox(zoomNode) {
    alignment = Pos.Center
  }
  viewPortContent.setOnScroll(e => {
    e.consume()
    onScroll(e.getTextDeltaY, new Point2D(e.getX, e.getY))
  })

  content = viewPortContent
  pannable = true
  hbarPolicy = ScrollPane.ScrollBarPolicy.Never
  vbarPolicy = ScrollPane.ScrollBarPolicy.Never
  fitToHeight = true
  fitToWidth = true
  updateScale()

  private def updateScale(): Unit = {
    canvas.setScaleX(scaleValue)
    canvas.setScaleY(scaleValue)
  }

  private def onScroll(wheelDelta: Double, mousePoint: Point2D): Unit = {
    val zoomFactor = Math.exp(wheelDelta * zoomIntensity)
    val innerBounds = zoomNode.getLayoutBounds
    val viewportBounds = this.getViewportBounds
    // calculate pixel offsets from [0, 1] range
    val valX = this.getHvalue * (innerBounds.getWidth - viewportBounds.getWidth)
    val valY = this.getVvalue * (innerBounds.getHeight - viewportBounds.getHeight)
    scaleValue = scaleValue * zoomFactor
    updateScale()
    this.layout() // refresh ScrollPane scroll positions & target bounds

    // convert target coordinates to zoomTarget coordinates
    val posInZoomTarget = canvas.parentToLocal(zoomNode.parentToLocal(mousePoint))
    // calculate adjustment of scroll position (pixels)
    val adjustment = canvas.getLocalToParentTransform.deltaTransform(posInZoomTarget.multiply(zoomFactor - 1))
    // convert back to [0, 1] range
    // (too large/small values are automatically corrected by ScrollPane)
    val updatedInnerBounds = zoomNode.getBoundsInLocal
    this.setHvalue((valX + adjustment.getX) / (updatedInnerBounds.getWidth - viewportBounds.getWidth))
    this.setVvalue((valY + adjustment.getY) / (updatedInnerBounds.getHeight - viewportBounds.getHeight))
  }

  private def executeTurn(i: Int, j: Int): Unit = {
    val chosenCoord = grid.gridToCoord(i, j)
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
              case Some(stack: Stack) => {
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
        state.execute(placeWeight(state.currentTurn, chosenPos, scale, state))
        endTurn()
        parentPane.updateInfoPane()
        draw()
      case None =>
    }
  }
}
