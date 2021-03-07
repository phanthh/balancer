package balancer.gui

import balancer.Game
import MainGUI.{createScene, draw}
import balancer.utils.Prompts
import scalafx.scene.control._

class TopMenuBar(private val friend: MidSplitPane, private val game: Game) extends MenuBar {
  private def state = game.state

  private def fm = game.fileManager

  private def updateInfoPane() = friend.updateInfoPane()

  menus = List(
    new Menu("File") {
      items = List(
        new MenuItem("New") {
          onAction = _ => {
            game.reset()
            fm.loadDefault()
            createScene()
          }

        },
        new MenuItem("Open...") {
          onAction = _ => {
            Prompts.openDialog(
              success = (f) => {
                game.reset()
                fm.loadGame(f.getAbsolutePath)
                createScene()
              },
              failed = () => {}
            )
          }
        },
        new SeparatorMenuItem(),
        new MenuItem("Save...") {
          onAction = _ =>
            Prompts.saveDialog(
              success = (f) => {
                fm.saveGame(f.getAbsolutePath)
              },
              failed = () => {}
            )
        },
        new SeparatorMenuItem(),
        new MenuItem("Exit") {
          onAction = _ =>
            Prompts.uWannaSaveDialog(
              reason = "Exiting Confirmation",
              yes = () => {
                Prompts.saveDialog(
                  success = (f) => {
                    fm.saveGame(f.getAbsolutePath)
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
            Prompts.askNameDialog("Adding Human Player") match {
              case Some(name) => {
                state.buildHuman(name)
                createScene()
              }
              case None =>
            }
        },
        new MenuItem("Add Bot") {
          onAction = _ =>
            Prompts.askNameDialog("Adding Bot Player") match {
              case Some(name) => {
                state.buildBot(name)
                createScene()
              }
              case None =>
            }
        },
        new SeparatorMenuItem,
        new MenuItem("Undo") {
          onAction = _ => {
            state.undo()
            updateInfoPane()
            draw()
          }
        },
        new MenuItem("Redo") {
          onAction = _ => {
            state.redo()
            updateInfoPane()
            draw()
          }
        },
      )
    },
  )
}
