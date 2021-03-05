package game.gui

import game.Game
import game.grid.Grid._
import game.gui.Constants.{CellHeight, CellWidth, Height, Width}
import game.objects.{Bot, Human, Player}
import scalafx.application.JFXApp
import scalafx.geometry.VPos
import scalafx.scene.Scene
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout.{BorderPane, Priority, Region, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.text.{Font, TextAlignment}

import scala.util.Random


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
  val midSplitPane = new MidSplitPlane(game)
  val topMenuBar = new TopMenuBar

  stage = new JFXApp.PrimaryStage {
    title = "Balancer !"
    scene = new Scene(Width, Height) {
      root = {
        new BorderPane {
          top = topMenuBar
          center = midSplitPane
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
    println("RENDERED") // TODO
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

  def gameLoopLogic(): Unit = {
    // AFTER A TURN
    state.weightLeftOfRound -= 1
    state.currentIdx += 1

    if (state.currentIdx >= state.players.length) state.currentIdx = 0

    //// IF THE ROUND END
    if (state.weightLeftOfRound <= 0 || game.over) {
      (new Alert(AlertType.Information) {
        title = "End of Round !!"
        headerText = s"The winner of this round is: ${game.winner}"
      }).showAndWait()

      game.winner.win()
      state.currentRound += 1
      // IF ALSO FINAL ROUND I.E THE GAME END
      if (state.currentRound > game.numRounds || game.over) {
        game.over = true
        (new Alert(AlertType.Information) {
          title = "Game Over !!"
          headerText = s"The winner of the game is: ${game.finalWinner}"
          contentText =
            "================================================\n" +
              "========== !!!! Congratulation !!!! ============\n" +
              "================================================\n"
        }).showAndWait()
      } else {
        //// IF THE GAME CONTINUE => START NEW ROUND
        state.weightLeftOfRound = game.weightsPerRound
        state.currentIdx = 0
        (new Alert(AlertType.Information) {
          title = "New round begin !!!"
          headerText = s"ROUND: ${state.currentRound}"
        }).showAndWait()
      }
    } else {
      //// IF THE ROUND CONTINUE => NEXT PLAYER
      state.currentTurn match {
        case bot: Bot => botMove(bot)
        case human: Human =>
      }
    }
  }

  def createSpacer(): Region = {
    val spacer = new Region
    VBox.setVgrow(spacer, Priority.Always)
    spacer
  }

  def randomColor(): Color = Color.hsb(Random.nextInt(255), 1, 1)

  def botMove(bot: Bot) = {
    if (Random.nextFloat() > game.botDiffiiculty) {
      bot.random()
    } else {
      bot.bestMove()
    }
    gameLoopLogic()
  }
}

