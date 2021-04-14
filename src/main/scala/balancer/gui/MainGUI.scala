package balancer.gui

import balancer.Game
import balancer.objects.{Bot, Human}
import balancer.utils.Constants.{ScreenHeight, ScreenWidth}
import balancer.utils.Helpers.{placeSomeWildScale, placeSomeWildWeight}
import balancer.utils.Prompts
import balancer.utils.Prompts.showInfoDialog
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane

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
    stage.scene =
      new Scene(stage.getWidth, stage.getHeight) {
        root = {
          new BorderPane {
            top = topMenuBar
            center = midSplitPane
          }
        }
      }

    stage.sizeToScene()
    draw()
  }

  def draw() = midSplitPane.drawCanvas()

  def select(id: String)= {
    stage.getScene.lookup("#" + id)
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
      showInfoDialog(
        titleText = "End of Round",
        header = s"The winner of this round is: ${game.winner}",
        content = ""
      )
      game.winner.incRoundWon()
      state.currentRound += 1

      // If it is also the final round
      if (state.currentRound > game.numRounds || game.over) {
        // GUARANTEE game.over = true
        game.over = true
        showInfoDialog(
          titleText = "Game Over!!!",
          header = s"The winner of the game is: ${game.finalWinner}",
          content = "!!!! Congratulation !!!!"
        )
      } else {
        // If the game continue => start new round
        state.weightLeftOfRound = game.weightsPerRound
        state.currentTurnIdx = 0

        // Place some wild weight and scale
        placeSomeWildScale(state, amount=1)
        placeSomeWildWeight(state, amount=5)

//        showInfoDialog(
//          titleText = "New round begin !!!",
//          header = s"ROUND: ${state.currentRound}",
//          content = ""
//        )
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
          Human turn will be conducted by clicking on the
          canvas or entering the position and scale into the
          form. These is handled in MidSplitPane and InfoPane
          respectively.
         */
      }
    }
  }
}

