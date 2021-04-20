package balancer.gui

import balancer.Game
import balancer.gui.MainGUI.{draw, getDefaultFont, select}
import balancer.objects.{Bot, Player}
import balancer.utils.Helpers.{getTextColorFitBG, toBackgroundCSS}
import scalafx.beans.binding.Bindings
import scalafx.beans.property.StringProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, HBox, Priority, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

class InfoPane(private val game: Game) extends VBox {
  private val allPlayersInfo =
    new VBox {
      vgrow = Priority.Always
      spacing = 10
      alignment = Pos.TopCenter
      children = state.players.map(createPlayerInfoBlock).toList
    }

  // Binding points for form input (For manual input - debugging)
  // private var inputScaleCode: StringProperty = StringProperty("")
  // private var inputPos: StringProperty = StringProperty("")

  alignment = Pos.Center
  fillWidth = true
  maxWidth = 300
  minWidth = 200
  spacing = 10

  /**
   * Updating UI elements when the state changes.
   */
  def updateContent() = {
    select("turnLabel").asInstanceOf[javafx.scene.control.Label].setText(state.currentTurn.name.capitalize)
    select("turnLabel").asInstanceOf[javafx.scene.control.Label].setTextFill(getTextColorFitBG(state.currentTurn.propColor))
    select("turnBox").setStyle(toBackgroundCSS(state.currentTurn.propColor))
    select("weightsLeftLabel").asInstanceOf[javafx.scene.control.Label].setText("Weights Left: " + state.weightLeftOfRound.toString)
    select("roundLabel").asInstanceOf[javafx.scene.control.Label].setText("ROUND #" + state.currentRound.toString)
    state.players.foreach(p => p.propScore.update(p.score))
    allPlayersInfo.children = state.players.sortBy(-_.propScore.value).map(createPlayerInfoBlock).toList
  }

  private def state = game.state

  children = List(
    // Header
    new VBox {
      alignment = Pos.Center
      children = List(
        new Label("SCOREBOARD") {
          font = getDefaultFont(18)
        },
        new Label() {
          id = "weightsLeftLabel"
          font = getDefaultFont(18)
          text = "Weights Left: " + state.weightLeftOfRound.toString
        }
      )
    },
    new Separator,
    // Body
    // Players' scores, colors, ...
    allPlayersInfo,
    // Adding random weight and scale buttons
    new HBox {
      alignment = Pos.Center
      spacing = 20
      children = List(
        new Button {
          text = "Add weight"
          font = getDefaultFont(14)
          onAction = _ => {
            if (!(game.over)) {
              state.buildWildWeight()
              updateContent()
              draw()
            }
          }
        },
        new Button {
          text = "Add scale"
          font = getDefaultFont(14)
          onAction = _ => {
            if (!(game.over)) {
              state.buildWildScale()
              updateContent()
              draw()
            }
          }
        },
      )
    },
    new Separator,
    // Undo Redo Button
    new HBox {
      alignment = Pos.Center
      spacing = 20
      children = List(
        new Button {
          text = "Undo"
          font = getDefaultFont(14)
          onAction = _ => {
            if (!(game.over) && state.undoable) {
              state.undo()
              updateContent()
              draw()
            }
          }
        },
        new Button {
          text = "Redo"
          font = getDefaultFont(14)
          onAction = _ => {
            if (!(game.over) && state.redoable) {
              state.redo()
              updateContent()
              draw()
            }
          }
        },
      )
    },
    new Separator,
    // Current turn label/indicator
    new VBox {
      id = "turnBox"
      alignment = Pos.Center
      style = toBackgroundCSS(state.currentTurn.propColor)
      children =
        new Label {
          id = "turnLabel"
          font = getDefaultFont(30)
          textFill = getTextColorFitBG(state.currentTurn.propColor)
          text = state.currentTurn.name.capitalize
        }
    },
    // Input fields for adding scale manually (DEBUG)
    //    new TextField {
    //      promptText = "Enter the scale code"
    //      maxWidth = 200
    //      text <==> inputScaleCode
    //    },
    //    new TextField {
    //      promptText = "Enter the position"
    //      maxWidth = 200
    //      text <==> inputPos
    //    },
    //    // End turn button to submit turn
    //    new Button {
    //      text = "END TURN"
    //      maxWidth = 200
    //      minWidth = 150
    //      onAction = _ => {
    //        // Disable button when game is over
    //        if (!(game.over)) executeTurn()
    //      }
    //    },
    //    new Separator,
    new VBox {
      alignment = Pos.Center
      children =
        new Label {
          id = "roundLabel"
          hgrow = Priority.Always
          font = getDefaultFont(24)
          text = "Round #" + state.currentRound.toString
        }
    }
  )

