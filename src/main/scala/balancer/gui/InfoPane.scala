package balancer.gui

import balancer.Game
import balancer.gui.MainGUI.{draw, select}
import balancer.objects.Player
import balancer.utils.Helpers.{getTextColorFitBG, toBackgroundCSS}
import scalafx.beans.property.StringProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, HBox, Priority, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

class InfoPane(private val game: Game) extends VBox {
  // For ease of reference
  private def state = game.state

  // Binding points for form input
//  private var inputScaleCode: StringProperty = StringProperty("")
//  private var inputPos: StringProperty = StringProperty("")

  alignment = Pos.Center
  fillWidth = true
  maxWidth = 300
  minWidth = 200
  spacing = 10

  private val allPlayersInfo =
    new VBox {
      vgrow = Priority.Always
      spacing = 10
      alignment = Pos.TopCenter
      children = state.players.map(createPlayerInfoBlock).toList
    }

  /*
     Methods to update elements
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

  // Layouts (header, body, footer)

  children = List(
    // Header
    new VBox {
      alignment = Pos.Center
      children = List(
        new Label("SCOREBOARD") {
          font = new Font("Arial", 24)
        },

        new Label() {
          id = "weightsLeftLabel"
          font = new Font("Arial", 18)
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
          text = "Weight"
          onAction = _ => {
            if (!(game.over)) {
              state.buildWildWeight()
              updateContent()
              draw()
            }
          }
        },
        new Button {
          text = "Scale"
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
    // Bot difficulty sliders
    new VBox {
      val difficultyLabel = new Label {
        text = "Bot Difficulty: " + game.botDifficulty.toString
      }
      val difficultySlider = new ScrollBar {
        max = 1.0
        min = 0.0
        unitIncrement = 0.1
        value = game.botDifficulty
        value.onChange((_, _, _) => {
          game.botDifficulty = value()
          difficultyLabel.text = "Bot Difficulty: " + ((game.botDifficulty * 10).toInt / 10.0).toString
        })
      }
      alignment = Pos.Center
      children = List(difficultyLabel, difficultySlider)
    }
    ,
    new Separator,
    // Undo Redo Button
    new HBox {
      alignment = Pos.Center
      spacing = 20
      children = List(
        new Button {
          text = "Undo"
          onAction = _ => {
            if (!(game.over)) {
              state.undo()
              updateContent()
              draw()
            }
          }
        },
        new Button {
          text = "Redo"
          onAction = _ => {
            if (!(game.over)) {
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
          font = new Font("Arial", 30)
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
          font = new Font("Arial", 24)
          text = "Round #" + state.currentRound.toString
        }

    }
  )

  // Helpers function
  private def createPlayerInfoBlock(player: Player): BorderPane = {
    val parentBlock = new BorderPane {
      style = toBackgroundCSS(player.propColor)
    }
    val playerName =
      new Label {
        text = player.name.capitalize + "(" + player.playerCode + ")"
        textFill = getTextColorFitBG(player.propColor)
        font = new Font("Arial", 18)
      }
    val playerScore =
      new Label {
        font = new Font("Arial", 14)
        textFill = getTextColorFitBG(player.propColor)
        text <== StringProperty("Score: ") + player.propScore.asString()
      }

    val playerRoundWon =
      new Label {
        font = new Font("Arial", 14)
        textFill = getTextColorFitBG(player.propColor)
        text <== StringProperty("Won: ") + player.propRoundWon.asString()
      }

    val colorPicker = new ColorPicker(player.propColor) {
      maxWidth = 30
      onAction = (e) => {
        val newColor = new Color(value())
        player.propColor = newColor
        parentBlock.style = toBackgroundCSS(newColor)
        playerName.textFill = getTextColorFitBG(newColor)
        playerScore.textFill = getTextColorFitBG(newColor)
        playerRoundWon.textFill = getTextColorFitBG(newColor)
        updateContent()
        draw()
      }
    }

    val deleteButton = new VBox {
      alignment = Pos.Center
      padding = Insets(10, 10, 10, 10)
      children = new Button {
        text = "X"
        onAction = _ => {
          if(state.players.length > 1){
            state.removePlayer(player)
            updateContent()
            draw()
          }
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
    parentBlock.center = new VBox {
      alignment = Pos.Center
      spacing = 10
      minWidth = 100
      maxWidth = 200
      children = List(playerScore, playerRoundWon)
    }
    parentBlock.right = deleteButton
    parentBlock
  }

  // Execute when end turn button is clicked
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
//      endTurn()
//      updateContent()
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
