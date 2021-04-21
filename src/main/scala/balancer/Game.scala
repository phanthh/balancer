package balancer

import balancer.grid.Grid
import scalafx.beans.property.BooleanProperty


class Game (var numRounds: Int = 3, var weightsPerRound: Int = 10, var baseScaleRadius: Int = 5, var botDifficulty: Double = 0.3) {
  var state = new State(this)
  var grid = new Grid(this)
  val fileManager = new FileManager(this)

  // Game over
  var over = false
  def reset() = {
    state = new State(this)
    grid = new Grid(this)
    over = false
  }

  // Winners
  def winner = state.players.maxBy(_.score)
  def finalWinner = state.players.maxBy(_.roundWon)
}
