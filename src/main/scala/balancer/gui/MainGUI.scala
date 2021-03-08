package balancer.gui

import balancer.Game
import balancer.grid.Grid._
import balancer.objects.{Bot, Human, Player, Scale}
import balancer.utils.Constants.{CellHeight, CellWidth, ScreenHeight, ScreenWidth}
import balancer.utils.Helpers.{placeSomeWildScale, placeSomeWildWeight}
import balancer.utils.Prompts
import scalafx.application.JFXApp
import scalafx.geometry.VPos
import scalafx.scene.Scene
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout.BorderPane
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, TextAlignment}

import scala.util.Random


/*
  The entry to the GUI
 */
object MainGUI extends JFXApp {
  private val game = new Game()
  game.fileManager.loadDefault()

  // Setup a basic stage
  stage = new JFXApp.PrimaryStage {
    width = ScreenWidth
    height = ScreenHeight
    title = "Balancer !"
    scene = new Scene
  }

  // If there is no player in the default file -> prompt for name
  if (game.state.players.isEmpty) {
    Prompts.askNameDialog("Default file has no player") match {
      case Some(name) => {
        game.state.buildHuman(name)
      }
      case None =>
    }
    game.fileManager.loadDefault()
  }

  // Components of the main scene
  var topMenuBar: TopMenuBar = _
  var midSplitPane: MidSplitPane = _
  createScene()

  // For ease of references
  def state = game.state

  def grid = game.grid

  def gc = midSplitPane.gc

  /*
    Initialize the scene.

    This is also called when there is a change in the UI that can't
    be updated dynamically (i.e adding a new VBox,
    a new Label, etc,...). An entire new scene will be built.
   */
  def createScene() = {
    midSplitPane = new MidSplitPane(game)
    topMenuBar = new TopMenuBar(midSplitPane, game)
    // Preserver ScreenWidth and ScreenHeight
    this.stage.setScene(
      new Scene(stage.getWidth, stage.getHeight) {
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

  /*
    Setting up basic canvas settings, such as default text
    alignment, font and background.
   */
  def setup(): Unit = {
    grid.update()
    gc.setTextAlign(TextAlignment.Center)
    gc.setTextBaseline(VPos.Center)
    gc.setFill(Color.LightGrey)
    gc.setStroke(Color.LightGrey)
    gc.setFont(new Font("Arial", 24))
    gc.fillRect(0, 0, gc.canvas.getWidth, gc.canvas.getHeight)
  }

  /*
     Draw the game onto the canvas

     This will be called when there is a change in the game state
     (new weight added, scale flipped,...)
   */
  def draw(): Unit = {
    // Update the grid representation of the gamestate
    grid.update()

    // Each grid cell dimension (width, height) is fixed. The canvas is resized accordingly
    gc.canvas.setWidth(CellWidth * grid.width)
    gc.canvas.setHeight(CellHeight * grid.height)

    // Color the background white
    gc.setFill(Color.White)
    gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)

    // Rendering the grid
    for (i <- 0 until grid.height) {
      for (j <- 0 until grid.width) {
        grid(i, j) match {
          case GROUND => gc.setFill(Color.Brown)
          case FULCRUM => gc.setFill(Color.Grey)
          case PADDER => {
            // This will draw the torque indicator, showing how close is the scale to flipping
            val padderCoord = grid.gridToCoord(i, j)
            var parentScale: Scale = null

            // A helper function using return to search which scale the padder belong to
            def findScale(): Unit = {
              for (scale <- state.scales) {
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
              gc.setFill(Color.Red) // On the left side
            } else if (offset.x > 0 && (offset.x + 1) / 2 <= torqueDiff) {
              gc.setFill(Color.Red) // On the right side
            } else
              gc.setFill(Color.LightGrey)
          }
          case LEFT | RIGHT | EMPTY =>
            gc.setFill(Color.White)
          case WILD =>
            gc.setFill(Color.Gray)
          case c: Char if (c.isDigit) =>
            gc.setFill(Color.Grey) // If it is a number indicating the distance from the center
          case c: Char =>
            // If it is a letter -> player's weight
            gc.setFill(state.players.find(_.playerCode == c) match {
              case Some(player: Player) => player.propColor
              case None => Color.Gray
            })
        }
        gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)

        // Drawing the letter and number indicator
        gc.setStroke(Color.LightGrey)
        grid(i, j) match {
          case FULCRUM | PADDER | LEFT | RIGHT =>
          case _ => gc.strokeText(grid(i, j).toString, (j + 0.5) * CellWidth, (i + 0.5) * CellHeight)
        }

        // Drawing the checker pattern over
        gc.strokeRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
      }
    }

    // Grey out the canvas if game end
    if (game.over) {
      gc.setFill(Color(0, 0, 0, 0.4))
      gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)
    }
  }

  // This is run after a move is finished (end of a turn)
  def endTurn(): Unit = {

    state.deleteFlippedScale()
    state.weightLeftOfRound -= 1
    state.currentTurnIdx += 1

    // If it is the last player, loop back
    if (state.currentTurnIdx >= state.players.length) state.currentTurnIdx = 0

    // If the round ends
    if (state.weightLeftOfRound <= 0 || game.over) {

      //
      (new Alert(AlertType.Information) {
        title = "End of Round !!"
        headerText = s"The winner of this round is: ${game.winner}"
      }).showAndWait()

      game.winner.incRoundWon()
      state.currentRound += 1

      // Place some wild weight and scale

      // if it is also the final round
      if (state.currentRound > game.numRounds || game.over) {

        // GUARANTEE game.over = true
        game.over = true

        //
        (new Alert(AlertType.Information) {
          title = "Game Over !!"
          headerText = s"The winner of the game is: ${game.finalWinner}"
          contentText = "!!!! Congratulation !!!!"
        }).showAndWait()

      } else {
        // If the game continue => start new round
        state.weightLeftOfRound = game.weightsPerRound
        state.currentTurnIdx = 0

        placeSomeWildScale(state, amount=1)
        placeSomeWildWeight(state, amount=5)

        //
        (new Alert(AlertType.Information) {
          title = "New round begin !!!"
          headerText = s"ROUND: ${state.currentRound}"
        }).showAndWait()
      }
    } else {
      // The round continue -> move on to next player
      state.currentTurn match {
        case bot: Bot =>
          /*
           Simple implementation of difficulty for bot
           Higher value of game.botDifficulty -> higher chance
           of the bot just placing weight randomly.
           */
          if (Random.nextFloat() > game.botDifficulty) {
            bot.random()
          } else {
            bot.bestMove()
          }

          // End of move, calling again endTurn()
          endTurn()
        case human: Human =>
        /*
          Human turn will be determined by clicking on the
          canvas or entering the position and scale into the
          form. These is handled in MidSplitPane and InfoPane
          respectively.
         */
      }
    }
  }
}

