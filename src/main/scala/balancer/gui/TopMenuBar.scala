package balancer.gui

import balancer.gui.MainGUI.{game, stage, state}
import scalafx.Includes._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.io.File

class TopMenuBar extends MenuBar {
  var saveFile = ""
  def saved: Boolean = saveFile.nonEmpty
  def load(filePath: String) = {
    saveFile = filePath
    game.fileManager.loadGame(filePath)
  }
  def save() =
    if(saveFile.isEmpty)
      saveFile = "loadfile.txt" // TODO: Hardcoded
    else
      game.fileManager.saveGame(saveFile)


    menus = List(
    new Menu("File") {
      items = List(
        new MenuItem("New") {
          onAction = _ => {
            if(!saved)
              uWannaSaveDialog(
                reason = "New game",
                yes = () => {save(); game.reset()},
                no = () => game.reset(),
                cancel = () => {}
              )
            else game.reset()
          }
        },
        new MenuItem("Open...") {
          onAction = _ =>
            openDialog(
              success = (f) => {
                if(!saved)
                  uWannaSaveDialog(
                    reason = "Loading New Save File",
                    yes = () => {
                      save()
                      load(f.getAbsolutePath)
                    },
                    no = () => load(f.getAbsolutePath),
                    cancel = () => {}
                  )
                else
                  load(f.getAbsolutePath)
              },
              failed = () => {}
            )
        },
        new SeparatorMenuItem(),
        new MenuItem("Save") {
          onAction = _ => save()
        },
        new MenuItem("Save as...") {
          onAction = _ =>
            openDialog(
              success = (f) => {
                saveFile = f.getAbsolutePath
                save()
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
              cancel = () => {}
            )
        }
      )
    },
    new Menu("Edit") {
      items = List(
        new MenuItem("Add Human") {
          onAction = _ =>
            askNameDialog("Adding Human Player") match {
              case Some(name) => state.buildHuman(name)
              case None =>
            }
        },
        new MenuItem("Add Bot") {
          onAction = _ =>
            askNameDialog("Adding Bot Player") match {
              case Some(name) => state.buildBot(name)
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

  def uWannaSaveDialog(reason: String, yes: () => Unit, no: () => Unit, cancel: () => Unit) = {
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
      case Some(ButtonType.Cancel) => cancel()
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

  def askNameDialog(header: String): Option[String] = {
    val textInputDialog = new TextInputDialog("Aalto") {
      initOwner(stage)
      headerText = header
      contentText = "Please enter the name:"
    }
    textInputDialog.showAndWait()
  }
}
