package game.gui

import game.Game
import game.grid.Grid._
import game.gui.Constants.{CellHeight, CellWidth, Height, Width}
import game.objects.Player
import scalafx.application.JFXApp
import scalafx.geometry.VPos
import scalafx.scene.Scene
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.layout.{BorderPane, Priority, Region, VBox}
import scalafx.scene.paint.Color._
import scalafx.scene.text.{Font, TextAlignment}


object Constants {
  val Width = 800
  val Height = 800
  val CellWidth = 50
  val CellHeight = 50
}

object MainGUI extends JFXApp {
  val game = new Game()
  game.fileManager.loadGame("loadfile.txt")

  def state = game.state
  def grid = game.grid

  stage = new JFXApp.PrimaryStage {
    title = "Balancer !"
    scene = new Scene(Width, Height) {
      root = {
        new BorderPane {
          top = new TopMenuBar
          center = new MidSplitPlane(game)
        }
      }
    }
  }

  def setup(gc: GraphicsContext): Unit = {
    grid.update()
    gc.setTextAlign(TextAlignment.Center)
    gc.setTextBaseline(VPos.Center)
    gc.setFill(LightGrey)
    gc.setStroke(LightGrey)
    gc.setFont(new Font("Arial", 24))
    gc.fillRect(0, 0, gc.canvas.getWidth, gc.canvas.getHeight)
  }

  def draw(gc: GraphicsContext): Unit = {
    grid.update()
    gc.setFill(White)
    gc.canvas.setWidth(CellWidth * grid.width)
    gc.canvas.setHeight(CellHeight * grid.height)
    gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)

    // Render Grid

    for (i <- 0 until grid.height) {
      for (j <- 0 until grid.width) {
        grid(i, j) match {
          case GROUND => gc.setFill(Brown)
          case FULCRUM => gc.setFill(Grey)
          case PADDER => gc.setFill(LightGrey)
          case LEFT | RIGHT | EMPTY => gc.setFill(White)
          case WILD => gc.setFill(Gray)
          case c: Char if (c.isDigit) => gc.setFill(Grey)
          case c: Char => gc.setFill(state.players.find(_.playerCode == c) match {
            case Some(player: Player) => player.propColor
            case None => Gray
          })
        }
        gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
        gc.setStroke(LightGrey)
        grid(i, j) match {
          case FULCRUM | PADDER | LEFT | RIGHT =>
          case _ => gc.strokeText(grid(i, j).toString, (j + 0.5) * CellWidth, (i + 0.5) * CellHeight)
        }
        gc.strokeRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
      }
    }
  }

  def createSpacer(): Region = {
    val spacer = new Region
    VBox.setVgrow(spacer, Priority.Always)
    spacer
  }
}

