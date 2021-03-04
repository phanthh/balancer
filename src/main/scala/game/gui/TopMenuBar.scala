package game.gui

import scalafx.scene.control.{Menu, MenuBar, MenuItem, SeparatorMenuItem}

class TopMenuBar extends MenuBar {
  this.menus = List(
    new Menu("File") {
      items = List(
        new MenuItem("New"),
        new MenuItem("Save"),
        new SeparatorMenuItem(),
        new MenuItem("Exit")
      )
    },
    new Menu("Edit") {
      items = List(
        new MenuItem("Add Human"),
        new MenuItem("Add Bot"),
        new SeparatorMenuItem(),
        new MenuItem("+ Difficulty"),
        new MenuItem("- Difficulty")
      )
    }
  )
}
