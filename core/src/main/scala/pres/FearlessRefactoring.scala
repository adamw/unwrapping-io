package pres

import kyo.*
import zio.{Console, ZIO, ZIOAppDefault}

object ZioFearlessRefactoringBefore extends ZIOAppDefault:
  override def run: ZIO[Any, Exception, Unit] = for {
    _ <- Console.printLine("Prepare ...")
    _ <- Console.printLine("Launching rockets")
    _ <- Console.printLine("Launching rockets")
  } yield ()

object ZioFearlessRefactoringAfter extends ZIOAppDefault:
  val launch = Console.printLine("Launching rockets")

  override def run: ZIO[Any, Exception, Unit] = for {
    _ <- Console.printLine("Prepare ...")
    _ <- launch
    _ <- launch
  } yield ()

object ZioDirectFearlessRefactoringBefore extends ZIOAppDefault:
  override def run: ZIO[Any, Exception, Unit] = {
    import zio.direct.*
    defer {
      Console.printLine("Prepare ...").run
      Console.printLine("Launching rockets").run
      Console.printLine("Launching rockets").run
    }
  }

object ZioDirectFearlessRefactoringAfter1 extends ZIOAppDefault:
  override def run: ZIO[Any, Exception, Unit] = {
    import zio.direct.*
    defer {
      val launch = Console.printLine("Launching rockets").run

      Console.printLine("Prepare ...").run
      launch
      launch
    }
  }

object ZioDirectFearlessRefactoringAfter2 extends ZIOAppDefault:
  override def run: ZIO[Any, Exception, Unit] = {
    import zio.direct._
    defer {
      val launch = Console.printLine("Launching rockets")

      Console.printLine("Prepare ...").run
      launch.run
      launch.run
    }
  }

@main def directFearlessRefactoringBefore(): Unit =
  println("Prepare ...")
  println("Launching rockets")
  println("Launching rockets")

@main def directFearlessRefactoringAfter1(): Unit =
  val launch = println("Launching rockets")

  println("Prepare ...")
  launch
  launch

@main def directFearlessRefactoringAfter2(): Unit =
  def launch = println("Launching rockets")

  println("Prepare ...")
  launch
  launch

object KyoFearlessRefactoring extends KyoApp:
  val launch: Unit < IOs = Consoles.println("Launching rockets")

  run {
    Consoles.println("Prepare ...").map(_ => launch).map(_ => launch)
  }
