package game.objects

import game.Game
import game.objects.Factory._

object Factory {
  val WEIGHT = "w"
  val SCALE = "s"
  val STACK = "t"
  val PLAYER = "p"

  def apply(game: Game) = new Factory(game)
}

class Factory(private val game: Game) {

  private var _idCounter = 0
  private def nextID(types: String): String = {_idCounter += 1; types + _idCounter.toString}

  def build_weight(pos: Int, parent_scale: Scale, owner: Option[Player] = None): (String, Weight) = {
    val newID = nextID(WEIGHT)
    val newWeight = new Weight(Some(parent_scale), newID, game)

    newWeight.set_owner(owner)

    if(parent_scale.isEmptyAt(pos)) {
      val newStackID = nextID(SCALE)
      val newStack = new Stack(parent_scale, newWeight, newStackID, game)
      parent_scale.place_at(pos, newStack)
    } else {
      parent_scale.objectAt(pos) match {
        case scale: Scale => throw new Exception("Occupied")
        case stack: Stack => stack.append(newWeight)
      }
    }
    newID -> newWeight
  }

  def build_first_scale(radius: Int): (String, Scale) = {
    val newID = nextID(SCALE)
    val newScale = new Scale(None, radius, newID, game)
    newID -> newScale
  }

  def build_scale(pos: Int, radius: Int, parent_scale: Scale): (String, Scale) = {
    val newID = nextID(SCALE)
    val newScale = new Scale(Some(parent_scale), radius, newID, game)
    parent_scale.place_at(pos, newScale)
    newID -> newScale
  }

  def build_bot(name: String): (String, Bot) = {
    val newID = nextID(PLAYER)
    val newPlayer = new Bot(name, newID, game)
    newID -> newPlayer
  }

  def build_human(name: String): (String, Human) = {
    val newID = nextID(PLAYER)
    val newPlayer = new Human(name, newID, game)
    newID -> newPlayer
  }
}
