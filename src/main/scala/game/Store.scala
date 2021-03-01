package game

import game.objects._

import scala.collection.mutable.ArrayBuffer

object Store {
  def apply(game: Game, base_scale_radius: Int = 5) = new Store(game, base_scale_radius)
}

private class Store(private val game: Game, base_scale_radius: Int) {

  private var _scaleCode: Char = 96
  def nextScaleCode(): Char = {_scaleCode = (_scaleCode.toInt + 1).toChar; _scaleCode}

  var baseScale = new Scale(null, 0, base_scale_radius, nextScaleCode(), this)
  val players = ArrayBuffer[Player]()

  def scaleWithCode(code: Char) = scales.find(_.scale_code == code)

  def reset(): Unit = {
    _scaleCode = 96
  }

  def buildWeight(pos: Int, parent_scale: Scale, owner: Option[Player] = None, soft_append: Boolean = false): Weight = {
    if(pos == 0) throw new Exception("Position must not be 0")
    parent_scale.at(pos) match {
      case Some(scale: Scale) => throw new Exception("Occupied")
      case Some(stack: Stack) =>
        val newWeight = new Weight(stack,this, owner)
        if(soft_append)
          stack.softAppend(newWeight)
        else
          stack.append(newWeight)
        newWeight
      case None =>
        val newStack = new Stack(parent_scale, pos, this)
        val newWeight = new Weight(newStack, this, owner)
        if(soft_append)
          newStack.softAppend(newWeight)
        else
          newStack.append(newWeight)
        parent_scale.put(pos, newStack)
        newWeight
    }
  }

  def buildScale(pos: Int, radius: Int, parent_scale: Scale, scale_code: Option[Char] = None): Scale = {
    val newScale = new Scale(parent_scale, pos, radius, scale_code.getOrElse(nextScaleCode()), this)
    parent_scale.put(pos, newScale)
    newScale
  }

  def buildBot(name: String): Bot = {
    val newBot = new Bot(name, this)
    players.append(newBot)
    newBot
  }

  def buildHuman(name: String): Human = {
    val newHuman = new Human(name, this)
    players.append(newHuman)
    newHuman
  }

  // Recursive function to get all scalesVector
  private def _scales(root_scale: Scale): Vector[Scale] =
    root_scale.scalesVector.map(_scales).flatMap(_.toList).appended(root_scale)
  def scales = _scales(baseScale)
}
