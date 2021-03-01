import game.{ConsoleManager, Game}

object MainText extends App {
  val game = new Game(numRounds=3, weightsPerRound=20)
  val savedfile = if(args.nonEmpty) args(0) else null
  (new ConsoleManager(game)).run(savedfile)
//  game.fileManager.loadGame(savedfile)
//  game.fileManager.saveGame("writtenfile.txt")
}
