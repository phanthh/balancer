package balancer.gui

import balancer.Game
import balancer.grid.Grid._
import balancer.gui.Constants.{CellHeight, CellWidth, Height, Width}
import balancer.objects.{Bot, Human, Player, Scale}
import scalafx.application.JFXApp
import scalafx.geometry.VPos
import scalafx.scene.Scene
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout.{BorderPane, HBox, Priority, Region, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, TextAlignment}

import scala.util.Random


object Constants {
  val Width = 800
  val Height = 800
  val CellWidth = 50
  val CellHeight = 50
}

object MainGUI extends JFXApp {
  private val game = new Game()
  game.fileManager.loadDefault()

  // SETUP DUMMY STAGE
  stage = new JFXApp.PrimaryStage {
    title = "Balancer !"
    scene = new Scene
  }
  if(game.state.players.isEmpty){
    PromptGUI.askNameDialog("Adding Human Player") match {
      case Some(name) => {
        game.state.buildHuman(name)
      }
      case None =>
    }
    game.fileManager.saveGame(game.fileManager.defaultFile)
  }

  // Scene
  var topMenuBar: TopMenuBar = _
  var midSplitPane: MidSplitPane = _
  initScene()

  ///
  def state = game.state
  def grid = game.grid
  def gc = midSplitPane.gc
  ///

  def initScene() = {
    topMenuBar = new TopMenuBar(game)
    midSplitPane = new MidSplitPane(game)
    val currentWidth = stage.getWidth
    val currentHeight = stage.getHeight
    this.stage.setScene(new Scene(currentWidth, currentHeight) {
      root = {
        new BorderPane {
          top = topMenuBar
          center = midSplitPane
        }
      }
    })
    setup()
    draw()
  }

  def setup(): Unit = {
    grid.update()
    gc.setTextAlign(TextAlignment.Center)
    gc.setTextBaseline(VPos.Center)
    gc.setFill(Color.LightGrey)
    gc.setStroke(Color.LightGrey)
    gc.setFont(new Font("Arial", 24))
    gc.fillRect(0, 0, gc.canvas.getWidth, gc.canvas.getHeight)
  }

  def draw(): Unit = {
    grid.update()
    gc.setFill(Color.White)
    gc.canvas.setWidth(CellWidth * grid.width)
    gc.canvas.setHeight(CellHeight * grid.height)
    gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)

    // Render Grid

    for (i <- 0 until grid.height) {
      for (j <- 0 until grid.width) {
        grid(i, j) match {
          case GROUND => gc.setFill(Color.Brown)
          case FULCRUM => gc.setFill(Color.Grey)
          case PADDER => {
            // TORQUE INDICATOR
            val padderCoord = grid.gridToCoord(i,j)
            var parentScale: Scale = null
            def findScale(): Unit = {
              for(scale <- state.scales){
                val offset = padderCoord - scale.boardCenter
                if(offset.y == 0 && offset.x >= -scale.radius*2 && offset.x <= scale.radius*2) {
                  parentScale = scale
                  return
                }
              }
            }
            findScale()
            val offset = padderCoord - parentScale.boardCenter
            val torqueDiff = parentScale.rightTorque - parentScale.leftTorque
            if(offset.x < 0 && (offset.x-1)/2 >= torqueDiff){
              // ON LEFT SIDE
              gc.setFill(Color.Red)
            } else if(offset.x > 0 && (offset.x+1)/2 <= torqueDiff){
              // ON RIGHT SIDE
              gc.setFill(Color.Red)
            } else
              gc.setFill(Color.LightGrey)
          }
          case LEFT | RIGHT | EMPTY => gc.setFill(Color.White)
          case WILD => gc.setFill(Color.Gray)
          case c: Char if (c.isDigit) => gc.setFill(Color.Grey)
          case c: Char => gc.setFill(state.players.find(_.playerCode == c) match {
            case Some(player: Player) => player.propColor
            case None => Color.Gray
          })
        }
        gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
        gc.setStroke(Color.LightGrey)
        grid(i, j) match {
          case FULCRUM | PADDER | LEFT | RIGHT =>
          case _ => gc.strokeText(grid(i, j).toString, (j + 0.5) * CellWidth, (i + 0.5) * CellHeight)
        }
        gc.strokeRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
      }
    }

    // GREY OUT THE CANVAS IF GAME END
    if(game.over) {
      gc.setFill(Color(0, 0, 0, 0.4))
      gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)
    }
  }

  // THIS IS RUN AFTER A MOVE
  def gameLoopLogic(): Unit = {

    state.deleteFlippedScale()
    // THE GAME IS NOW UNSAVED
    if(game.fileManager.saved) game.fileManager.saved = false

    state.weightLeftOfRound -= 1
    state.currentTurnIdx += 1

    if (state.currentTurnIdx >= state.players.length) state.currentTurnIdx = 0

    //// IF THE ROUND END
    if (state.weightLeftOfRound <= 0 || game.over) {
      (new Alert(AlertType.Information) {
        title = "End of Round !!"
        headerText = s"The winner of this round is: ${game.winner}"
      }).showAndWait()

      game.winner.incRoundWon()
      state.currentRound += 1


      // IF ALSO FINAL ROUND
      if (state.currentRound > game.numRounds || game.over) {
        game.over = true // GUARANTEE game.over = true
        ///
        (new Alert(AlertType.Information) {
          title = "Game Over !!"
          headerText = s"The winner of the game is: ${game.finalWinner}"
          contentText =  "!!!! Congratulation !!!!"
        }).showAndWait()
        ///
      } else {
        //// IF THE GAME CONTINUE => START NEW ROUND
        state.weightLeftOfRound = game.weightsPerRound
        state.currentTurnIdx = 0
        ///
        (new Alert(AlertType.Information) {
          title = "New round begin !!!"
          headerText = s"ROUND: ${state.currentRound}"
        }).showAndWait()
        ///
      }
    } else {
      //// IF THE ROUND CONTINUE => NEXT PLAYER
      state.currentTurn match {
        case bot: Bot =>
          if (Random.nextFloat() > game.botDiffiiculty) {
            bot.random()
          } else {
            bot.bestMove()
          }
          gameLoopLogic()
        case human: Human =>
      }
    }
  }

  // HELPERS FUNCTION TO CREATE GUI
  def createVSpacer(): Region = {
    val spacer = new Region
    VBox.setVgrow(spacer, Priority.Always)
    spacer
  }

  def createHSpacer(): Region = {
    val spacer = new Region
    HBox.setHgrow(spacer, Priority.Always)
    spacer
  }

  def randomColor(): Color = Color.hsb(Random.nextInt(255), 1, 1)
}

