package balancer.utils

import balancer.State
import balancer.utils.Constants.FontFile
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

import java.awt.Desktop
import java.io.IOException
import java.net.{URI, URISyntaxException}
import scala.util.Random

object Helpers {

  /**
   * Converting scalafx's Color to CSS background color
   * @param color: the color
   * @return the color as CSS background color
   */
  def toBackgroundCSS(color: Color) =
    s"-fx-background-color: rgb(${color.getRed * 255}, ${color.getGreen * 255}, ${color.getBlue * 255});"

  /**
   * Get the appropriate text fill color depending on the background color
   * @param bgColor: the background color of the text
   * @return the best text-fill color in CSS
   */
  def getTextColorFitBG(bgColor: Color) = {
    val brightness = Math.sqrt(
      0.241 * (bgColor.getRed) * (bgColor.getRed) +
        0.691 * (bgColor.getGreen) * (bgColor.getGreen) +
        0.068 * (bgColor.getBlue) * (bgColor.getBlue))
    if (brightness > 0.5) Color.Black else Color.White
  }

  /**
   * Open a URL in the default browser
   * @param url the URL to be opened
   * @return
   */
  def openURLInDefaultBrowser(url: String) = {
    if (Desktop.isDesktopSupported) {
      val desktop = Desktop.getDesktop
      try desktop.browse(new URI(url))
      catch {
        case e@(_: IOException | _: URISyntaxException) =>
          e.printStackTrace()
      }
    }
    else {
      val runtime = Runtime.getRuntime
      try runtime.exec("xdg-open " + url)
      catch {
        case e: IOException =>
          e.printStackTrace()
      }
    }
  }

  def randomColor(): Color = Color.hsb(Random.nextInt(255), 1, 1)

  def placeSomeWildWeight(state: State, amount: Int) = {
    for (i <- 0 until amount) {
      state.buildRandomWeight()
    }
  }

  def placeSomeWildScale(state: State, amount: Int) = {
    for (i <- 0 until amount) {
      state.buildRandomScale()
    }
  }

  def scaleImage(source: Image, targetWidth: Int, targetHeight: Int, preserveRatio: Boolean) = {
    val imageView = new ImageView(source)
    imageView.setPreserveRatio(preserveRatio)
    imageView.setFitWidth(targetWidth)
    imageView.setFitHeight(targetHeight)
    imageView.snapshot(null, null)
  }

  def getDefaultFont(size: Int) = Font.loadFont(FontFile, size)

  @inline def clamp[@specialized(Int, Double) T : Ordering](value: T, low: T, high: T): T = {
    import Ordered._
    if (value < low) low else if (value > high) high else value
  }

  @inline def loop(value: Int, modulo: Int): Int = {
    (value % modulo + modulo) % modulo
  }
}
