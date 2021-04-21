package balancer.gui

import balancer.Game
import scalafx.scene.control.SplitPane

class MainPane(private val game: Game) extends SplitPane {
  var sidePane = new InfoPane(this, game)
  var gameCanvas = new GameCanvas(this, game)

  // Interface to call its components' functions
  def updateSidePane() = sidePane.updateContent()
  def drawCanvas() = gameCanvas.draw()
  def toggleGrid() = gameCanvas.toggleGrid()

  items.setAll(gameCanvas, sidePane)
  setDividerPosition(0, 0.75)
}