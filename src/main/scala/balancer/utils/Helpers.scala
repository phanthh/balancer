package balancer.utils

import balancer.State
import scalafx.scene.layout.{HBox, Priority, Region, VBox}
import scalafx.scene.paint.Color

import scala.util.Random

object Helpers {

  def toBackgroundCSS(color: Color) =
    s"-fx-background-color: rgb(${color.getRed * 255}, ${color.getGreen * 255}, ${color.getBlue * 255});"

  def toBaseCSS(color: Color) =
    s"-fx-base: rgb(${color.getRed * 255}, ${color.getGreen * 255}, ${color.getBlue * 255});"


  def createVSpacer(): Region = {
    val spacer = new Region
    VBox.setVgrow(spacer, Priority.Always)
    spacer
  }

  def createHSpacer(): Region = {
    val spacer = new Region
    HBox.setHgrow(spacer, Priority.Always)
    spacer
  }

  def randomColor(): Color = Color.hsb(Random.nextInt(255), 1, 1)

  def placeSomeWildWeight(state: State, amount: Int) = {
    for(i <- 0 until amount){
      state.buildWildWeight()
    }
  }

  def placeSomeWildScale(state: State, amount: Int) = {
    for(i <- 0 until amount){
      state.buildWildScale()
    }
  }
}
