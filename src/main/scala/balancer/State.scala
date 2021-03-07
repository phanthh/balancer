package balancer

import balancer.objects._

import scala.collection.mutable.ArrayBuffer

class State(val game: Game) {

  private val MAXUNDO = 20

  // CREATE NEW OBJECT
  private var scaleIndex: Char = 96

  def nextScaleCode(): Char = {
    scaleIndex = (scaleIndex.toInt + 1).toChar; scaleIndex
  }

  // STATE
  var currentRound = 1
  var currentTurnIdx = 0
  def currentTurn = players(currentTurnIdx)

  var weightLeftOfRound = game.weightsPerRound
  //
  var baseScale = new Scale(null, 0, game.baseScaleRadius, nextScaleCode(), this)
  var players = ArrayBuffer[Player]()

  def scaleWithCode(code: Char) = scales.find(_.code == code)

  def flippedScales = scales.filterNot(_.isBalanced)

  def deleteFlippedScale(): Unit = {
    flippedScales.foreach(s => {
      if(s == baseScale){
        game.over = true
        return
      } else s.parentScale.remove(s.pos)
    })
  }

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
  val redoStack = scala.collection.mutable.Stack[Command]()

  def execute(command: Command) = {
    if (redoStack.nonEmpty) redoStack.clear()
    undoStack.push(command.execute())
    if (undoStack.length >= MAXUNDO) undoStack.dropRightInPlace(1)
  }

  def undo(): Unit = {
    if (undoStack.nonEmpty) {
      redoStack.push(undoStack.pop().undo())
    }
  }

  def redo(): Unit = {
    if (redoStack.nonEmpty) {
      redoStack.pop().execute()
    }
  }
}


