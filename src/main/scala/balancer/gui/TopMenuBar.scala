package balancer.gui

import balancer.Game
import balancer.gui.MainGUI.{setGameScene, setMenuScene}
import balancer.utils.Constants._
import balancer.utils.Helpers.openURLInDefaultBrowser
import balancer.utils.Prompts
import balancer.utils.Prompts.showInfoDialog
import scalafx.scene.control._

class TopMenuBar(private val mainPane: MainPane, private val game: Game) extends MenuBar {
  private def state = game.state

  private def newGame(f: String = null) = {
    game.reset()
    if (f == null) {
      fm.loadDefault()
    } else {
      fm.loadGame(f)
    }
    setGameScene()
  }

  private def fm = game.fileManager

  menus = Seq(
    new Menu("File") {
      items = Seq(
        // "New": Reset the game
        new MenuItem("New") {
          onAction = _ => {
            if (!(game.over)) {
              Prompts.askSavingDialog(
                reason = "New game",
                yes = () => {
                  Prompts.saveFileDialog(
                    success = (f) => {
                      fm.saveGame(f.getAbsolutePath)
                      newGame()
                    },
                    failed = () => {}
                  )
                },
                no = () => {
                  newGame()
                })
            } else {
              newGame()
            }
          }
        },
        // "Open": Load new game from file
        new MenuItem("Open...") {
          onAction = _ => {
            Prompts.openFileDialog(
              success = (f) => {
                newGame(f.getAbsolutePath)
              },
              failed = () => {}
            )
          }
        },
        new SeparatorMenuItem(),
        // "Save": Save the game to file
        new MenuItem("Save...") {
          if (game.over) disable = true
          onAction = _ =>
            Prompts.saveFileDialog(
              success = (f) => {
                fm.saveGame(f.getAbsolutePath)
              },
              failed = () => {}
            )
        },
        new SeparatorMenuItem(),
        // "Back to Menu": Go back to the menu splash screen
        new MenuItem("Back To Menu") {
          if (game.over) disable = true
          onAction = _ => {
            Prompts.askSavingDialog(
              reason = "Back To Menu",
              yes = () => {
                Prompts.saveFileDialog(
                  success = (f) => {
                    fm.saveGame(f.getAbsolutePath)
                    setMenuScene()
                  },
                  failed = () => {}
                )
              },
              no = () => {
                setMenuScene()
              }
            )
          }
        },
        new SeparatorMenuItem(),
        // "Exit": Exit the game
        new MenuItem("Exit") {
          onAction = _ =>
            Prompts.askSavingDialog(
              reason = "Exiting Confirmation",
              yes = () => {
                Prompts.saveFileDialog(
                  success = (f) => {
                    sys.exit(0)
                    fm.saveGame(f.getAbsolutePath)
                  },
                  failed = () => {}
                )
              },
              no = () => sys.exit(0),
            )
        }
      )
    },
    new Menu("Edit") {
      items = Seq(
        // "Add Human": Add a new human player
        new MenuItem("Add Human") {
          if (game.over) disable = true
          onAction = _ =>
            Prompts.askNameDialog("Adding Human Player") match {
              case Some(name) => {
                state.buildHuman(name)
                setGameScene()
              }
              case None =>
            }
        },
        // "Add Human": Add a new bot player
        new MenuItem("Add Bot") {
          if (game.over) disable = true
          onAction = _ =>
            Prompts.askNameDialog("Adding Bot Player") match {
              case Some(name) => {
                state.buildBot(name)
                setGameScene()
              }
              case None =>
            }
        },
        // "Undo": Undo the previos moves
        new SeparatorMenuItem,
        new MenuItem("Undo") {
          onAction = _ => {
            if (!(game.over) && state.undoable) {
              state.undo()
              mainPane.updateSidePane()
              mainPane.drawCanvas()
            }
          }
        },
        // "Redo": Redo the previos undoed moves
        new MenuItem("Redo") {
          onAction = _ => {
            if (!(game.over) && state.redoable) {
              state.redo()
              mainPane.updateSidePane()
              mainPane.drawCanvas()
            }
          }
        },
        // "Toggle Grid": Toggling the grid on or off
        new MenuItem("Toggle Grid") {
          if (game.over) disable = true
          onAction = _ => {
            mainPane.toggleGrid()
            mainPane.drawCanvas()
          }
        },
      )
    },
    new Menu("Help") {
      items = Seq(
        // "Rules": Show the rules of the game
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
        // "Github": open the github link using the default browwer
        new MenuItem("Github") {
          onAction = _ => {
            openURLInDefaultBrowser(GithubURL)
          }
        },
        // "Gitlab": open the gitlab link using the default browwer
        new MenuItem("Gitlab") {
          onAction = _ => {
            openURLInDefaultBrowser(GitlabURL)
          }
        },
        new SeparatorMenuItem,
        // "About": showing the about page
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
