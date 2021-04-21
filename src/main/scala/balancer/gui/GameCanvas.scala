package balancer.gui

import balancer.Game
import balancer.grid.Grid._
import balancer.gui.MainGUI.endTurnHook
import balancer.objects.Command.placeWeight
import balancer.objects.{Human, Player, Scale, Stack}
import balancer.utils.Constants._
import balancer.utils.Helpers.{getDefaultFont, scaleImage}
import scalafx.geometry.{Point2D, Pos, VPos}
import scalafx.scene.Group
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.ScrollPane
import scalafx.scene.input.MouseButton
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.text.TextAlignment


/**
 * The main display of the game
 *
 * @param mainPane ref to the Main Pane
 * @param game     ref to the Game object
 */
class GameCanvas(private val mainPane: MainPane, private val game: Game) extends ScrollPane {
  private val canvas = new Canvas(ScreenWidth, ScreenHeight) {
    onMouseClicked = e => {
      if (!(game.over)
        && MouseButton(e.getButton) == MouseButton.Primary
        && state.currentTurn.isInstanceOf[Human]) {
        executeTurn(e.getX, e.getY)
      }
    }
  }
  private val zoomIntensity = 0.02
  private val gc = canvas.graphicsContext2D
  private val zoomNode = new Group(canvas)
  private val viewPortContent = new VBox(zoomNode) {
    alignment = Pos.Center
    onScroll = e => {
      e.consume()
      onZoomScroll(e.getTextDeltaY, new Point2D(e.getX, e.getY))
    }
  }
  private var scaleValue: Double = 1.0

  // Grid Toggling
  private var gridOn = false

  def toggleGrid() = gridOn = !gridOn

  // Execute a turn when click on the canvas (if the move is valid)

  // ScrollPane settings
  pannable = true
  hbarPolicy = ScrollPane.ScrollBarPolicy.Never
  vbarPolicy = ScrollPane.ScrollBarPolicy.Never
  fitToHeight = true
  fitToWidth = true
  content = viewPortContent
  // Canvas's graphic context settings
  gc.setTextAlign(TextAlignment.Center)
  gc.setTextBaseline(VPos.Center)
  gc.setFont(getDefaultFont(35))
  grid.update()
  updateScale()

  /**
   * Zooming the canvas and adjust the HValue and VValue of the ScrollPane
   * to zoom to the mouse position.
   *
   * @param wheelDelta the scrolling amount
   * @param mousePoint the position of the mouse
   */
  private def onZoomScroll(wheelDelta: Double, mousePoint: Point2D): Unit = {
    val zoomFactor = Math.exp(wheelDelta * zoomIntensity)
    val innerBounds = zoomNode.getLayoutBounds
    val viewportBounds = this.getViewportBounds
    // Calculate pixel offsets from [0, 1] range
    var valX = this.getHvalue * (innerBounds.getWidth - viewportBounds.getWidth)
    var valY = this.getVvalue * (innerBounds.getHeight - viewportBounds.getHeight)
    // Limit the scaleValue to not zoom out too far.
    val maxScaleX = this.getViewportBounds.getWidth / (grid.width * CellWidth)
    val maxScaleY = this.getViewportBounds.getHeight / (grid.height * CellHeight)
    scaleValue = Math.max(scaleValue * zoomFactor, Math.max(maxScaleX, maxScaleY))
    updateScale()
    this.layout() // Refresh ScrollPane scroll positions & target bounds
    // Convert target coordinates to zoomTarget coordinates
    val posInZoomTarget = canvas.parentToLocal(zoomNode.parentToLocal(mousePoint))
    // Calculate adjustment of scroll position (pixels)
    val adjustment = canvas.getLocalToParentTransform.deltaTransform(posInZoomTarget.multiply(zoomFactor - 1))
    // Convert back to [0, 1] range
    // (too large/small values are automatically corrected by ScrollPane)
    val updatedInnerBounds = zoomNode.getBoundsInLocal
    this.setHvalue((valX + adjustment.getX) / (updatedInnerBounds.getWidth - viewportBounds.getWidth))
    this.setVvalue((valY + adjustment.getY) / (updatedInnerBounds.getHeight - viewportBounds.getHeight))
  }

  /**
   * Scale the canvas according to scaleValue
   */
  private def updateScale() = {
    canvas.setScaleX(scaleValue)
    canvas.setScaleY(scaleValue)
  }

  protected def grid = game.grid

