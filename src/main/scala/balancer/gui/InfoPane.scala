package balancer.gui


import balancer.Game
import balancer.gui.MainGUI.{createVSpacer, draw, gameLoopLogic}
import balancer.objects.Command.placeWeight
import balancer.objects.Player
import scalafx.beans.property.StringProperty
import scalafx.geometry.Pos
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, HBox, Priority, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

class InfoPane(private val game: Game) extends VBox {
  def state = game.state

  // PROPERTY LINKER FOR INPUT FIELDS
  var inputScaleCode: StringProperty = StringProperty("")
  var inputPos: StringProperty = StringProperty("")

  alignment = Pos.Center
  fillWidth = true
  maxWidth = 300
  minWidth = 200
  spacing = 10

  //////// UI ELEMENTS THAT CHANGE
  val currentTurnLabel =
    new Label {
      font = new Font("Arial", 30)
      textFill = Color.White
      text = state.currentTurn.name.capitalize
    }

  val currentTurnBox =
    new VBox {
      alignment = Pos.Center
      style = toBackgroundCSS(state.currentTurn.propColor)
      children = currentTurnLabel
    }

  val numberOfWeightLeftLabel =
    new Label() {
      font = new Font("Arial", 18)
      text = "Weights Left: " + state.weightLeftOfRound.toString
    }

  val currentRoundLabel =
    new Label {
      hgrow = Priority.Always
      font = new Font("Arial", 30)
      text = "ROUND #" + state.currentRound.toString
    }


  val botDifficultySlider =
    new VBox {
      val difficultyLabel = new Label {
        text = "Bot Difficulty: " + game.botDiffiiculty.toString
      }
      val difficultySlider = new ScrollBar {
        max = 1.0
        min = 0.0
        unitIncrement = 0.1
        value.onChange((_, _, _) => {
          game.botDiffiiculty = value()
          difficultyLabel.text = "Bot Difficulty: " + ((game.botDiffiiculty * 10).toInt/ 10.0).toString
        })
      }
      alignment = Pos.Center
      children = List(difficultyLabel, difficultySlider)
    }

  val undoRedoButtons =
    new HBox {
      alignment = Pos.Center
      spacing = 20
      children = List(
        new Button {
          text = "Undo"
          onAction = _ => {
            state.undo()
            updateGUI()
            draw()
          }
        },
        new Button {
          text = "Redo"
          onAction = _ => {
            state.redo()
            updateGUI()
            draw()
          }
        },
      )
    }

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

  // UPDATE FUNCTION: UPDATE ALL ELEMENTS THAT CHANGES AND THEIR LOGICS
  def updateGUI() = {
    currentTurnLabel.text = state.currentTurn.name.capitalize
    currentTurnBox.style = toBackgroundCSS(state.currentTurn.propColor)
    numberOfWeightLeftLabel.text = "Weights Left: " + state.weightLeftOfRound.toString
    currentRoundLabel.text = "Round #" + state.currentRound.toString
    state.players.foreach(p => p.propScore.update(p.score))
  }

  /////////////////////

  /////// ELEMENTS THAT STATIC
  val allPlayersInfo = List(
    new VBox {
      style = toBackgroundCSS(Color.LightGrey)
      alignment = Pos.Center
      children = state.players.map(createPlayerInfoBlock).toList
    }
  )

  val inputFields = List(
    createVSpacer(),
    new Separator,
    botDifficultySlider,
    new Separator,
    undoRedoButtons,
    new Separator,
    currentTurnBox,
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
          numberOfWeightLeftLabel
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
        children = currentRoundLabel
      }
    )

  /// ADDING CHILDREN
  children = header ++ allPlayersInfo ++ inputFields ++ footer

  // HELPER FUNCTION

  def createPlayerInfoBlock(player: Player) = {

    val parentBlock = new BorderPane {
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
        parentBlock.style = toBackgroundCSS(newColor)
        player.propColor = newColor
        currentTurnBox.style = toBackgroundCSS(state.currentTurn.propColor)
        draw()
      }
    }
    parentBlock.left = new VBox {
      alignment = Pos.Center
      minWidth = 80
      children = List(
        playerName,
        colorPicker
      )
    }
    parentBlock.right = playerStats
    parentBlock
  }

  def toBackgroundCSS(color: Color) =
    s"-fx-background-color: rgb(${color.getRed * 255}, ${color.getGreen * 255}, ${color.getBlue * 255});"

  def toBaseCSS(color: Color) =
    s"-fx-base: rgb(${color.getRed * 255}, ${color.getGreen * 255}, ${color.getBlue * 255});"

  // FOR FORM BASED INPUT
  def executeTurn(): Unit = {
    // TODO: EXCEPTION HANDLING
    state.execute(
      placeWeight(state.currentTurn,
        inputPos.value.toInt,
        state.scaleWithCode(inputScaleCode.value(0)).get,
        state
      )
    )
    gameLoopLogic()
    updateGUI() // FORCE GUI UPDATE
    draw()
  }
}
