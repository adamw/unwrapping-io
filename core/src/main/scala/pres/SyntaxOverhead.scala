package pres

object SyntaxOverhead:
  case class Passenger()
  case class RocketStage()
  case class LaunchParams(rocketStages: List[RocketStage], farAway: Boolean)

  object Zio:
    import zio.{Task, ZIO}

    def fetchPassengers(): Task[List[Passenger]] = ???
    def prepareLaunch(passengers: List[Passenger]): Task[LaunchParams] = ???
    def attachBoosterRockets(): Task[Unit] = ???
    def fuelUp(stage: RocketStage): Task[Unit] = ???
    def pressBigRedButton(): Task[Unit] = ???

    val result: Task[Unit] = for {
      passengers <- fetchPassengers()
      params <- prepareLaunch(passengers)
      _ <- if (params.farAway) attachBoosterRockets() else ZIO.unit
      _ <- ZIO.foreachDiscard(params.rocketStages)(fuelUp)
      _ <- pressBigRedButton()
    } yield ()

  object ZioDirect:
    import zio.{Task, ZIO}

    def fetchPassengers(): Task[List[Passenger]] = ???
    def prepareLaunch(passengers: List[Passenger]): Task[LaunchParams] = ???
    def attachBoosterRockets(): Task[Unit] = ???
    def fuelUp(stage: RocketStage): Task[Unit] = ???
    def pressBigRedButton(): Task[Unit] = ???

    import zio.direct.*
    val result: Task[Unit] = defer {
      val passengers = fetchPassengers().run
      val params = prepareLaunch(passengers).run

      if (params.farAway) {
        attachBoosterRockets().run
      }

      params.rocketStages.foreach(stage => fuelUp(stage).run)

      pressBigRedButton().run
    }

  object Direct:
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

  object Kyo:
    import kyo.*

    def fetchPassengers(): List[Passenger] < IOs = ???
    def prepareLaunch(passengers: List[Passenger]): LaunchParams < IOs = ???
    def attachBoosterRockets(): Unit < IOs = ???
    def fuelUp(stage: RocketStage): Unit < IOs = ???
    def pressBigRedButton(): Unit < IOs = ???

    def fuelUp(stages: List[RocketStage]): Unit < IOs =
      stages match {
        case Nil => IOs.unit
        case stage :: tail =>
          for {
            _ <- fuelUp(stage)
            _ <- fuelUp(tail)
          } yield ()
      }

    val result: Unit < IOs = for {
      passengers <- fetchPassengers()
      params <- prepareLaunch(passengers)
      _ <- if (params.farAway) attachBoosterRockets() else ().pure
      _ <- fuelUp(params.rocketStages)
      _ <- pressBigRedButton()
    } yield ()

  object KyoDirect:
    import kyo.*
    import kyo.direct.*

    def fetchPassengers(): List[Passenger] < IOs = ???
    def prepareLaunch(passengers: List[Passenger]): LaunchParams < IOs = ???
    def attachBoosterRockets(): Unit < IOs = ???
    def fuelUp(stage: RocketStage): Unit < IOs = ???
    def pressBigRedButton(): Unit < IOs = ???

    def fuelUp(stages: List[RocketStage]): Unit < IOs =
      stages match {
        case Nil => IOs.unit
        case stage :: tail =>
          defer {
            await(fuelUp(stage))
            await(fuelUp(tail))
          }
      }

    // Fibers subsumes IOs
    val result: Unit < Fibers = defer {
      val passengers = await(fetchPassengers())
      val params = await(prepareLaunch(passengers))

      if (params.farAway) {
        await(attachBoosterRockets())
      }

      // foreach doesn't work
      await(fuelUp(params.rocketStages))

      await(pressBigRedButton())
    }
