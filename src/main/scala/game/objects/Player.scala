package game.objects

import game.Store
import game.objects.Bot.{BESTMOVE, RANDOM, RANDOMBESTMOVE}

import scala.util.Random

sealed abstract class Player
  extends GameObject {
  val name: String
  def player_code: Char = name(0)
  def score: Int = factory.baseScale.score(this)
  var roundWon = 0
}

case class Human(val name: String, val factory: Store) extends Player

object Bot {
  val BESTMOVE = 1
  val RANDOM = 2
  val RANDOMBESTMOVE = 3
}

case class Bot(val name: String, val factory: Store)
  extends Player {

  def placeWeight(method: Int = BESTMOVE): Unit = {
    method match {
      case BESTMOVE => bestMove()
      case RANDOM => random()
      case RANDOMBESTMOVE =>
        if(Random.nextFloat()>0.3) random() else bestMove()
      case _ => random()
    }
  }

  def random(): Unit = {
    val scales = factory.scales
    var pos = 0
    var scale: Scale = null

    while(pos == 0){
      scale = scales(Random.nextInt(scales.length))
      pos = Random.between(-scale.radius, scale.radius)
      if(pos != 0){
        scale(pos) match {
          case Some(scale: Scale) => pos = 0
          case Some(stack: Stack) =>
            factory.buildWeight(pos, scale, Some(this), true)
            if(!scale.isBalanced) pos = 0
            stack.pop()
          case None =>
            factory.buildWeight(pos, scale, Some(this), true)
            if(!scale.isBalanced) pos = 0
            scale.remove(pos)
        }
      }
    }

    factory.buildWeight(pos, scale, Some(this))
  }

  def bestMove(): Unit = {
    var best_score = -1
    var best_pos = 0
    var best_scale: Scale = null

    def update(scale: Scale, pos: Int) = {
      val currentScore = score
      if(currentScore > best_score && scale.isBalanced) {
        best_score = currentScore
        best_scale = scale
        best_pos = pos
      }
    }

    for(scale <- factory.scales){
      for(idx <- 0 until 2*scale.radius+1){
        val pos = idx - scale.radius
        if(pos != 0){
          scale(pos) match {
            case Some(scale: Scale) =>
            case Some(stack: Stack) =>
              factory.buildWeight(pos, scale, Some(this), true)
              update(scale, pos)
              stack.pop()
            case None =>
              factory.buildWeight(pos, scale, Some(this), true)
              update(scale, pos)
              scale.remove(pos)
          }
        }
      }
    }

    factory.buildWeight(best_pos, best_scale, Some(this))
  }
}
