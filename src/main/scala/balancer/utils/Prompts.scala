package balancer.utils

import balancer.gui.MainGUI.stage
import scalafx.Includes.observableList2ObservableBuffer
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, TextInputDialog}
import scalafx.scene.layout.Region
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.io.File

object Prompts {

  def askSavingDialog(reason: String, yes: () => Unit, no: () => Unit, cancel: () => Unit = () => {}) = {
    val alert = new Alert(AlertType.Confirmation) {
      initOwner(stage)
      title = reason
      headerText = "Do you want to save your game ?"
      buttonTypes = Seq(
        ButtonType.Yes, ButtonType.No, ButtonType.Cancel)
    }
    val result = alert.showAndWait()
    result match {
      case Some(ButtonType.Yes) => yes()
      case Some(ButtonType.No) => no()
      case Some(ButtonType.Cancel) => cancel()
      case _ =>
    }
  }

  def openFileDialog(success: (File) => Unit, failed: () => Unit) = {
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

  def saveFileDialog(success: (File) => Unit, failed: () => Unit) = {
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
    val textInputDialog = new TextInputDialog {
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

  def showInfoDialog(titleText: String, header: String, content: String): Unit = {
    val alert = new Alert(AlertType.Information){
      dialogPane().setMinHeight(Region.USE_PREF_SIZE)
      initOwner(stage)
      title = titleText
      headerText = header
      contentText = content
      resizable = true
    }
    alert.showAndWait()
  }
}
