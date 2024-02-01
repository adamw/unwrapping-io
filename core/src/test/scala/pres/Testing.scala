package pres

object ZioTesting extends zio.test.ZIOSpecDefault:
  import zio.*
  import zio.test.*
  import zio.test.Assertion.hasSameElements

  val program = for {
    _ <- Console.printLine("Hello!")
    _ <- ZIO.sleep(1.second)
    _ <- Console.printLine("World!")
    _ <- ZIO.sleep(1.second)
    _ <- Console.printLine("Bye now!")
  } yield ()

  override def spec = suite("ZioTestingSpec")(
    test("will this take a lot of time?") {
      for {
        f <- program.fork
        _ <- TestClock.adjust(1.minute)
        output <- TestConsole.output
      } yield assert(output)(hasSameElements(Vector("Hello!\n", "World!\n", "Bye now!\n")))
    }
  )

class DirectTesting extends org.scalatest.flatspec.AnyFlatSpec:
  it should "work" in {
    println("Hello!")
    Thread.sleep(1000)
    println("World!")
    Thread.sleep(1000)
    println("Bye now!")
  }
