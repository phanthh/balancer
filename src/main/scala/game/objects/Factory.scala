package game.objects

import game.Game
import game.objects.Factory._

object Factory {
  val WEIGHT = "w"
  val SCALE = "s"
  val STACK = "t"
  val PLAYER = "p"

  def apply(game: Game) = new Factory(game)
}

class Factory(private val game: Game) {

  private var _idCounter = 0
  private var _scaleCode: Char = 96
  private var _playerCode: Char = 96
  private def nextID(types: String): String = {_idCounter += 1; types + _idCounter.toString}
  private def nextScaleCode(): Char = {_scaleCode = (_scaleCode.toInt + 1).toChar; _scaleCode}

  def build_weight(pos: Int, parent_scale: Scale, owner: Option[Player] = None): Unit = {
    parent_scale.object_at(pos) match {
      case Some(scale: Scale) => throw new Exception("Occupied")
      case Some(stack: Stack) =>
        val newID = nextID(WEIGHT)
        val newWeight = new Weight(stack, newID, game, owner)
        stack.append(newWeight)
        game.register(newID -> newWeight)
      case None =>
        val newStackID = nextID(STACK)
        val newStack = new Stack(parent_scale, pos, newStackID, game)
        val newWeightID = nextID(WEIGHT)
        val newWeight = new Weight(newStack, newWeightID, game, owner)
        parent_scale.place_at(pos, newStack)
        game.register(newStackID -> newStack)
        game.register(newWeightID -> newWeight)
    }
  }

  def build_scale(radius: Int): Unit = {
    val newScale = new Scale(null, 0, radius, nextScaleCode(), nextID(SCALE), game)
    game.register(newScale.id -> newScale)
  }

  def build_scale(pos: Int, radius: Int, parent_scale: Scale): Unit = {
    val newScale = new Scale(parent_scale, pos, radius, nextScaleCode(), nextID(SCALE), game)
    parent_scale.place_at(pos, newScale)
    game.register(newScale.id -> newScale)
  }

  def build_bot(name: String): Unit = {
    val newPlayer = new Bot(name, nextID(PLAYER), game)
    game.register(newPlayer.id -> newPlayer)
  }

  def build_human(name: String): Unit = {
    val newPlayer = new Human(name, nextID(PLAYER), game)
    game.register(newPlayer.id-> newPlayer)
  }
}
