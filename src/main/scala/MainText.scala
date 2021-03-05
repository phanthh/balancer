import game.{ConsoleManager, Game}

object MainText extends App {
  val game = new Game()
  val savedfile = if(args.nonEmpty) args(0) else null
  (new ConsoleManager(game)).run(savedfile)
//  game.fileManager.loadGame(savedfile)
//  game.fileManager.saveGame("writtenfile.txt")
}
