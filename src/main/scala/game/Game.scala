package game

import game.grid.Grid


class Game (val numRounds: Int = 3, val weightsPerRound: Int = 10, baseScaleRadius: Int = 5) {
  var factory = Store(this, baseScaleRadius)
  val fileManager = new FileManager(this)
  var currentRound = 1
  var currentTurn: String = "" // name of the player
  val grid = Grid(this)

  // Store methods interface
  def baseScale = factory.baseScale
  def players = factory.players
  def scaleWithCode(code: Char) = factory.scaleWithCode(code)
  def reset() = factory.reset()
  def scales = factory.scales
  //

  // Winners
  def winner = players.maxBy(_.score)
  def finalWinner = players.maxBy(_.roundWon)
}
