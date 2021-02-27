package game.objects

import game.Factory

sealed abstract class Player
  extends GameObject {
  val name: String
  def player_code: Char = name(0)
  def score: Int = factory.baseScale.score(this)
  var roundWon = 0
}

case class Bot(val name: String, val factory: Factory)
  extends Player {
  def place_weight(): Unit = {
    var max_score = -1
    var max_score_pos = 0
    var max_score_scale: Scale = null

    def update(scale: Scale, pos: Int) = {
      val currentScore = score
      if(currentScore > max_score && scale.isBalanced) {
        max_score = currentScore
        max_score_scale = scale
        max_score_pos = pos
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

    factory.build_weight(max_score_pos, max_score_scale, Some(this))
  }
}

case class Human(val name: String, val factory: Factory) extends Player