package game

import game.grid.Grid


class Game (var numRounds: Int = 3, var weightsPerRound: Int = 10, var baseScaleRadius: Int = 5) {
  var state = new State(this)
  val fileManager = new FileManager(this)
  val grid = Grid(this)

  // Game over
  var over = false

  // Winners
  def winner = state.players.maxBy(_.score)
  def finalWinner = state.players.maxBy(_.roundWon)
}
