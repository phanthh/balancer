package balancer.gui

import balancer.Game
import balancer.objects.{Bot, Human}
import balancer.utils.Constants.{ScreenHeight, ScreenWidth, logo}
import balancer.utils.Helpers.{placeSomeWildScale, placeSomeWildWeight}
import balancer.utils.Prompts
import balancer.utils.Prompts.showInfoDialog
import scalafx.animation._
import scalafx.application.JFXApp
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.effect.BoxBlur
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, StackPane, VBox}
import scalafx.scene.text.Font
import scalafx.util.Duration

import scala.util.Random


/*
  The entry to the GUI
 */
object MainGUI extends JFXApp {
  private val game = new Game()
  private val fontFile = "file:fonts/cyber.otf"
  // Components of the main scene
  var topMenuBar: TopMenuBar = _

  game.fileManager.loadDefault()

  // Setup a basic stage
  stage = new JFXApp.PrimaryStage {
    title = "Balancer"
    width = ScreenWidth
    height = ScreenHeight
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
  var midSplitPane: MainPane = _

  def getDefaultFont(size: Int) = Font.loadFont(fontFile, size)


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
  def setGameScene() = {
    midSplitPane = new MainPane(game)
    topMenuBar = new TopMenuBar(midSplitPane, game)
    // Preserver ScreenWidth and ScreenHeight
    val (w, h) = getSceneDimension

    stage.scene = new Scene(w, h) {
      root = new BorderPane {
        top = topMenuBar
        center = midSplitPane
      }
    }

    stage.sizeToScene()
    draw()
  }

  private def getSceneDimension =
    if (stage.getScene == null)
      (ScreenWidth, ScreenHeight)
    else
      (stage.getScene.getWidth.toInt, stage.getScene.getHeight.toInt)

  def draw() = midSplitPane.drawCanvas()

  def setMenuScene() = {
    if (midSplitPane == null) midSplitPane = new MainPane(game)
    if (topMenuBar == null) topMenuBar = new TopMenuBar(midSplitPane, game)

    val imgView = new ImageView(logo)

    val transition = new TranslateTransition {
      duration = Duration(1500)
      fromY = -20
      toY = 20
      autoReverse = true
      cycleCount = Animation.Indefinite
      node = imgView
    }
    transition.play()
    game.over = true
    midSplitPane.items.clear()
    midSplitPane.items.addAll(
      new StackPane {
        children = Seq(
          midSplitPane.gameCanvas,
          new VBox {
            alignment = Pos.Center
            midSplitPane.gameCanvas.setEffect(new BoxBlur(10, 10, 3))
            children = Seq(
              imgView,
            )
          },
        )
      }
    )

    val timeline = new Timeline {
      t =>
      cycleCount = Animation.Indefinite
      autoReverse = true
      keyFrames = Seq(
        KeyFrame(
          time = Duration(30000),
          values = Set(
            KeyValue(midSplitPane.gameCanvas.hvalue, 1)
          )
        ))
    }

    timeline.play()

    val (w, h) = getSceneDimension

    stage.scene = new Scene(w, h) {
      root = new BorderPane {
        top = topMenuBar
        center = midSplitPane
      }
    }

    stage.sizeToScene()
    draw()
  }

  setMenuScene()

  def select(id: String) = stage.getScene.lookup("#" + id)

  // This is run after a move is finished (end of a turn)
  def endTurnHook(): Unit = {

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
        placeSomeWildScale(state, amount = 1)
        placeSomeWildWeight(state, amount = 5)

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
          if (Random.nextFloat() > bot.difficulty) {
            bot.random()
          } else {
            bot.bestMove()
          }

          // End of bot move, calling again endTurnHook()
          endTurnHook()
        case human: Human =>
        /*
          Human turn will be conducted by clicking on the
          canvas or entering the position and scale into the
          form. These is handled in MainPane and InfoPane
          respectively.
         */
      }
    }
  }
}

