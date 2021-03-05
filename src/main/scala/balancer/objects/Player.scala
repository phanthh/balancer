package balancer.objects

import balancer.{Game, State}
import balancer.gui.MainGUI.randomColor
import balancer.objects.Command.placeWeight
import scalafx.beans.property.IntegerProperty
import scalafx.scene.paint.Color

import scala.util.Random

sealed trait Player
  extends GameObject {

  val name: String

  // BINDING POINT FOR GUI PROPS
  var propScore: IntegerProperty = IntegerProperty(0)
  var propRoundWon: IntegerProperty = IntegerProperty(0)
  var propColor: Color = randomColor()
  ///

  def playerCode: Char = name(0)
  def score: Int = state.baseScale.score(this)

  def roundWon = propRoundWon.value

  def win() = propRoundWon.update(propRoundWon.value + 1)

  override def toString: String = name
}

case class Human(val name: String, val state: State) extends Player

case class Bot(val name: String, val state: State)
  extends Player {

  def random(): Unit = {
    val scales = state.scales
    var pos = 0
    var scale: Scale = null
    var command: Command = null

    while(pos == 0){
      scale = scales(Random.nextInt(scales.length))
      pos = Random.between(-scale.radius, scale.radius)
      if(pos != 0) {
        command = placeWeight(this, pos, scale, state)
        scale(pos) match {
          case Some(s: Scale) => pos = 0
          case _ =>
            command.execute()
            if (!scale.isBalanced) {
              pos = 0
              command.undo()
            }
        }
      }
    }
    state.undoStack.append(command)
  }

  def bestMove(): Unit = {
    var best_score = -1
    var best_pos = 0
    var best_scale: Scale = null

    def update(scale: Scale, pos: Int): Unit = {
      val currentScore = score
      if(currentScore > best_score && scale.isBalanced) {
        best_score = currentScore
        best_scale = scale
        best_pos = pos
      }
    }

    for(scale <- state.scales){
      for(idx <- 0 until 2*scale.radius+1){
        val pos = idx - scale.radius
        if(pos != 0){
          scale(pos) match {
            case Some(s: Scale) =>
            case _ =>
              val command = placeWeight(this, pos, scale, state)
              command.execute()
              update(scale, pos)
              command.undo()
          }
        }
      }
    }
    state.execute(placeWeight(this, best_pos, best_scale, state))
  }
}
