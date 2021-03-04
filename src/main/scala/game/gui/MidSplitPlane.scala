package game.gui

import game.Game
import game.gui.Constants.{Height, Width}
import game.gui.MainGUI.{draw, setup}
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.SplitPane

class MidSplitPlane(val game: Game) extends SplitPane {

  val canvas = new Canvas(Width, Height)
  val gc = canvas.graphicsContext2D
  val infoPane = new InfoPane(gc, game)
  setup(gc)
  draw(gc)

  val scrollPane = new ZoomableScrollPane(canvas)
  this.items.addAll(scrollPane, infoPane)
  this.setDividerPosition(0, 0.75)
}

