package pres

import zio.{Task, ZIO}

object SyntaxOverhead {
  case class Passenger()
  case class RocketStage()
  case class LaunchParams(rocketStages: List[RocketStage], farAway: Boolean)

  object Zio {
    def fetchPassengers(): Task[List[Passenger]] = ???
    def prepareLaunch(passengers: List[Passenger]): Task[LaunchParams] = ???
    def attachBoosterRockets(): Task[Unit] = ???
    def fuelUp(stage: RocketStage): Task[Unit] = ???
    def pressBigRedButton(): Task[Unit] = ???

    val result: Task[Unit] = for {
      passengers <- fetchPassengers()
      params <- prepareLaunch(passengers)
      _ <- if (params.farAway) attachBoosterRockets() else ZIO.unit
      _ <- ZIO.foreachParDiscard(params.rocketStages)(fuelUp)
      _ <- pressBigRedButton()
    } yield ()
  }

  object ZioDirect {
    def fetchPassengers(): Task[List[Passenger]] = ???
    def prepareLaunch(passengers: List[Passenger]): Task[LaunchParams] = ???
    def attachBoosterRockets(): Task[Unit] = ???
    def fuelUp(stage: RocketStage): Task[Unit] = ???
    def pressBigRedButton(): Task[Unit] = ???

    import zio.direct._
    val result: Task[Unit] = defer {
      val passengers = fetchPassengers().run
      val params = prepareLaunch(passengers).run

      if (params.farAway) {
        attachBoosterRockets().run
      }

      // foreach is supported, but we want concurrency
      ZIO.foreachParDiscard(params.rocketStages)(fuelUp).run

      pressBigRedButton().run
    }
  }

  object Ox {
    def fetchPassengers(): List[Passenger] = ???
    def prepareLaunch(passengers: List[Passenger]): LaunchParams = ???
    def attachBoosterRockets(): Unit = ???
    def fuelUp(stage: RocketStage): Unit = ???
    def pressBigRedButton(): Unit = ???

    val passengers = fetchPassengers()
    val params = prepareLaunch(passengers)

    if (params.farAway) {
      attachBoosterRockets()
    }

    params.rocketStages.foreach(stage => fuelUp(stage))

    pressBigRedButton()
  }

  object Kyo {
    import kyo._

    def fetchPassengers(): List[Passenger] < IOs = ???
    def prepareLaunch(passengers: List[Passenger]): LaunchParams < IOs = ???
    def attachBoosterRockets(): Unit < IOs = ???
    def fuelUp(stage: RocketStage): Unit < IOs = ???
    def pressBigRedButton(): Unit < IOs = ???

    val result: Unit < Fibers = for {
      passengers <- fetchPassengers()
      params <- prepareLaunch(passengers)
      _ <- if (params.farAway) attachBoosterRockets() else ().pure
      _ <- Fibers.parallel(params.rocketStages.map(stage => fuelUp(stage)))
      _ <- pressBigRedButton()
    } yield ()
  }

  object KyoDirect {
    import kyo._
    import kyo.direct._

    def fetchPassengers(): List[Passenger] < IOs = ???
    def prepareLaunch(passengers: List[Passenger]): LaunchParams < IOs = ???
    def attachBoosterRockets(): Unit < IOs = ???
    def fuelUp(stage: RocketStage): Unit < IOs = ???
    def pressBigRedButton(): Unit < IOs = ???

    val result: Unit < Fibers = defer {
      val passengers = await(fetchPassengers())
      val params = await(prepareLaunch(passengers))

      if (params.farAway) {
        await(attachBoosterRockets())
      }

      await(Fibers.parallel(params.rocketStages.map(stage => fuelUp(stage))))

      await(pressBigRedButton())
    }
  }
}
