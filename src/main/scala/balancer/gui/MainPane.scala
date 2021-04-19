package balancer.gui

import balancer.Game
import scalafx.scene.control.SplitPane

/*
  The main area, below the menu bar on top. This will store the canvas
  and the right side pane (InfoPane)
 */
class MainPane(private val game: Game) extends SplitPane {
  private var infoPane = new InfoPane(game)
  private val gamecanvas = new GameCanvas(this, game)

  def gc = gamecanvas.gc

  def updateInfoPane() = infoPane.updateContent()
  def drawCanvas() = gamecanvas.draw()

  items.addAll(gamecanvas, infoPane)
  setDividerPosition(0, 0.75)
}