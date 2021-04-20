package balancer.gui

import balancer.utils.Constants.{ScreenHeight, ScreenWidth}
import scalafx.Includes.jfxSceneProperty2sfx
import scalafx.animation.AnimationTimer
import scalafx.beans.property.DoubleProperty
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.{Group, Scene}


/**
 * The main display of the game
 */
class MenuCanvas extends Group {
  private val logo = new Image("file:assets/logo_edited2.png")

  val imX = DoubleProperty(200)
  val imY = DoubleProperty(200)

  val cX = 200.0
  val cY = 200.0
  val radius = 100.0
  children = Seq(
    new Rectangle {
      width = ScreenWidth
      height = ScreenHeight
      fill = Color.Green
    },
    new ImageView(logo){
      x <== imX
      y <== imY
    }
  )

  val NanoPerSec = 1.0e9
  final case class FrameRate(initTime: Long, lastTime: Long = 0L, frames: Long = 0L) {
    // Convert to time in seconds
    def totalTime: Double = if(frames == 0L) 1.0 else (lastTime - initTime) / NanoPerSec

    def mean: Double = frames / totalTime
    def update(time: Long) = copy(lastTime = time, frames = frames + 1)
  }

  // Current frame rate.
  var frames: Option[FrameRate] = None
  val animateTimer = AnimationTimer {t =>

    // Update frame rate.
    frames = Some(frames.fold(FrameRate(t))(_.update(t)))

    // Change the position of the image. We'll make the image move around a circle
    // clockwise, doing 1 revolution every 10 seconds. The center of the circle will be
    // (cX, cY). The angle is therefore the modulus of the time in seconds divided by 10
    // as a proportion of 2 pi radians.
    val angle = (frames.get.totalTime % 10.0) * 2.0 * Math.PI / 10.0

    // Update X and Y co-ordinates related to the center and angle.
    imX.value = cX + radius * Math.cos(angle)
    imY.value = cY + radius * Math.sin(angle)
  }

  animateTimer.start()
}
