package game

import game.grid.Grid
import game.objects.{Factory, GameObject, Player, Scale}
import game.UI._

import scala.collection.mutable.Map

class Game (val numRounds: Int = 5, val weightsPerRound: Int = 10) {
  def register(pair: (String, GameObject)) = gameObjects += pair

  val grid = new Grid(20,40)
  val factory = Factory(this)
  private val gameObjects = Map[String, GameObject]()

  def scales = gameObjects.filter(_._1.startsWith(Factory.SCALE)).values.map(_.asInstanceOf[Scale])
  def scaleWithCode(code: Char) = scales.find(_.scale_code == code)
  def players = gameObjects.filter(_._1.startsWith(Factory.PLAYER)).values.map(_.asInstanceOf[Player])
  def winner = players.maxBy(_.score)
  def finalWinner = players.maxBy(_.roundWon)
  def baseScale = scaleWithCode('a').getOrElse(???) // TODO: Exception handling

  factory.build_scale(radius=5)
  factory.build_scale(3, radius=3, baseScale)
}
