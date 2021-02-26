package game

import game.objects._

import scala.collection.mutable.ArrayBuffer

object Factory {
  def apply(game: Game, base_scale_radius: Int = 5) = new Factory(game, base_scale_radius)
}

class Factory(private val game: Game, base_scale_radius: Int) {

  private var _scaleCode: Char = 96
  def nextScaleCode(): Char = {_scaleCode = (_scaleCode.toInt + 1).toChar; _scaleCode}

  var baseScale = new Scale(null, 0, base_scale_radius, nextScaleCode(), game)
  val players = ArrayBuffer[Player]()

  def scaleWithCode(code: Char) = baseScale.scaleWithCode(code)

  def reset(): Unit = {
    _scaleCode = 96
  }

  def build_weight(pos: Int, parent_scale: Scale, owner: Option[Player] = None): Weight = {
    parent_scale.object_at(pos) match {
      case Some(scale: Scale) => throw new Exception("Occupied")
      case Some(stack: Stack) =>
        val newWeight = new Weight(stack, game, owner)
        stack.append(newWeight)
        newWeight
      case None =>
        val newStack = new Stack(parent_scale, pos, game)
        val newWeight = new Weight(newStack, game, owner)
        newStack.append(newWeight)
        parent_scale.place_at(pos, newStack)
        newWeight
    }
  }

  def build_scale(pos: Int, radius: Int, parent_scale: Scale, scale_code: Option[Char] = None): Scale = {
    val newScale = new Scale(parent_scale, pos, radius, scale_code.getOrElse(nextScaleCode()), game)
    parent_scale.place_at(pos, newScale)
    newScale
  }

  def build_bot(name: String): Bot = {
    val newBot = new Bot(name, game)
    players.append(newBot)
    newBot
  }

  def build_human(name: String): Human = {
    val newHuman = new Human(name, game)
    players.append(newHuman)
    newHuman
  }
}
