package game

import game.grid.Grid
import game.objects.{Player, Scale}


class Game (val numRounds: Int = 5, val weightsPerRound: Int = 10, base_scale_radius: Int = 5) {
  val grid = new Grid(20,40)
  var factory = Factory(this, base_scale_radius)
  val fileManager = new FileManager(this)
  var currentRound = 1
  var currentTurn: String = "" // name of the player

  // Factory methods interface
  def baseScale = factory.baseScale
  def players = factory.players
  def scaleWithCode(code: Char) = factory.scaleWithCode(code)
  def reset() = factory.reset()
  //

  // Recursive function for get all scales
  private def _scales(scale: Scale): Vector[Scale] =
    scale.scales.map(_scales(_)).flatMap(_.toList).appended(scale)
  def scales = _scales(baseScale)

  // Winners
  def winner = players.maxBy(_.score)
  def finalWinner = players.maxBy(_.roundWon)
}
