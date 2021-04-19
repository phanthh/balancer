package balancer.gui

import balancer.Game
import balancer.grid.Grid._
import balancer.gui.MainGUI.endTurnHook
import balancer.objects.Command.placeWeight
import balancer.objects.{Player, Scale, Stack}
import balancer.utils.Constants._
import scalafx.geometry.{Point2D, Pos, VPos}
import scalafx.scene.Group
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.ScrollPane
import scalafx.scene.image.Image
import scalafx.scene.input.MouseButton
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, TextAlignment}


/**
 * The main display of the game
 *
 * @param mainPane ref to the Main Pane
 * @param game     ref to the Game object
 */
class GameCanvas(private val mainPane: MainPane, private val game: Game) extends ScrollPane {
  private val canvas = new Canvas(ScreenWidth, ScreenHeight)
  private val zoomIntensity = 0.02
  def gc = canvas.graphicsContext2D

  // ScrollPane settings
  pannable = true
  hbarPolicy = ScrollPane.ScrollBarPolicy.Never
  vbarPolicy = ScrollPane.ScrollBarPolicy.Never
  fitToHeight = true
  fitToWidth = true

  // Canvas's graphic context settings
  gc.setTextAlign(TextAlignment.Center)
  gc.setTextBaseline(VPos.Center)
  gc.setFont(Font.loadFont("file:fonts/cyber.otf", 35))
  grid.update()
  private val zoomNode = new Group(canvas)
  /**
   * Zooming to the position of the mouse by scrolling the mouse wheel
   */
  private val viewPortContent = new VBox(zoomNode) {
    alignment = Pos.Center
  }
  private var scaleValue = 0.8

  viewPortContent.setOnScroll(e => {
    e.consume()
    onScroll(e.getTextDeltaY, new Point2D(e.getX, e.getY))
  })

  /**
   * Zooming the canvas and adjust the HValue and VValue of the ScrollPane
   * to give the illusion of zooming to the mouse position.
   *
   * @param wheelDelta the scrolling amount
   * @param mousePoint the position of the mouse
   */
  private def onScroll(wheelDelta: Double, mousePoint: Point2D): Unit = {
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

  // Execute a turn when click on the canvas (if the move is valid)
  canvas.onMouseClicked = e => {
    if (!(game.over) && MouseButton(e.getButton) == MouseButton.Primary) {
      executeTurn(e.getX, e.getY)
    }
  }

  content = viewPortContent
  updateScale()

  /**
   * Scale the canvas according to scaleValue
   */
  private def updateScale() = {
    canvas.setScaleX(scaleValue)
    canvas.setScaleY(scaleValue)
  }

  private def grid = game.grid
  private def state = game.state

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

    /*
      Find the corresponding stack at the clicked grid position
     */

    def findStack(): Unit = {
      for (scale <- state.scalesVector) {
        // Move the origin to the center of the scale's board
        val offset = chosenCoord - scale.boardCenter
        // Check if in the range
        if (
          offset.x >= -scale.radius * 2 &&
            offset.x <= scale.radius * 2 &&
            offset.x % 2 == 0 &&
            offset.x != 0
        ) {
          val xPos = offset.x / 2
          val height = offset.y
          scale(xPos) match {
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
        mainPane.updateInfoPane()
        draw()
      case None =>
    }
  }

  /**
   * @return The pixel width of the grid
   */
  def gridPixelWidth = CellWidth * grid.width

  /**
   * @return the pixel height of the grid
   */
  def gridPixelHeight = CellHeight * grid.height

  // Loading all the sprite
  lazy val spriteMap = List("ground", "padder0", "padder1", "right", "left", "weight", "fulcrum").map(
    f => (f, new Image(s"file:assets/tiles/$f.png", CellWidth, CellHeight, false, true))
  ).toMap

  def drawCell(cellType: String, i: Int, j: Int) = {
    gc.drawImage(spriteMap(cellType), CellWidth * j, CellHeight * i)
  }

  private var gridOn = false
  def toggleGrid() = {
    gridOn = !gridOn
  }

  /**
   * Draw the game onto the canvas.
   * This will only be called when needed, when there is a change to the game state
   * (new weight added, scale flipped,...)
   */
  def draw(): Unit = {
    // Update the grid representation of the gamestate before rendering
    grid.update()

    // Resized the canvas
    gc.canvas.setWidth(gridPixelWidth)
    gc.canvas.setHeight(gridPixelHeight)

    // Draw the background image
    var w = 0.0
    var skylineImages = List(
      new Image("file:assets/backgrounds/skyline1.png", CellWidth * grid.width, CellHeight * grid.height, true, true),
      new Image("file:assets/backgrounds/skyline2.png", CellWidth * grid.width, CellHeight * grid.height, true, true)
    )
    while (w < CellWidth * grid.width) {
      gc.drawImage(skylineImages(scala.util.Random.nextInt(skylineImages.length)), w, 0)
      w += skylineImages.head.getWidth
    }

    // Rendering each cell on the grid
    for (i <- 0 until grid.height) {
      for (j <- 0 until grid.width) {
        grid(i, j) match {
          case GROUND =>
            drawCell("ground", i, j)
          case FULCRUM =>
            drawCell("fulcrum", i, j)
          case PADDER => {
            // This will draw the torque indicator, showing how close is the scale to flipping
            val padderCoord = grid.gridToCoord(i, j)
            var parentScale: Scale = null

            // A helper function using return to search which scale the padder belong to
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
            // Translate the padder position to where the scale center is at the origin
            val offset = padderCoord - parentScale.boardCenter
            val torqueDiff = parentScale.rightTorque - parentScale.leftTorque
            if (offset.x < 0 && (offset.x - 1) / 2 >= torqueDiff) {
              drawCell("padder1", i, j)
              gc.setFill(Color.Red.opacity(0.4))
              gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
            } else if (offset.x > 0 && (offset.x + 1) / 2 <= torqueDiff) {
              drawCell("padder1", i, j)
              gc.setFill(Color.Red.opacity(0.4))
              gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
            } else {
              drawCell("padder0", i, j)
            }
          }
          case LEFT =>
            drawCell("left", i, j)
          case RIGHT =>
            drawCell("right", i, j)
          case EMPTY =>
          case WILD =>
            drawCell("weight", i, j)
          case c: Char if (c.isDigit) =>
            drawCell("padder0", i, j)
          case c: Char =>
            // If it is a letter -> player's weight
            state.players.find(_.playerCode == c) match {
              case Some(player: Player) => {
                drawCell("weight", i, j)
                // Overlay by the player color
                gc.setFill(player.propColor.opacity(0.5))
                gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
              }
              case None => {
                // If it not any player's weight -> Just a scale code
                drawCell("fulcrum", i, j)
              }
            }
        }
        // Drawing the letter and number indicator
        gc.setFill(Color.White)
        grid(i, j) match {
          case FULCRUM | PADDER | LEFT | RIGHT | GROUND =>
          case _ => gc.fillText(grid(i, j).toString, (j + 0.5) * CellWidth, (i + 0.5) * CellHeight)
        }
        if(gridOn){
          // Drawing the checker pattern over if grid is turnOn
          gc.setStroke(Color.LightGrey.opacity(0.5))
          gc.strokeRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
        }
      }
    }

    // Grey out the canvas if game end
    if (game.over) {
      gc.setFill(Color(0, 0, 0, 0.4))
      gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)
    }
  }
}
