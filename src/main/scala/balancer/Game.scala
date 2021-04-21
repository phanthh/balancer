package balancer

import balancer.grid.Grid

/**
 * The Game object store other components other than the State
 * of the game, such as the Grid and the FileManager. It also
 * store some basic settings of the game.
 * @param numRounds the number of rounds of each game
 * @param weightsPerRound the number of weights per round
 * @param baseScaleRadius the radius of the bottom-most scale
 * @param botDifficulty the default bot difficulty
 */
class Game(var numRounds: Int = 3, var weightsPerRound: Int = 10, var baseScaleRadius: Int = 5, var botDifficulty: Double = 0.3) {
  val fileManager = new FileManager(this)
  var state = new State(this)
  var grid = new Grid(this)
  var over = false

  /**
   * Fully reset the game
   */
  def reset() = {
    state = new State(this)
    grid = new Grid(this)
    over = false
  }

  def roundWinner = state.players.maxBy(_.score)
  def finalWinner = state.players.maxBy(_.roundWon)
}
