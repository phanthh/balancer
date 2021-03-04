package game.gui


import game.Game
import game.objects.Command.placeWeight
import game.objects.{Bot, Human, Player}
import game.gui.MainGUI.{createSpacer, draw}
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}
import scalafx.geometry.Pos
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.control.{Alert, Button, ColorPicker, Label, Separator, TextField}
import scalafx.scene.layout.{AnchorPane, BorderPane, HBox, Priority, VBox}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

import scala.util.Random

class InfoPane(val gc: GraphicsContext, val game: Game) extends VBox {
  def state = game.state

  var inputScaleCode: StringProperty = StringProperty("")
  var inputPos: StringProperty = StringProperty("")

  var currentRound: IntegerProperty = IntegerProperty(state.currentRound) // IF ROUND CHANGE
  var currentIdx: IntegerProperty = IntegerProperty(state.currentIdx) // IF TURN CHANGE

  def updateProperty() = {
    currentRound.update(state.currentRound)
    currentIdx.update(state.currentIdx)
  }

  this.alignment = Pos.Center
  this.fillWidth = true
  this.maxWidth = 300
  this.minWidth = 200
  this.spacing = 10

  val playerBlocks = state.players.map(playerBlock).toList

  val turnLabel =
    new Label {
      font = new Font("Arial", 30)
      text = state.currentTurn.name
    }

  val turnInfo =
    new VBox {
      alignment = Pos.Center
      style = toBackgroundCSS(state.currentTurn.propColor)
      children = turnLabel
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
    endTurnButton(),
  )

  val header =
    List(
      new VBox {
        style = toBackgroundCSS(Color.LightGrey)
        alignment = Pos.Center
        children = new Label("SCOREBOARD") {
          font = new Font("Arial", 24)
        }
      },
      new Separator
    )

  val footer =
    List(
      new Separator,
      new VBox {
        style = toBackgroundCSS(Color.LightGrey)
        alignment = Pos.Center
        children =
          new Label {
            hgrow = Priority.Always
            font = new Font("Arial", 30)
            text <== StringProperty("ROUND #") + currentRound.asString()
          }
      }
    )

  this.children = header ++ playerBlocks ++ inputFields ++ footer

  // EVENT LISTENER
  currentIdx.onChange((obs, o, n) => {
    turnLabel.text = state.currentTurn.name
    turnInfo.style = toBackgroundCSS(state.currentTurn.propColor)
  })

  def playerBlock(player: Player) = {

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
          text <== StringProperty("Score: ") + player.propScore.asString()
        },
        new Label {
          font = new Font("Arial", 14)
          text <== StringProperty("Round won: ") + player.propRoundWon.asString()
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
        draw(gc)
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

  def endTurnButton() = {
    new Button {
      text = "END TURN"
      maxWidth = 200
      minWidth = 150
      onAction = _ => {
        ///////// NEW TURN HERE /////////////
        state.undoStack.append(
          placeWeight(state.currentTurn,
            inputPos.value.toInt,
            state.scaleWithCode(inputScaleCode.value(0)).get,
            state
          ).execute()
        )

        ////

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
            case b: Bot =>
              if (Random.nextFloat() > game.botDiffiiculty) {
                b.random()
              } else {
                b.bestMove()
              }
            case h: Human =>
          }
        }
        updateProperty() // FORCE GUI UPDATE
        draw(gc)
      }
    }
  }
}
