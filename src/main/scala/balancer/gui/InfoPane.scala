package balancer.gui


import balancer.Game
import balancer.gui.MainGUI.{createSpacer, draw, gameLoopLogic}
import balancer.objects.Command.placeWeight
import balancer.objects.Player
import scalafx.beans.property.StringProperty
import scalafx.geometry.Pos
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, Priority, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

class InfoPane(private val game: Game) extends VBox {
  def state = game.state

  var inputScaleCode: StringProperty = StringProperty("")
  var inputPos: StringProperty = StringProperty("")

  alignment = Pos.Center
  fillWidth = true
  maxWidth = 300
  minWidth = 200
  spacing = 10

  //////// UI ELEMENTS THAT CHANGE
  val turnLabel =
    new Label {
      font = new Font("Arial", 30)
      textFill = Color.White
      text = state.currentTurn.name.capitalize
    }

  val turnInfo =
    new VBox {
      alignment = Pos.Center
      style = toBackgroundCSS(state.currentTurn.propColor)
      children = turnLabel
    }

  val weightLeftLabel =
    new Label(){
      font = new Font("Arial", 18)
      text = "Weights Left: " + state.weightLeftOfRound.toString
    }

  val roundFooter =
    new Label {
      hgrow = Priority.Always
      font = new Font("Arial", 30)
      text = "ROUND #" + state.currentRound.toString
    }

  // UPDATE FUNCTION
  def updateGUI() = {
    turnLabel.text = state.currentTurn.name.capitalize
    turnInfo.style = toBackgroundCSS(state.currentTurn.propColor)
    weightLeftLabel.text = "Weights Left: " + state.weightLeftOfRound.toString
    roundFooter.text = "Round #" + state.currentRound.toString
    state.players.foreach(p => p.propScore.update(p.score))
  }
  /////////////////////

  /////// ELEMENTS THAT STATIC
  val playerBlocks = state.players.map(createPlayerBlock).toList

  val endTurnButton =
    new Button {
      text = "END TURN"
      maxWidth = 200
      minWidth = 150
      onAction = _ => {
        // DISABLE BUTTON WHEN GAME IS OVER
        if (!(game.over))
          executeTurn()
      }
    }

  val inputFields = List(
    createSpacer(),
    new Separator,
    turnInfo,
    new TextField {
      promptText = "Enter the scale code"
      maxWidth = 200
      text <==> inputScaleCode
    },
    new TextField {
      promptText = "Enter the position"
      maxWidth = 200
      text <==> inputPos
    },
    endTurnButton,
  )

  val header =
    List(
      new VBox {
        style = toBackgroundCSS(Color.LightGrey)
        alignment = Pos.Center
        children = List(
          new Label("SCOREBOARD") {
            font = new Font("Arial", 24)
          },
          weightLeftLabel
        )

      },
      new Separator
    )

  val footer =
    List(
      new Separator,
      new VBox {
        style = toBackgroundCSS(Color.LightGrey)
        alignment = Pos.Center
        children = roundFooter
      }
    )

  /// ADDING CHILDREN
  children = header ++ playerBlocks ++ inputFields ++ footer

  // DONE

  // HELPER FUNCTION

  def createPlayerBlock(player: Player) = {

    val block = new BorderPane {
      style = toBackgroundCSS(player.propColor)
    }
    val playerName = new Label {
      text = player.name.capitalize + "(" + player.playerCode + ")"
      font = new Font("Arial", 18)
    }

    val playerStats = new VBox {
      alignment = Pos.Center
      spacing = 10
      minWidth = 100
      maxWidth = 200
      children = List(
        new Label {
          font = new Font("Arial", 14)
          text <== player.propScore.asString()
        },
        new Label {
          font = new Font("Arial", 14)
          text <== player.propRoundWon.asString()
        }
      )
    }

    val colorPicker = new ColorPicker(player.propColor) {
      maxWidth = 30
      onAction = (e) => {
        val newColor = new Color(value())
        block.style = toBackgroundCSS(newColor)
        player.propColor = newColor
        turnInfo.style = toBackgroundCSS(state.currentTurn.propColor)
        draw()
      }
    }
    block.left = new VBox {
      alignment = Pos.Center
      minWidth = 80
      children = List(
        playerName,
        colorPicker
      )
    }
    block.right = playerStats
    block
  }

  def toBackgroundCSS(color: Color) =
    s"-fx-background-color: rgb(${color.getRed * 255}, ${color.getGreen * 255}, ${color.getBlue * 255});"

  def toBaseCSS(color: Color) =
    s"-fx-base: rgb(${color.getRed * 255}, ${color.getGreen * 255}, ${color.getBlue * 255});"

  // FOR FORM BASED INPUT
  def executeTurn(): Unit = {
    // TODO: EXCEPTION HANDLING
    state.undoStack.append(
      placeWeight(state.currentTurn,
        inputPos.value.toInt,
        state.scaleWithCode(inputScaleCode.value(0)).get,
        state
      ).execute()
    )
    gameLoopLogic()
    updateGUI() // FORCE GUI UPDATE
    draw()
  }
}
