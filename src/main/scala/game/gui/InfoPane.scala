package game.gui


import game.Game
import game.objects.Command.placeWeight
import game.objects.{Bot, Human, Player}
import game.gui.MainGUI.{createSpacer, draw, gameLoopLogic}
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}
import scalafx.event.ActionEvent
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

  var propRound: IntegerProperty = IntegerProperty(state.currentRound) // IF ROUND CHANGE
  var propTurnIdx: IntegerProperty = IntegerProperty(state.currentIdx) // IF TURN CHANGE

  def updateProperty() = {
    propRound.update(state.currentRound)
    propTurnIdx.update(state.currentIdx)
  }

  alignment = Pos.Center
  fillWidth = true
  maxWidth = 300
  minWidth = 200
  spacing = 10

  val playerBlocks = state.players.map(createPlayerBlock).toList

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

  val weightLeftLabel =
    new Label(){
      font = new Font("Arial", 18)
      text = "Weights Left: " + state.weightLeftOfRound.toString
    }

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
        children =
          new Label {
            hgrow = Priority.Always
            font = new Font("Arial", 30)
            text <== StringProperty("ROUND #") + propRound.asString()
          }
      }
    )

  children = header ++ playerBlocks ++ inputFields ++ footer

  // EVENT LISTENER: CHANGE WHEN A TURN COMPLETE
  propTurnIdx.onChange((obs, o, n) => {
    turnLabel.text = state.currentTurn.name.capitalize
    turnInfo.style = toBackgroundCSS(state.currentTurn.propColor)
    weightLeftLabel.text = "Weights Left: " + state.weightLeftOfRound.toString
  })
  //

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

  def endTurnButton() =
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
    updateProperty() // FORCE GUI UPDATE
    draw(gc)
  }
}
