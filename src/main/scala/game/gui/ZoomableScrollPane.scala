package game.gui

import javafx.geometry.Point2D
import scalafx.geometry.Pos
import scalafx.scene.{Group, Node}
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.VBox


class ZoomableScrollPane(var target: Node) extends ScrollPane {
  private var scaleValue = 0.7
  private val zoomIntensity = 0.02
  var zoomNode: Group = new Group(target)
  this.setContent(outerNode(zoomNode))
  this.setPannable(true)
  this.setHbarPolicy(ScrollPane.ScrollBarPolicy.Never)
  this.setVbarPolicy(ScrollPane.ScrollBarPolicy.Never)
  this.setFitToHeight(true)
  this.setFitToWidth(true)
  updateScale()

  private def outerNode(node: Node) = {
    val outerNode = centeredNode(node)
    outerNode.setOnScroll((e) => {
      e.consume()
      onScroll(e.getTextDeltaY, new Point2D(e.getX, e.getY))
    })
    outerNode
  }

  private def centeredNode(node: Node) = {
    val vBox = new VBox(node)
    vBox.setAlignment(Pos.Center)
    vBox
  }

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
