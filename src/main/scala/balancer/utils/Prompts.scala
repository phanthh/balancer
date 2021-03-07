package balancer.utils

import balancer.gui.MainGUI.stage
import scalafx.Includes.observableList2ObservableBuffer
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, TextInputDialog}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.io.File

object Prompts {

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

  def invalidDialog(reason: String): Unit = {
    val alert = new Alert(AlertType.Warning){
      initOwner(stage)
      title = "Invalid Input"
      headerText = reason
    }
    alert.showAndWait()
  }
}
