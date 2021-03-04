package game.gui

import game.Game
import game.grid.Grid._
import game.gui.Constants.{CellHeight, CellWidth, Height, Width}
import game.objects.Command.placeWeight
import game.objects.Player
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalafx.geometry.{Pos, VPos}
import scalafx.scene.Scene
import scalafx.scene.canvas.{Canvas, GraphicsContext}
import scalafx.scene.control._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, HBox, Priority, Region, VBox}
import scalafx.scene.paint.Color._
import scalafx.scene.text.{Font, TextAlignment}


object Constants {
  val Width = 800
  val Height = 800
  val CellWidth = 50
  val CellHeight = 50
}

object MainGUI extends JFXApp {
  val game = new Game()
  game.fileManager.loadGame("loadfile.txt")

  def state = game.state

  def grid = game.grid

  var inputScaleCode: StringProperty = StringProperty("")
  var inputPos: StringProperty = StringProperty("")
  var currentRound: IntegerProperty = IntegerProperty(state.currentRound)
  var currentIdx: IntegerProperty = IntegerProperty(state.currentIdx)

  stage = new JFXApp.PrimaryStage {
    title = "Balancer !"
    scene = new Scene(Width, Height) {
      root = {
        val menuBar = new MenuBar {
          menus = List(
            new Menu("File") {
              items = List(
                new MenuItem("New"),
                new MenuItem("Save"),
                new SeparatorMenuItem(),
                new MenuItem("Exit")
              )
            },
            new Menu("Edit") {
              items = List(
                new MenuItem("Add Human"),
                new MenuItem("Add Bot"),
                new SeparatorMenuItem(),
                new MenuItem("+ Difficulty"),
                new MenuItem("- Difficulty")
              )
            }
          )
        }

        val splitPane = new SplitPane

        val canvas = new Canvas(Width, Height)
        val gc = canvas.graphicsContext2D
        setup(gc)
        draw(gc)
        canvas.onMouseMoved = (e: MouseEvent) => draw(gc)

        val scrollPane = new ZoomableScrollPane(canvas)

        def playerBlock(player: Player) = {
          new HBox {
            style = "-fx-border-color: red;"
            alignment = Pos.Center
            spacing = 20
            children = List(
              new Button(player.name.capitalize + "(" + player.player_code + ")"),
              new VBox {
                style = "-fx-border-color: red;"
                alignment = Pos.Center
                spacing = 10
                children = List(
                  new Label {
                    text <== StringProperty("Score: ") + player.propScore.asString()
                  },
                  new Label {
                    text <== StringProperty("Round won: ") + player.propRoundWon.asString()
                  }
                )
              }
            )
          }
        }

        val playerBlocks = state.players.map(playerBlock).toList
        val inputFields = List(
          new TextField {
            promptText = "Enter the scale code"
            maxWidth = 200
            text <==> inputScaleCode
          },
          new TextField {
            promptText = "Enter the position"
            maxWidth = 200
            text <==> inputPos
          }
        )

        val infoPane = new VBox {
          alignment = Pos.Center
          fillWidth = true
          maxWidth = 300
          minWidth = 200
          spacing = 20
          children =
            List(
              createSpacer(),
              new Label("SCOREBOARD"){
                style = "-fx-border-color: red;"
              },
              createSpacer()
            ) ++
              playerBlocks ++
              inputFields ++
              List(
                createSpacer(),
                new Button {
                  text = "END TURN"
                  maxWidth = 200
                  onAction = _ => {
                    ///////// NEW TURN HERE /////////////
                    state.undoStack.append(
                      placeWeight(state.currentTurn,
                        inputPos.value.toInt,
                        state.scaleWithCode(inputScaleCode.value(0)).get,
                        state
                      ).execute()
                    )
                    draw(gc)
                  }
                },
                createSpacer(),
                new Label {
                  text <== StringProperty("ROUND #") + currentRound.asString()
                },
                createSpacer()
              )
        }

        splitPane.items.addAll(scrollPane, infoPane)
        splitPane.setDividerPosition(0, 0.75)

        new BorderPane {
          top = menuBar
          center = splitPane
        }
      }
    }
  }

  def setup(gc: GraphicsContext): Unit = {
    grid.update()
    gc.setTextAlign(TextAlignment.Center)
    gc.setTextBaseline(VPos.Center)
    gc.setFill(LightGrey)
    gc.setStroke(LightGrey)
    gc.setFont(new Font("Arial", 24))
    gc.fillRect(0, 0, gc.canvas.getWidth, gc.canvas.getHeight)
  }

  def draw(gc: GraphicsContext): Unit = {
    grid.update()
    gc.setFill(LightGrey)
    gc.canvas.setWidth(CellWidth * grid.width)
    gc.canvas.setHeight(CellHeight * grid.height)
    gc.fillRect(0, 0, CellWidth * grid.width, CellHeight * grid.height)

    // Render Grid

    for (i <- 0 until grid.height) {
      for (j <- 0 until grid.width) {
        grid(i, j) match {
          case FULCRUM | GROUND => gc.setFill(Brown)
          case PADDER => gc.setFill(LightGrey)
          case LEFT | RIGHT => gc.setFill(Red)
          case WILD => gc.setFill(Gray)
          case EMPTY => gc.setFill(White)
          case e: Char if (e.isDigit) => gc.setFill(Grey)
          case _ => gc.setFill(Red)
        }
        gc.fillRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
        gc.setStroke(White)
        gc.strokeText(grid(i, j).toString, (j + 0.5) * CellWidth, (i + 0.5) * CellHeight)
        gc.setStroke(LightGrey)
        gc.strokeRect(j * CellWidth, i * CellHeight, CellWidth, CellHeight)
      }
    }
  }

  private def createSpacer(): Region = {
    val spacer = new Region
    VBox.setVgrow(spacer, Priority.Always)
    spacer
  }
}

