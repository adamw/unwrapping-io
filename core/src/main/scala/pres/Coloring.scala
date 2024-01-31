package pres

import kyo.*
import zio.ZIO

object Coloring:
  case class RocketStage()
  case class RocketNose()
  case class Rocket(stages: List[RocketStage], nose: RocketNose)

  object Zio:
    def createStages: ZIO[Any, Nothing, List[RocketStage]] = ???
    def createNose: ZIO[Any, Nothing, RocketNose] = ???

    def assembleRocket: ZIO[Any, Nothing, Rocket] =
      for {
        stages <- createStages
        nose <- createNose
      } yield Rocket(stages, nose)

  object Kyo:
    def createStages: List[RocketStage] < IOs = ???
    def createNose: RocketNose < IOs = ???

    def assembleRocket: Rocket < IOs =
      for {
        stages <- createStages
        nose <- createNose
      } yield Rocket(stages, nose)

  object Ox:
    def createStages: List[RocketStage] = ???
    def createNose: RocketNose = ???

    def assembleRocket: Rocket =
      val stages = createStages
      val nose = createNose
      Rocket(stages, nose)