  // Helpers function
  private def createPlayerInfoBlock(player: Player): BorderPane = {
    val parentBlock = new BorderPane {
      style = toBackgroundCSS(player.propColor)
      padding = Insets(5)
    }

    val playerName =
      new Label {
        text = player.name.capitalize + "(" + player.playerCode + ")"
        textFill = getTextColorFitBG(player.propColor)
        font = getDefaultFont(24)
      }
    val playerScore =
      new Label {
        font = getDefaultFont(14)
        textFill = getTextColorFitBG(player.propColor)
        text <== StringProperty("Score: ") + player.propScore.asString()
      }

    val playerRoundWon =
      new Label {
        font = getDefaultFont(14)
        textFill = getTextColorFitBG(player.propColor)
        text <== StringProperty("Won: ") + player.propRoundWon.asString()
      }


    val deleteButton = new VBox {
      alignment = Pos.Center
      padding = Insets(10, 10, 10, 10)
      children = new Button {
        text = "X"
        font = getDefaultFont(14)
        onAction = _ => {
          if (state.players.length > 1) {
            state.removePlayer(player)
            updateContent()
            draw()
          }
        }
      }
    }

    parentBlock.center = new VBox {
      alignment = Pos.Center
      spacing = 10
      minWidth = 100
      maxWidth = 200
      children = List(playerScore, playerRoundWon)
    }
    parentBlock.right = deleteButton

    // Bot difficulty sliders
    var difficultyLabel: Label = null
    player match {
      case bot: Bot =>
        val diff = new HBox {
          difficultyLabel = new Label {
            text <== Bindings.createStringBinding(
              () => "Difficulty: " + (bot.difficultyProp.value * 10.0).toInt / 10.0,
              bot.difficultyProp
            )
            textFill = getTextColorFitBG(bot.propColor)
            margin = Insets(5, 5, 0, 5)
            font = getDefaultFont(14)
          }
          alignment = Pos.Center
          margin = Insets(5, 5, 0, 5)
          children = List(difficultyLabel,
            new ScrollBar {
              max = 1.0
              min = 0.0
              unitIncrement = 0.1
              value <==> bot.difficultyProp
              hgrow = Priority.Always
              vgrow = Priority.Always
            }
          )
        }
        parentBlock.bottom = diff
      case _ =>
    }

    val colorPicker = new ColorPicker(player.propColor) {
      maxWidth = 30
      onAction = (e) => {
        val newColor = new Color(value())
        val textFillColor = getTextColorFitBG(newColor)
        if (newColor != player.propColor) {
          player.propColor = newColor
          parentBlock.style = toBackgroundCSS(newColor)
          playerName.textFill = textFillColor
          playerScore.textFill = textFillColor
          playerRoundWon.textFill = textFillColor
          if(player.isInstanceOf[Bot]){
            difficultyLabel.textFill = textFillColor
          }
          updateContent()
          draw()
        }
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
    parentBlock
  }

  // Execute when end turn button is clicked (For Debuging)
  //  private def executeTurn(): Unit = {
  //    try {
  //      val pos = inputPos.value.toIntOption match {
  //        case Some(pos: Int) =>
  //          if (pos == 0) throw new InvalidInput("Position cannot be 0")
  //          pos
  //        case None => throw new InvalidInput("Invalid Position")
  //      }
  //
  //      val scale = inputScaleCode.value.headOption match {
  //        case Some(code: Char) =>
  //          state.scaleWithCode(code).getOrElse(throw new InvalidInput(
  //            s"Invalid scale code must be: ${state.scalesVector.map(_.code).mkString(",")}"
  //          ))
  //        case None =>
  //          throw new InvalidInput("Invalid scale code")
  //      }
  //      state.execute(placeWeight(state.currentTurn, pos, scale, state))
  //      endTurnHook()
  //      updateScoreBoard()
  //      draw()
  //    } catch {
  //      case e: ArrayIndexOutOfBoundsException =>
  //        invalidDialog(s"Position is off the scale")
  //      case e: OccupiedPosition =>
  //        invalidDialog("Position has already been occupied")
  //      case e: InvalidInput =>
  //        invalidDialog(e.getMessage)
  //    }
  //  }
}
