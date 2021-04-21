package balancer.gui

import balancer.Game
import scalafx.scene.control.SplitPane

/*
  The main area, below the menu bar on top. This will store the canvas
  and the right side pane (InfoPane)
 */
class MainPane(private val game: Game) extends SplitPane {
  var sidePane = new InfoPane(game)
  var gameCanvas = new GameCanvas(this, game)

  def gc = gameCanvas.gc

  def updateSidePane() = sidePane.updateContent()
  def drawCanvas() = gameCanvas.draw()
  def toggleGrid() = gameCanvas.toggleGrid()

  items.addAll(gameCanvas, sidePane)
  setDividerPosition(0, 0.75)
}