package balancer.gui

import balancer.Game
import scalafx.scene.control.SplitPane

/*
  The main area, below the menu bar on top. This will store the canvas
  and the right side pane (InfoPane)
 */
class MainPane(private val game: Game) extends SplitPane {
  var infoPane = new InfoPane(game)
  private val gameCanvas = new GameCanvas(this, game)
  def getGameCanvas = gameCanvas

  def gc = gameCanvas.gc

  def updateInfoPane() = infoPane.updateContent()
  def drawCanvas() = gameCanvas.draw()
  def toggleGrid() = gameCanvas.toggleGrid()

  items.addAll(gameCanvas, infoPane)
  setDividerPosition(0, 0.75)
}