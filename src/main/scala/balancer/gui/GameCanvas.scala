package balancer.gui

import balancer.Game
import balancer.grid.Grid._
import balancer.gui.MainGUI.endTurn
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




class GameCanvas(private val parentPane: MainPane, private val game: Game) extends ScrollPane {
  private def state = game.state

  private val canvas = new Canvas(ScreenWidth, ScreenHeight)

  private def grid = game.grid

  def gc = canvas.graphicsContext2D

  /*
    Setting up basic canvas settings, such as default text
    alignment, font and background.
   */
  pannable = true
  hbarPolicy = ScrollPane.ScrollBarPolicy.Never
  vbarPolicy = ScrollPane.ScrollBarPolicy.Never
  fitToHeight = true
  fitToWidth = true

  gc.setTextAlign(TextAlignment.Center)
  gc.setTextBaseline(VPos.Center)
  gc.setFill(Color.LightGrey)
  gc.setStroke(Color.LightGrey)
//  gc.setFont(Font("fonts/Cybersomething.ttf", 24))
//  gc.setFont(Font.loadFont("fonts/Cybersomthing.ttf", 24))
  gc.setFont(Font.loadFont("file:fonts/cyber.otf", 35))
//  gc.setFont(new Font("Arial", 24))
  gc.fillRect(0, 0, gc.canvas.getWidth, gc.canvas.getHeight)
  grid.update()

  // Zooming and panning

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

  private def updateScale(): Unit = {
    canvas.setScaleX(scaleValue)
    canvas.setScaleY(scaleValue)
  }

  private def maxScaleX() = {
    this.getViewportBounds.getWidth / (grid.width * CellWidth)
  }

  private def maxScaleY() = {
    this.getViewportBounds.getHeight / (grid.height * CellHeight)
  }

  updateScale()

  private def onScroll(wheelDelta: Double, mousePoint: Point2D): Unit = {
    val zoomFactor = Math.exp(wheelDelta * zoomIntensity)
    val innerBounds = zoomNode.getLayoutBounds
    val viewportBounds = this.getViewportBounds
    // calculate pixel offsets from [0, 1] range
    var valX = this.getHvalue * (innerBounds.getWidth - viewportBounds.getWidth)
    var valY = this.getVvalue * (innerBounds.getHeight - viewportBounds.getHeight)
    scaleValue = Math.max(scaleValue * zoomFactor, Math.max(maxScaleX(), maxScaleY()))
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

    /*
      Find the corresponding stack from the player-picked Cell
     */
    def findStack(): Unit = {
      for (scale <- state.scalesVector) {
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

  // LOADING SPRITE HERE

  lazy val spriteMap = List("padder0", "padder1", "right", "left", "weight", "fulcrum").map(
    f => (f, new Image(s"file:assets/tiles/$f.png", CellWidth, CellHeight, false, true))
  ).toMap

  def drawCell(cellType: String, i: Int, j: Int) = {
    gc.drawImage(spriteMap(cellType), CellWidth*j, CellHeight*i)
  }


  /*
     Draw the game onto the canvas
     This will be called when there is a change to the game state
     (new weight added, scale flipped,...)
   */
  def draw(): Unit = {
    // Update the grid representation of the gamestate before rendering
    grid.update()

    // Each grid cell dimension (width, height) is fixed. The canvas is resized accordingly
    gc.canvas.setWidth(CellWidth * grid.width)
    gc.canvas.setHeight(CellHeight * grid.height)

    // Color the background white (DEBUG)
    gc.setFill(Color.White)
    gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)

    var w = 0.0
    var skylineImages = List(
      new Image("file:assets/backgrounds/skyline1.png", CellWidth * grid.width, CellHeight * grid.height, true, true),
      new Image("file:assets/backgrounds/skyline2.png", CellWidth * grid.width, CellHeight * grid.height, true, true)
    )
    while(w < CellWidth*grid.width){
      gc.drawImage(skylineImages(scala.util.Random.nextInt(skylineImages.length)), w, 0)
      w += skylineImages.head.getWidth
    }

    // Rendering the grid
    for (i <- 0 until grid.height) {
      for (j <- 0 until grid.width) {
        grid(i, j) match {
          case GROUND =>
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
              gc.fillRect(j*CellWidth, i*CellHeight, CellWidth, CellHeight)
            } else if (offset.x > 0 && (offset.x + 1) / 2 <= torqueDiff) {
              drawCell("padder1", i, j)
              gc.setFill(Color.Red.opacity(0.4))
              gc.fillRect(j*CellWidth, i*CellHeight, CellWidth, CellHeight)
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
                gc.fillRect(j*CellWidth, i*CellHeight, CellWidth, CellHeight)
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
          case FULCRUM | PADDER | LEFT | RIGHT =>
          case _ => gc.fillText(grid(i, j).toString, (j + 0.5) * CellWidth, (i + 0.5) * CellHeight)
        }
        // Drawing the checker pattern over
//        gc.setStroke(Color.LightGrey.opacity(0.5))
//        gc.strokeRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
      }
    }

    // Grey out the canvas if game end
    if (game.over) {
      gc.setFill(Color(0, 0, 0, 0.4))
      gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)
    }
  }
}
