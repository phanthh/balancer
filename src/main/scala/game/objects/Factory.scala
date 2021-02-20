package game.objects

import game.Game

class Factory(private val game: Game) {
  private val WEIGHT = "w"
  private val SCALE = "s"
  private val STACK = "t"
  private val PLAYER = "p"

  private var _idCounter = 0
  private def nextID(types: String): String = {_idCounter += 1; types + _idCounter.toString}

  def add_weight(pos: Int, parent_scale: Scale, owner: Option[Player] = None): Weight = {
    val newID = nextID(WEIGHT)
    val newWeight = new Weight(Some(parent_scale), newID, game)

    newWeight.set_owner(owner)

    if(parent_scale.isEmptyAt(pos)) {
      val newStackID = STACK + _idCounter.toString
      _idCounter += 1
      val newStack = new Stack(parent_scale, newWeight, newStackID, this)

      parent_scale.place_at(pos, newStack)

      gameObject += (newStackID -> newStack)

      parent_scale.place_at(pos, newStack)
    } else {

      parent_scale.objectAt(pos) match {
        case scale: Scale => throw new Exception("Occupied")
        case stack: Stack => stack.append(newWeight)
      }
    }

    gameObject += (newID -> newWeight)
    newWeight
  }

  def add_first_scale(radius: Int): Scale = {
    val newID = SCALE + _idCounter.toString
    _idCounter += 1
    val newScale = new Scale(None, radius, newID, this)
    gameObject += (newID -> newScale)
    newScale
  }

  def add_scale(pos: Int, radius: Int, parent_scale: Scale): Scale = {
    val newID = SCALE + _idCounter.toString
    _idCounter += 1
    val newScale = new Scale(None, radius, newID, this)
    parent_scale.place_at(pos, newScale)
    newScale
  }
}
