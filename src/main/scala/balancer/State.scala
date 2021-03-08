package balancer

import balancer.objects.Command.placeWeight
import balancer.objects._
import balancer.utils.Constants.{MAXRANDOMFIND, MaxUndo}
import balancer.utils.OccupiedPosition
import scalafx.scene.AccessibleRole.RadioButton

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class State(val game: Game) {


  // CREATE NEW OBJECT
  private var scaleIndex: Char = 96
  def setScaleIndex(code: Char) = scaleIndex = code

  def nextScaleCode(): Char = {
    scaleIndex = (scaleIndex.toInt + 1).toChar;
    scaleIndex
  }

  // STATE
  var currentRound = 1
  var currentTurnIdx = 0

  def currentTurn = players(currentTurnIdx)

  var weightLeftOfRound = game.weightsPerRound
  //
  val baseScale = new Scale(null, 0, game.baseScaleRadius, nextScaleCode(), this)
  val players = ArrayBuffer[Player]()

  def humans = players.flatMap { case p: Human => Some(p) case _ => None }

  def bots = players.flatMap { case p: Bot => Some(p) case _ => None }

  def scaleWithCode(code: Char) = scales.find(_.code == code)

  def flippedScales = scales.filterNot(_.isBalanced)

  def deleteFlippedScale(): Unit = {
    flippedScales.foreach(s => {
      if (s == baseScale) {
        game.over = true
        return
      } else s.parentScale.remove(s.pos)
    })
  }

  def buildWeight(pos: Int, parentScale: Scale, owner: Option[Player] = None) = {
    if (pos == 0) throw new Exception("Position must not be 0")
    parentScale(pos) match {
      case Some(scale: Scale) => throw new OccupiedPosition
      case Some(stack: Stack) =>
        val newWeight = new Weight(stack, this, owner)
        stack.append(newWeight)
        stack
      case None =>
        val newStack = new Stack(parentScale, pos, this)
        val newWeight = new Weight(newStack, this, owner)
        newStack.append(newWeight)
        parentScale(pos) = newStack
        newStack
    }
  }

  def buildScale(pos: Int, radius: Int, parentScale: Scale, scaleCode: Option[Char] = None) = {
    val newScale = new Scale(parentScale, pos, radius, scaleCode.getOrElse(nextScaleCode()), this)
    parentScale(pos) = newScale
    newScale
  }

  def buildWildScale(): Unit = {
    val scaleCode = nextScaleCode()
    // Picking parent scales at level i, prioritize lower level scales
    // We are trying to place scale at level i+1
    var found = false
    for (i <- 0 to maxScaleLevel) {
      var randomFindCount = 0
      val scalesAtLevelI = scalesAtLevel(i)
      val scalesAtLevelI1 = scalesAtLevel(i + 1)
      def findScale(): Unit = {
        while(randomFindCount <= MAXRANDOMFIND){
          val parentScale = scalesAtLevelI(Random.nextInt(scalesAtLevelI.length))
          val openPos = parentScale.openPos
          if(openPos.length > 0){
            val pos = openPos(Random.nextInt(openPos.length))
            val aRadius = Random.between(1, baseScale.radius+1)
            val newScale = new Scale(parentScale, pos, aRadius, scaleCode, this)
            if(!(scalesAtLevelI1.exists(_.isOverLapWith(newScale)))) {
              parentScale(pos) = newScale
              found = true
              return
            }
            randomFindCount += 1
          }
        }
      }

      findScale()
      if(found) return
    }
  }

  def buildWildWeight(): Unit = {
    val cachedScales = scales
    var pos = 0
    var scale: Scale = null
    var command: Command = null
    var randomFindCount = 0

    while(pos == 0 && randomFindCount < MAXRANDOMFIND){
      scale = cachedScales(Random.nextInt(cachedScales.length))
      pos = Random.between(-scale.radius, scale.radius)
      if(pos != 0) {
        scale(pos) match {
          case Some(s: Scale) => pos = 0
          case _ =>
            val stack = buildWeight(pos, scale, None)
            if (flippedScales.nonEmpty){
              stack.pop()
              pos = 0
            }
        }
      }
      randomFindCount += 1
    }
  }


  def buildBot(name: String) = {
    val newBot = new Bot(name, this)
    players.append(newBot)
    newBot
  }

  def buildHuman(name: String) = {
    val newHuman = new Human(name, this)
    players.append(newHuman)
    newHuman
  }

  // Recursive function to get all scalesVector
  private def _scales(root_scale: Scale): Vector[Scale] =
    root_scale.scalesVector.map(_scales).flatMap(_.toList).appended(root_scale)

  def scales = _scales(baseScale)

  // Note: inefficient but easy to understand
  def scalesAtLevel(level: Int) = scales.filter(_.level == level)

  def maxScaleLevel = scales.map(_.level).max

  def removePlayer(player: Player) = {
    for (scale <- scales) {
      for (stack <- scale.stacksVector) {
        stack.filterOut(player)
      }
    }
    players.remove(players.indexOf(player))
  }

  // Undo player move
  private val undoStack = scala.collection.mutable.Stack[Command]()
  private val redoStack = scala.collection.mutable.Stack[Command]()

  def execute(command: Command) = {
    if (redoStack.nonEmpty) redoStack.clear()
    undoStack.push(command.execute())
    if (undoStack.length >= MaxUndo) undoStack.dropRightInPlace(1)
  }

  def addExecuted(command: Command) = {
    if (redoStack.nonEmpty) redoStack.clear()
    undoStack.push(command)
    if (undoStack.length >= MaxUndo) undoStack.dropRightInPlace(1)
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


