package balancer.gui

import balancer.Game
import scalafx.scene.control.SplitPane

/*
  The main area, below the menu bar on top. This will store the canvas
  and the right side pane (InfoPane)
 */
class MidSplitPane(private val game: Game) extends SplitPane {
  private var infoPane = new InfoPane(game)
  private val scrollPane = new GameCanvas(this, game)

  def gc = scrollPane.gc

  def updateInfoPane() = infoPane.updateContent()
  def drawCanvas() = scrollPane.draw()

  items.addAll(scrollPane, infoPane)
  setDividerPosition(0, 0.75)
}