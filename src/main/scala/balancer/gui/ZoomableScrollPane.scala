package balancer.gui

import scalafx.geometry.Point2D
import scalafx.geometry.Pos
import scalafx.scene.{Group, Node}
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.VBox


class ZoomableScrollPane(var target: Node) extends ScrollPane {
  private var scaleValue = 1.1
  private val zoomIntensity = 0.02

  var zoomNode: Group = new Group(target)

  content = {
    val centered = new VBox(zoomNode) {
      alignment = Pos.Center
    }
    centered.setOnScroll(e => {
      e.consume()
      onScroll(e.getTextDeltaY, new Point2D(e.getX, e.getY))
    })
    centered
  }
  pannable = true
  hbarPolicy = ScrollPane.ScrollBarPolicy.Never
  vbarPolicy = ScrollPane.ScrollBarPolicy.Never
  fitToHeight = true
  fitToWidth = true
  updateScale()

  private def updateScale(): Unit = {
    target.setScaleX(scaleValue)
    target.setScaleY(scaleValue)
  }

  private def onScroll(wheelDelta: Double, mousePoint: Point2D): Unit = {
    val zoomFactor = Math.exp(wheelDelta * zoomIntensity)
    val innerBounds = zoomNode.getLayoutBounds
    val viewportBounds = this.getViewportBounds
    // calculate pixel offsets from [0, 1] range
    val valX = this.getHvalue * (innerBounds.getWidth - viewportBounds.getWidth)
    val valY = this.getVvalue * (innerBounds.getHeight - viewportBounds.getHeight)
    scaleValue = scaleValue * zoomFactor
    updateScale()
    this.layout() // refresh ScrollPane scroll positions & target bounds

    // convert target coordinates to zoomTarget coordinates
    val posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint))
    // calculate adjustment of scroll position (pixels)
    val adjustment = target.getLocalToParentTransform.deltaTransform(posInZoomTarget.multiply(zoomFactor - 1))
    // convert back to [0, 1] range
    // (too large/small values are automatically corrected by ScrollPane)
    val updatedInnerBounds = zoomNode.getBoundsInLocal
    this.setHvalue((valX + adjustment.getX) / (updatedInnerBounds.getWidth - viewportBounds.getWidth))
    this.setVvalue((valY + adjustment.getY) / (updatedInnerBounds.getHeight - viewportBounds.getHeight))
  }
}
