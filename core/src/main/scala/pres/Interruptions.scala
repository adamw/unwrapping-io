package pres

import scala.util.control.NonFatal

object ZioInterruptions extends zio.ZIOAppDefault:
  import zio.*

  val child = (ZIO.sleep(1.second) *> Console.printLine("Working ...")).resurrect
    .catchAll(e => ZIO.logErrorCause("Error when processing", Cause.fail(e)))
    .forever
    .fork

  val work = for {
    _ <- Console.printLine("Starting child ...")
    f <- child
    _ <- ZIO.sleep(3.seconds)
    _ <- Console.printLine("Bye!")
    _ <- f.interrupt
  } yield ()

  override def run: ZIO[Any, Exception, Any] =
    work *> ZIO.sleep(3.seconds)

object KyoInterruptions extends kyo.KyoApp:
  import kyo.*
  import scala.concurrent.duration.*

  val child: Fiber[Unit] < Fibers =
    def runChild: Nothing < Fibers =
      Tries
        .handle(
          Fibers
            .sleep(1.second)
            .map(_ => Consoles.println("Working ..."))
        )(e => Consoles.println("Error when processing").map(_ => IOs(e.printStackTrace())))
        .map(_ => runChild)
    Fibers.init(runChild)

  val work: Unit < (Fibers & Tries) = for {
    _ <- Consoles.println("Starting child ...")
    f <- child
    _ <- Fibers.sleep(3.seconds)
    _ <- Consoles.println("Bye!")
    _ <- f.interrupt
  } yield ()

  run {
    work.map(_ => Fibers.sleep(3.seconds))
  }

object OxInterruptionsWrong extends App:
  import ox.*

  def child()(using Ox) =
    forkDaemon {
      forever {
        try
          Thread.sleep(1000)
          println("Working ...")
        catch
          case e: Exception =>
            println("Error when processing")
            e.printStackTrace()
      }
    }

  def work() = supervised {
    println("Starting child ...")
    child()
    Thread.sleep(3000)
    println("Bye!")
  }

  work()
  Thread.sleep(3000)
  println("All done")

object OxInterruptionsCorrect extends App:
  import ox.*

  def child()(using Ox) =
    forkDaemon {
      forever {
        try
          Thread.sleep(1000)
          println("Working ...")
        catch
          case NonFatal(e) =>
            println("Error when processing")
            e.printStackTrace()
      }
    }

  def work() = supervised {
    println("Starting child ...")
    child()
    Thread.sleep(3000)
    println("Bye!")
  }

  work()
  Thread.sleep(3000)
  println("All done")
