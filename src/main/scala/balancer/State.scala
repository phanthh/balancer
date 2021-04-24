package balancer

import balancer.objects._
import balancer.utils.Constants.{MaxRandomFind, MaxUndo}
import balancer.utils.Helpers.{clamp, loop}
import balancer.utils.OccupiedPosition

import scala.collection.mutable.{ArrayBuffer, HashSet}
import scala.util.Random

/**
 * The game's state.
 *
 * @param game the Game object
 */
class State(val game: Game) {
  /**
   * Scale code iterator
   */
  val sc = new Iterator[Char]{
    private var charCounter: Char = ('a' - 1).toChar
    override def hasNext: Boolean = charCounter < 'z'
    override def next(): Char = {
      charCounter = (charCounter.toInt + 1).toChar
      charCounter
    }
    def set(code: Char) = charCounter = code
  }

  /**
   * The bottom-most scale.
   */
  val baseScale = new Scale(null, 0, game.baseScaleRadius, sc.next(), this)
  val players = ArrayBuffer[Player]()


  ///////
  private var _currentRound = 1 // The round starts at 1
  def currentRound = _currentRound
  def currentRound_=(value: Int) = {
    _currentRound = clamp(value, 0, game.numRounds)
  }
  private var _currentTurnIdx = 0 // The first turn is 0
  def currentTurnIdx = _currentTurnIdx
  def currentTurnIdx_=(value: Int) = {
    _currentTurnIdx = loop(value, players.length)
  }

  private var _weightLeftOfRound = game.weightsPerRound
  def weightLeftOfRound = _weightLeftOfRound
  def weightLeftOfRound_=(value: Int) = {
    _weightLeftOfRound = clamp(value, 0, game.weightsPerRound)
  }


  def humans = players.flatMap { case p: Human => Some(p) case _ => None }.toVector

  def bots = players.flatMap { case p: Bot => Some(p) case _ => None }.toVector
  ///////

  /**
   * A hash set for quick retrival of scales. Linked for reliability and
   * ease of debugging.
   */
  private val scales = HashSet[Scale](baseScale)

  def scalesVector = scales.toVector

  def flippedScales = scalesVector.filterNot(_.isBalanced)

  def currentTurn = players(currentTurnIdx)

  def findScale(code: Char) = scalesVector.find(_.code == code)

  def scalesAtLevel(level: Int) = scalesVector.filter(_.level == level)

  def maxScaleLevel = scalesVector.map(_.level).max

  /**
   * Delete all scale that are flipped (unbalance)
   */
  def deleteFlippedScale(): Unit = {
    flippedScales.foreach(s => {
      if (s == baseScale) {
        game.over = true
        return
      } else {
        removeScale(s)
      }
    })
  }

  /**
   * Remove scale recursively
   * @param s the scale to remove
   */
  def removeScale(s: Scale): Unit = {
    scales.remove(s)
    s.parentScale.remove(s.pos)
    s.scalesVector.foreach(removeScale(_))
  }
  ///////

  /**
   * Build a scale and adding it to the game's state
   * @param pos position on the parentScale
   * @param radius radius of the new scale
   * @param parentScale which scale to place the new scale on
   * @param scaleCode the code of the new Scale
   * @return
   */
  def buildScale(pos: Int, radius: Int, parentScale: Scale, scaleCode: Option[Char] = None): Option[Scale] = {
    if(!sc.hasNext) return None
    val newScale = new Scale(parentScale, pos, radius, scaleCode.getOrElse(sc.next()), this)
    parentScale(pos) = newScale
    scales.add(newScale)
    Some(newScale)
  }

  /**
   * Build a random scale.
    */
  def buildRandomScale(): Unit = {
    if(!sc.hasNext) return
    val scaleCode = sc.next()
    // Picking parent scalesVector at level i, prioritize lower level scalesVector
    // We are trying to place scale at level i+1
    var found = false
    for (i <- 0 to maxScaleLevel) {
      val scalesAtLevelI = scalesAtLevel(i)
      val scalesAtLevelI1 = scalesAtLevel(i + 1)

      def findScale(): Unit = {
        var randomFindCount = 0
        while (randomFindCount <= MaxRandomFind) {
          val parentScale = scalesAtLevelI(Random.nextInt(scalesAtLevelI.length))
          val openPos = parentScale.openPos
          if (openPos.nonEmpty) {
            val pos = openPos(Random.nextInt(openPos.length))
            val aRadius = Random.between(1, baseScale.radius + 1)
            val newScale = new Scale(parentScale, pos, aRadius, scaleCode, this)
            if (!(scalesAtLevelI1.exists(_.isOverLapWith(newScale)))) {
              scales.add(newScale)
              parentScale(pos) = newScale
              found = true
              return
            }
          }
          randomFindCount += 1
        }
      }

      findScale()
      if (found) return
    }
  }

  /**
   * Build a random weight
   */
  def buildRandomWeight(): Unit = {
    var pos = 0
    var scale: Scale = null
    var command: Command = null
    var randomFindCount = 0

    while (pos == 0 && randomFindCount < MaxRandomFind) {
      scale = scalesVector(Random.nextInt(scalesVector.length))
      pos = Random.between(-scale.radius, scale.radius)
      if (pos != 0) {
        scale(pos) match {
          case Some(s: Scale) => pos = 0
          case _ =>
            val stack = buildWeight(pos, scale, None)
            if (flippedScales.nonEmpty) {
              stack.pop()
              pos = 0
            }
        }
      }
      randomFindCount += 1
    }
  }

  /**
   * Build a weight and add it to the game's state.
   * A stack will be created if needed.
   * @param pos the position on the parentScale
   * @param parentScale the scale to place the new weight on
   * @param owner the owner of this new weight
   */
  def buildWeight(pos: Int, parentScale: Scale, owner: Option[Player] = None) = {
    if (pos == 0) throw new Exception("Position must not be 0")
    parentScale(pos) match {
      case Some(scale: Scale) => throw new OccupiedPosition
      case Some(stack: Stack) =>
        val newWeight = new Weight(stack, this, owner)
        stack.push(newWeight)
        stack
      case None =>
        val newStack = new Stack(parentScale, pos, this)
        val newWeight = new Weight(newStack, this, owner)
        newStack.push(newWeight)
        parentScale(pos) = newStack
        newStack
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

  /**
   * Remove a player from the game's state.
   * and delete all the weight of the player.
   * @param player player to delete
   */
  def removePlayer(player: Player) = {
    for (scale <- scalesVector) {
      for (stack <- scale.stacksVector) {
        stack.removePlayer(player)
      }
    }
    players.remove(players.indexOf(player))
  }


  //////// Undo and Redo

  private val undoStack = scala.collection.mutable.Stack[Command]()
  private val redoStack = scala.collection.mutable.Stack[Command]()

  def execute(command: Command, addOnly: Boolean = false) = {
    if (redoStack.nonEmpty) {
      redoStack.clear()
    }
    if(addOnly){
      undoStack.push(command)
    } else {
      undoStack.push(command.execute())
    }
    if (undoStack.length >= MaxUndo) {
      undoStack.dropRightInPlace(1)
    }
  }

  def undoable = undoStack.nonEmpty
  def redoable = redoStack.nonEmpty

  def undo(): Unit = {
    if (undoable) {
      redoStack.push(undoStack.pop().undo())

      currentTurnIdx += 1
      weightLeftOfRound += 1
    }
  }

  def redo(): Unit = {
    if (redoable) {
      undoStack.push(redoStack.pop().execute())

      currentTurnIdx -= 1
      weightLeftOfRound -= 1
    }
  }
}
