import game.{ConsoleManager, Game}

object MainText extends App {
  val game = new Game(2)
  val savedfile = if(args.nonEmpty) args(0) else null
//  game.fileManager.load_game(savedfile)
//  game.fileManager.save_game("writtenfile.txt")
  (new ConsoleManager(game)).run(savedfile)
}
