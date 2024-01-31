package pres

object ZioSupervision extends zio.ZIOAppDefault:
  import zio.*

  val child = (ZIO.sleep(1.second) *> Console.printLine("Working ...")).forever.fork
  val work = Console.printLine("Starting to work ...") *> child *> ZIO.sleep(3.seconds) *> ZIO.fail("Boom!")

  override def run: ZIO[Any, Exception, Any] =
    work.catchAll(e => Console.printLine(s"Failed: $e")) *> ZIO.sleep(3.seconds)

object ZioSupervision2 extends zio.ZIOAppDefault:
  import zio.*

  val child = (ZIO.sleep(1.second) *> Console.printLine("Working ...")).forever.fork
  val work = Console.printLine("Starting to work ...") *> child *> ZIO.sleep(3.seconds) *> ZIO.fail("Boom!")

  override def run: ZIO[Any, Exception, Any] =
    work.fork.flatMap(_.join).catchAll(e => Console.printLine(s"Failed: $e")) *> ZIO.sleep(3.seconds)

object KyoSupervision extends kyo.KyoApp:
  import kyo.*
  import scala.concurrent.duration.*

  val child: Fiber[Nothing] < Fibers =
    def runChild: Nothing < Fibers = Fibers.sleep(1.second).map(_ => Consoles.println("Working ...")).map(_ => runChild)
    Fibers.init(runChild)

  val work: String < (Fibers & Tries) =
    Consoles.println("Starting to work ...").map(_ => child).map(_ => Fibers.sleep(3.seconds)).map(_ => Tries.fail(new Exception("Boom!")))

  run {
    val handled: String < Fibers = Tries.handle(work)(e => s"Failed: $e")
    handled.map(e => Consoles.println(s"Failed: $e")).map(_ => Fibers.sleep(3.seconds))
  }

object OxSupervision extends App:
  import ox.*
  import ox.syntax.*

  def child()(using Ox) = { Thread.sleep(1000); println("Working ...") }.forever.fork
  def work() = supervised {
    println("Starting to work ...")
    child()
    Thread.sleep(3000)
    throw new Exception("Boom!")
  }

  try work()
  catch case e: Exception => println(s"Failed: $e")
  Thread.sleep(3000)
