package balancer.gui

import balancer.Game
import balancer.gui.MainGUI.{draw, setGameScene, setMenuScene}
import balancer.utils.Constants.{Abouts, GithubURL, GitlabURL, Rules, Version}
import balancer.utils.Helpers.openURL
import balancer.utils.Prompts
import balancer.utils.Prompts.showInfoDialog
import scalafx.scene.control._

class TopMenuBar(private val mainPane: MainPane, private val game: Game) extends MenuBar {
  private def state = game.state

  private def fm = game.fileManager

  private def updateInfoPane() = mainPane.updateInfoPane()

  menus = List(
    new Menu("File") {
      items = List(
        new MenuItem("New") {
          onAction = _ => {
            game.reset()
            fm.loadDefault()
            setGameScene()
          }
        },
        new MenuItem("Open...") {
          onAction = _ => {
            Prompts.openFileDialog(
              success = (f) => {
                game.reset()
                fm.loadGame(f.getAbsolutePath)
                setGameScene()
              },
              failed = () => {}
            )
          }
        },
        new SeparatorMenuItem(),
        new MenuItem("Save...") {
          onAction = _ =>
            Prompts.saveFileDialog(
              success = (f) => {
                fm.saveGame(f.getAbsolutePath)
              },
              failed = () => {}
            )
        },
        new SeparatorMenuItem(),
        new MenuItem("Back To Menu") {
          onAction = _ => {
            game.over = true
            setMenuScene()
          }
        },
        new SeparatorMenuItem(),
        new MenuItem("Exit") {
          onAction = _ =>
            Prompts.askSavingDialog(
              reason = "Exiting Confirmation",
              yes = () => {
                Prompts.saveFileDialog(
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
                setGameScene()
              }
              case None =>
            }
        },
        new MenuItem("Add Bot") {
          onAction = _ =>
            Prompts.askNameDialog("Adding Bot Player") match {
              case Some(name) => {
                state.buildBot(name)
                setGameScene()
              }
              case None =>
            }
        },
        new SeparatorMenuItem,
        new MenuItem("Undo") {
          onAction = _ => {
            if(!(game.over) && state.undoable){
              state.undo()
              updateInfoPane()
              draw()
            }
          }
        },
        new MenuItem("Redo") {
          onAction = _ => {
            if(!(game.over) && state.redoable) {
              state.redo()
              updateInfoPane()
              draw()
            }
          }
        },
        new MenuItem("Toggle Grid") {
          onAction = _ => {
            mainPane.toggleGrid()
            draw()
          }
        },
      )
    },
    new Menu("Help") {
      items = List(
        new MenuItem("Rules") {
          onAction = _ => {
            showInfoDialog(
              titleText = "Rules",
              header = s"Balancer version $Version",
              content = Rules
            )
          }
        },
        new SeparatorMenuItem,
        new MenuItem("Github") {
          onAction = _ => {
            openURL(GithubURL)
          }
        },
        new MenuItem("Gitlab") {
          onAction = _ => {
            openURL(GitlabURL)
          }
        },
        new SeparatorMenuItem,
        new MenuItem("About...") {
          onAction = _ => {
            showInfoDialog(
              titleText = "About",
              header = s"Balancer version $Version",
              content = Abouts
            )
          }
        }
      )
    }
  )
}
