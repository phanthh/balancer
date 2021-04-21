package balancer.utils

import scalafx.scene.image.Image

object Constants {
  val ScreenWidth = 1280
  val ScreenHeight = 720
  val CellWidth = 50
  val CellHeight = 50
  val DefaultFile = "defaultfile.txt"
  val MaxUndo = 20
  val Version = 1.0
  val MaxRandomFind = 100
  val Rules = "TO BE IMPLEMENTED"  // TODO: Add rules
  val Abouts = "TO BE IMPLEMENTED" // TODO: Add about
  val GithubURL = "https://github.com/phanthh/balancer"
  val GitlabURL = "https://version.aalto.fi/gitlab/phanh7/balancer"

  lazy val spriteMap = List("ground", "padder0", "padder1", "right", "left", "weight", "fulcrum").map(
    f => (f, new Image(s"file:assets/tiles/$f.png", CellWidth, CellHeight, false, true))
  ).toMap

  lazy val logo = new Image("file:assets/logo_edited2.png")
}

