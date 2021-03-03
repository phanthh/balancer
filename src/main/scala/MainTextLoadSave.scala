import game.{ConsoleManager, Game}

object MainTextLoadSave extends App {
  val game = new Game()
  game.fileManager.loadGame("loadfile.txt")
  game.fileManager.saveGame("savefile.txt")
}
