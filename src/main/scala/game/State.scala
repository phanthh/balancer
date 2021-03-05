package game

import game.objects._

import scala.collection.mutable.ArrayBuffer

class State(private val game: Game) {

  // CREATE NEW OBJECT
  private var _scaleCode: Char = 96
  def nextScaleCode(): Char = {_scaleCode = (_scaleCode.toInt + 1).toChar; _scaleCode }
  def reset(): Unit = {_scaleCode = 96}

  // STATE
  var currentRound = 1
  var currentIdx = 0
  var baseScale = new Scale(null, 0, game.baseScaleRadius, nextScaleCode(), this)
  val players = ArrayBuffer[Player]()
  def scaleWithCode(code: Char): Option[Scale] = scales.find(_.scale_code == code)


  def buildWeight(pos: Int, parentScale: Scale, owner: Option[Player] = None): (Weight, Stack) = {
    if (pos == 0) throw new Exception("Position must not be 0")
    parentScale(pos) match {
      case Some(scale: Scale) => throw new Exception("Occupied")
      case Some(stack: Stack) =>
        val newWeight = new Weight(stack, this, owner)
        stack.append(newWeight)
        (newWeight, stack)
      case None =>
        val newStack = new Stack(parentScale, pos, this)
        val newWeight = new Weight(newStack, this, owner)
        newStack.append(newWeight)
        parentScale(pos) = newStack
        (newWeight, newStack)
    }
  }

  def buildScale(pos: Int, radius: Int, parentScale: Scale, scaleCode: Option[Char] = None): Scale = {
    val newScale = new Scale(parentScale, pos, radius, scaleCode.getOrElse(nextScaleCode()), this)
    parentScale(pos) = newScale
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


  // UNDO PLAYER MOVE
  val undoStack = scala.collection.mutable.Stack[Command]()
  def undo(): Command = undoStack.pop().undo()
}


