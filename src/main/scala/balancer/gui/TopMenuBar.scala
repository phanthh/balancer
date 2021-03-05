package balancer.gui

import balancer.Game
import balancer.gui.MainGUI.{stage, updateScene}
import scalafx.Includes._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.io.File

class TopMenuBar(private val game: Game) extends MenuBar {
  def state = game.state

  def defaultFile = game.fileManager.defaultFile

  def saved = game.fileManager.saved

  def savedFilePath = game.fileManager.savedFilePath

  def save() = game.fileManager.saveGame(savedFilePath)

  def save(filePath: String) = game.fileManager.saveGame(filePath)

  def load(filePath: String) = game.fileManager.loadGame(filePath)

  menus = List(
    new Menu("File") {
      items = List(
        new MenuItem("New") {
          onAction = _ => {
            if (!saved) save()
            game.reset()
            load(defaultFile)
            updateScene()
          }

        },
        new MenuItem("Open...") {
          onAction = _ =>{
            if (!saved) save()
            openDialog(
              success = (f) => {
                game.reset()
                load(f.getAbsolutePath)
                updateScene()
              },
              failed = () => {}
            )
          }
        },
        new SeparatorMenuItem(),
        new MenuItem("Save...") {
          onAction = _ =>
            saveDialog(
              success = (f) => {
                save(f.getAbsolutePath)
              },
              failed = () => {}
            )
        },
        new SeparatorMenuItem(),
        new MenuItem("Exit") {
          onAction = _ =>
            uWannaSaveDialog(
              reason = "Exiting Confirmation",
              yes = () => {
                save()
                sys.exit(0)
              },
              no = () => sys.exit(0),
            )
        }
      )
    },
    new Menu("Edit") {
      items = List(
        new MenuItem("Add Human") {
          onAction = _ =>
            askNameDialog("Adding Human Player") match {
              case Some(name) => {
                state.buildHuman(name)
                updateScene()
              }
              case None =>
            }
        },
        new MenuItem("Add Bot") {
          onAction = _ =>
            askNameDialog("Adding Bot Player") match {
              case Some(name) => {
                state.buildBot(name)
                updateScene()
              }
              case None =>
            }
        },
      )
    },
    new Menu("+ Difficulty") {
      onAction = _ =>
        game.botDiffiiculty = Math.min(game.botDiffiiculty + 0.1, 1.0)
    },
    new Menu("- Difficulty") {
      onAction = _ =>
        game.botDiffiiculty = Math.max(game.botDiffiiculty - 0.1, 0.0)
    }
  )

  def uWannaSaveDialog(reason: String, yes: () => Unit, no: () => Unit) = {
    val yesButton = new ButtonType("Yes")
    val noButton = new ButtonType("No")

    val alert = new Alert(AlertType.Confirmation) {
      initOwner(stage)
      title = reason
      headerText = "Do you want to save your current game ?"
      buttonTypes = Seq(
        yesButton, noButton, ButtonType.Cancel)
    }
    val result = alert.showAndWait()
    result match {
      case Some(yesButton) => yes()
      case Some(noButton) => no()
      case _ =>
    }
  }

  def openDialog(success: (File) => Unit, failed: () => Unit) = {
    val fileChooser = new FileChooser {
      title = "Open File"
      extensionFilters ++= Seq(
        new ExtensionFilter("Text Files", "*.txt"),
        new ExtensionFilter("Image Files", Seq("*.png", "*.jpg", "*.gif")),
        new ExtensionFilter("Audio Files", Seq("*.wav", "*.mp3", "*.aac")),
        new ExtensionFilter("All Files", "*.*")
      )
    }
    val result = fileChooser.showOpenDialog(stage)
    result match {
      case f: File => success(f)
      case _ => failed()
    }
  }

  def saveDialog(success: (File) => Unit, failed: () => Unit) = {
    val fileChooser = new FileChooser {
      title = "Save File"
      extensionFilters ++= Seq(
        new ExtensionFilter("Text Files", "*.txt"),
        new ExtensionFilter("Image Files", Seq("*.png", "*.jpg", "*.gif")),
        new ExtensionFilter("Audio Files", Seq("*.wav", "*.mp3", "*.aac")),
        new ExtensionFilter("All Files", "*.*")
      )
    }
    val result = fileChooser.showSaveDialog(stage)
    result match {
      case f: File => success(f)
      case _ => failed()
    }
  }

  def askNameDialog(header: String): Option[String] = {
    val textInputDialog = new TextInputDialog("Aalto") {
      initOwner(stage)
      headerText = header
      contentText = "Please enter the name:"
    }
    textInputDialog.showAndWait()
  }
}
