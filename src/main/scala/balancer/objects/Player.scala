package balancer.objects

import balancer.State
import balancer.objects.Command.placeWeight
import balancer.utils.Constants.MaxRandomFind
import balancer.utils.Helpers.randomColor
import scalafx.beans.property.{DoubleProperty, IntegerProperty}
import scalafx.scene.paint.Color

import scala.util.Random

/**
 * The Player trait represent players in the game
 * (Human or Bot)
 */
sealed trait Player extends GameObject {
  val name: String

  // Property for binding with UI elements
  val propScore: IntegerProperty = IntegerProperty(0)
  val propRoundWon: IntegerProperty = IntegerProperty(0)
  private var _color: Color = randomColor()
  def color = _color
  def color_=(value: Color) = _color = value


  /**
   * The code that will be shown on the grid. Unique
   * for each player.
   * Note: The player's code is alway upper-cased, compared to
   * the scale's code, which is always upper-cased
   * @return
   */
  def playerCode: Char = name(0).toUpper

  /**
   * The score of the player, recursively calculated from
   * the base scale.
   *
   * @return
   */
  def score: Int = state.baseScale.score(this)

  /**
   * The number of round the player has won.
   *
   * @return
   */
  def roundWon = propRoundWon.value

  def incRoundWon() = propRoundWon.update(propRoundWon.value + 1)

  override def toString: String = name
}

/**
 * A human player will be controlled manually, turn by turn
 *
 * @param name  the name of the human
 * @param state the game's state
 */
case class Human(val name: String, val state: State) extends Player

/**
 * A bot player will be controlled automatically, using a mixture
 * of randomized algorithm to search for feasible moves and a brute
 * force algorithm to find the best move. Both algorithm will be mixed together
 * using a random number generator to determine the bot move.
 *
 * @param name  the name of the bot
 * @param state the game's state
 */
case class Bot(val name: String, val state: State) extends Player {

  // Property for binding with UI elements
  val difficultyProp = DoubleProperty(state.game.botDifficulty)

  def difficulty = difficultyProp.value

  /**
   * Find the best move by exhaustively searching in the feasible space
   * (guaranteeing not flipping a scale) and testing each move by faking
   * the move and record the resulting score. The best move is chosen
   * to maximize the obtained points in that turn. (looking 1 step-ahead)
   */
  def bestMove(): Unit = {
    var best_score = -1
    var best_pos = 0
    var best_scale: Scale = null

    def update(scale: Scale, pos: Int): Unit = {
      val currentScore = score
      if (currentScore > best_score && scale.isBalanced) {
        best_score = currentScore
        best_scale = scale
        best_pos = pos
      }
    }

    for (scale <- state.scalesVector) {
      for (idx <- 0 until 2 * scale.radius + 1) {
        val pos = idx - scale.radius
        if (pos != 0) {
          scale(pos) match {
            case Some(s: Scale) =>
            case _ =>
              val command = placeWeight(this, pos, scale, state)
              command.execute()
              if (state.flippedScales.isEmpty) {
                update(scale, pos)
              }
              command.undo()
          }
        }
      }
    }

    if (best_pos == 0 || best_scale == null || best_score == -1) {
      random()
    } else
      state.execute(placeWeight(this, best_pos, best_scale, state))
  }

  /**
   * A random move is picked in the feasible space
   * (guranteeing not flipping a scale).
   */
  def random(): Unit = {
    val scales = state.scalesVector
    var pos = 0
    var scale: Scale = null
    var command: Command = null
    var randomFindCount = 0

    while (pos == 0 && randomFindCount < MaxRandomFind) {
      scale = scales(Random.nextInt(scales.length))
      pos = Random.between(-scale.radius, scale.radius)
      if (pos != 0) {
        command = placeWeight(this, pos, scale, state)
        scale(pos) match {
          case Some(s: Scale) => pos = 0
          case _ =>
            command.execute()
            if (state.flippedScales.nonEmpty) {
              command.undo()
              pos = 0
            }
        }
      }
      randomFindCount += 1
    }
    state.execute(command, addOnly = true)
  }
}

