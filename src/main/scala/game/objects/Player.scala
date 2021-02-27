package game.objects

import game.Factory
import game.objects.Bot.{BESTMOVE, RANDOM, RANDOMBESTMOVE}

import scala.util.Random

sealed abstract class Player
  extends GameObject {
  val name: String
  def player_code: Char = name(0)
  def score: Int = factory.baseScale.score(this)
  var roundWon = 0
}

case class Human(val name: String, val factory: Factory) extends Player

object Bot {
  val BESTMOVE = 1
  val RANDOM = 2
  val RANDOMBESTMOVE = 3
}

case class Bot(val name: String, val factory: Factory)
  extends Player {

  def place_weight(method: Int = BESTMOVE): Unit = {
    method match {
      case BESTMOVE => best_move()
      case RANDOM => random()
      case RANDOMBESTMOVE =>
        if(Random.nextFloat()>0.3) random() else best_move()
      case _ => random()
    }
  }

  def random(): Unit = {
    val scales = factory.scales
    var pos = 0

    while(pos == 0){
      val scale = scales(Random.nextInt(scales.length))
      pos = Random.between(-scale.radius, scale.radius)
      if(pos != 0){
        scale.at(pos) match {
          case Some(scale: Scale) => pos = 0
          case Some(stack: Stack) =>
            factory.build_weight(pos, scale, Some(this), true)
            if(!scale.isBalanced) stack.soft_pop()
          case None =>
            factory.build_weight(pos, scale, Some(this), true)
            if(!scale.isBalanced) scale.remove_at(pos)
        }
      }
    }
  }

  def best_move(): Unit = {
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
          scale.at(pos) match {
            case Some(scale: Scale) =>
            case Some(stack: Stack) =>
              factory.build_weight(pos, scale, Some(this), true)
              update(scale, pos)
              stack.soft_pop()
            case None =>
              factory.build_weight(pos, scale, Some(this), true)
              update(scale, pos)
              scale.remove_at(pos)
          }
        }
      }
    }

    factory.build_weight(best_pos, best_scale, Some(this))
  }
}