  /**
   * Execute a turn
   *
   * @param mouseX X position of the mouse
   * @param mouseY Y position of the mouse
   */
  private def executeTurn(mouseX: Double, mouseY: Double): Unit = {
    val i = (mouseY / CellHeight).toInt
    val j = (mouseX / CellWidth).toInt
    val chosenCoord = grid.gridToCoord(i, j)
    var chosenScale: Option[Scale] = None
    var chosenPos = 0

    // Find the corresponding stack at the clicked grid position
    def findStack(): Unit = {
      for (scale <- state.scalesVector) {
        // Move the origin to the center of the scale's board
        val offset = chosenCoord - scale.boardCenter
        // Check if in the clicking range
        if (
          offset.x >= -scale.radius * 2 &&
            offset.x <= scale.radius * 2 &&
            offset.x % 2 == 0 &&
            offset.x != 0
        ) {
          val xPos = offset.x / 2
          val height = offset.y
          scale(xPos) match {
            // Check if in the clicking range
            case Some(stack: Stack) if (height >= 0 && height <= stack.height + 1) =>
              chosenScale = Some(scale)
              chosenPos = xPos
              return
            case None if (height == 0 || height == 1) => {
              chosenScale = Some(scale)
              chosenPos = xPos
              return
            }
            case _ =>
          }
        }
      }
    }

    findStack()

    chosenScale match {
      case Some(scale: Scale) =>
        state.execute(placeWeight(state.currentTurn, chosenPos, scale, state))
        endTurnHook()
        mainPane.updateSidePane()
        draw()
      case None =>
    }
  }

  /**
   * Draw the game onto the canvas.
   * This will only be called when needed, when there's a change to
   * the game state (new weight added, scale flipped,...)
   */
  def draw(): Unit = {
    // Update the grid of the gamestate before rendering
    grid.update()

    val gridPixelWidth = CellWidth * grid.width
    val gridPixelHeight = CellHeight * grid.height

    // Resized the canvas
    gc.canvas.setWidth(gridPixelWidth)
    gc.canvas.setHeight(gridPixelHeight)

    // Draw the background image
    val scaledImages = skylineImages.map(i => scaleImage(i, gridPixelWidth, gridPixelHeight, true))
    var w = 0.0
    var imgWidth = scaledImages.head.getWidth
    while (w < gridPixelWidth) {
      gc.drawImage(scaledImages(scala.util.Random.nextInt(scaledImages.length)), w, 0)
      w += scaledImages.head.getWidth - 2
    }

    // Rendering each cell on the grid
    for (i <- 0 until grid.height) {
      for (j <- 0 until grid.width) {
        grid(i, j) match {
          case EMPTY =>
          case GROUND => drawCell("ground", i, j)
          case FULCRUM => drawCell("fulcrum", i, j)
          case LEFT => drawCell("left", i, j)
          case RIGHT => drawCell("right", i, j)
          case WILD => drawCell("weight", i, j)
          case PADDER => {
            // Drawing the torque indicator, showing how close is the scale to flipping
            val padderCoord = grid.gridToCoord(i, j)
            var parentScale: Scale = null

            // Helper function searching which scale the padder belong to
            def findScale(): Unit = {
              for (scale <- state.scalesVector) {
                val offset = padderCoord - scale.boardCenter
                if (offset.y == 0 && offset.x >= -scale.radius * 2 && offset.x <= scale.radius * 2) {
                  parentScale = scale
                  return
                }
              }
            }

            findScale()
            val offset = padderCoord - parentScale.boardCenter
            val torqueDiff = parentScale.rightTorque - parentScale.leftTorque
            if (offset.x < 0 && (offset.x - 1) / 2 >= torqueDiff) {
              // The indicator is on the left
              drawCell("padder1", i, j)
              gc.setFill(Color.Red.opacity(0.4))
              gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
            } else if (offset.x > 0 && (offset.x + 1) / 2 <= torqueDiff) {
              // The indicator is on the right
              drawCell("padder1", i, j)
              gc.setFill(Color.Red.opacity(0.4))
              gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
            } else {
              drawCell("padder0", i, j)
            }
          }
          case c: Char if (c.isDigit) => drawCell("padder0", i, j)
          case c: Char =>
            state.players.find(_.playerCode == c) match {
              case Some(player: Player) =>
                drawCell("weight", i, j)
                gc.setFill(player.propColor.opacity(0.5))
                gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
              case None =>
                drawCell("fulcrum", i, j)
            }
        }
        // Drawing the letter and number indicator
        gc.setFill(Color.White)
        grid(i, j) match {
          case FULCRUM | PADDER | LEFT | RIGHT | GROUND =>
          case _ => gc.fillText(grid(i, j).toString, (j + 0.5) * CellWidth, (i + 0.5) * CellHeight)
        }
        // Drawing the checker pattern over if grid is on
        if (gridOn) {
          gc.setStroke(Color.LightGrey.opacity(0.5))
          gc.strokeRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
        }
      }
    }
    // Dim the canvas if game is over
    if (game.over) {
      gc.setFill(Color.Black.opacity(0.5))
      gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)
    }
  }

  // A helper function to draw the i-j cell
  def drawCell(spriteName: String, i: Int, j: Int) = {
    gc.drawImage(spriteMap(spriteName), CellWidth * j, CellHeight * i)
  }

  private def state = game.state
}
