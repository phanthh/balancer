package balancer.utils

import scalafx.scene.image.Image

/**
 * This object store the constants and basic settings of the game.
 */
object Constants {
  // Assets
  lazy val spriteMap = Seq("ground", "padder0", "padder1", "right", "left", "weight", "fulcrum").map(
    f => (f, new Image(s"file:assets/tiles/$f.png", CellWidth, CellHeight, false, true))).toMap
  lazy val skylineImages = Seq(
    new Image("file:assets/backgrounds/skyline1.png"),
    new Image("file:assets/backgrounds/skyline2.png"))
  lazy val logo = new Image("file:assets/logo_edited2.png")

  // Settings
  val ScreenWidth = 1280
  val ScreenHeight = 720
  val CellWidth = 50
  val CellHeight = 50
  val MaxUndo = 20
  val MaxRandomFind = 100
  val DefaultFile = "defaultfile.txt"
  val FontFile = "file:fonts/cyber.otf"

  // Infos
  val Version = 1.0
  val GithubURL = "https://github.com/phanthh/balancer"
  val GitlabURL = "https://version.aalto.fi/gitlab/phanh7/balancer"
  val Rules =
    """
      |-  Each player will in turn place their weights on the scales.
      |-  Left-clicking on the screen to place your weight.
      |-  You can place your weights on the scale and on other players' weights (capturing).
      |-  The scale will flip if become unbalanced and all the weights will be lost.
      |-  Given enough weights, you can "capture" a scale, doubling only your points and give
      |your weights "resistance".
      |
      |-  The game spans over multiple rounds.
      |-  Each round has a weight pool.
      |-  The round ends when the pool is emptied.
      |-  The player with the most points win the round.
      |-  The player won the most round will be the final roundWinner.
      |
      |Tips:
      |-  Riskier weights (i.e, on the edges, on higher scales) give more points.
      |-  Intentionally flipping a scale can be a stategy.
      |
      |""".stripMargin
  val Abouts =
    s"""
       |-  A project for Programming Studio 2 Course - Aalto University
       |-  Source code is available on Github and Gitlab.
       |-  Author: Hau Phan
       |""".stripMargin
}

