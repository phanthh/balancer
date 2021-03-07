package balancer.gui

import balancer.Game
import balancer.gui.MainGUI.{draw, midSplitPane, initScene}
import scalafx.scene.control._

class TopMenuBar(private val game: Game) extends MenuBar {
  def state = game.state

  def defaultFile = game.fileManager.defaultFile

  def save(filePath: String) = game.fileManager.saveGame(filePath)

  def load(filePath: String) = game.fileManager.loadGame(filePath)

  menus = List(
    new Menu("File") {
      items = List(
        new MenuItem("New") {
          onAction = _ => {
            game.reset()
            load(defaultFile)
            initScene()
          }

        },
        new MenuItem("Open...") {
          onAction = _ => {
            PromptGUI.openDialog(
              success = (f) => {
                game.reset()
                load(f.getAbsolutePath)
                initScene()
              },
              failed = () => {}
            )
          }
        },
        new SeparatorMenuItem(),
        new MenuItem("Save...") {
          onAction = _ =>
            PromptGUI.saveDialog(
              success = (f) => {
                save(f.getAbsolutePath)
              },
              failed = () => {}
            )
        },
        new SeparatorMenuItem(),
        new MenuItem("Exit") {
          onAction = _ =>
            PromptGUI.uWannaSaveDialog(
              reason = "Exiting Confirmation",
              yes = () => {
                PromptGUI.saveDialog(
                  success = (f) => {
                    save(f.getAbsolutePath)
                  },
                  failed = () => {}
                )
                sys.exit(0)
              },
              no = () => sys.exit(0),
            )
        }
      )
    },
    new Menu("Edit") {
      items = List(
        new MenuItem("Add Human") {
          onAction = _ =>
            PromptGUI.askNameDialog("Adding Human Player") match {
              case Some(name) => {
                state.buildHuman(name)
                initScene()
              }
              case None =>
            }
        },
        new MenuItem("Add Bot") {
          onAction = _ =>
            PromptGUI.askNameDialog("Adding Bot Player") match {
              case Some(name) => {
                state.buildBot(name)
                initScene()
              }
              case None =>
            }
        },
        new SeparatorMenuItem,
        new MenuItem("Undo") {
          onAction = _ => {
            state.undo()
            midSplitPane.infoPane.updateGUI()
            draw()
          }
        },
        new MenuItem("Redo") {
          onAction = _ => {
            state.redo()
            midSplitPane.infoPane.updateGUI()
            draw()
          }
        },
      )
    },
  )

}
