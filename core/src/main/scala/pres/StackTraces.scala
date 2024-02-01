package pres

import kyo.*
import zio.{Console, ZIO, ZIOAppDefault}

object ZioStackTraces extends ZIOAppDefault:
  override def run: ZIO[Any, Exception, Any] =
    def a() = ZIO.fail(new Exception("boom!"))
    def b() = Console.printLine("In b") *> a()
    def c() = Console.printLine("In c") *> b()
    def d() = Console.printLine("In d") *> c()

    d()

object KyoStackTraces extends KyoApp:
  def a() = Tries.fail(new Exception("boom!"))
  def b() = Consoles.println("In b").map(_ => a())
  def c() = Consoles.println("In c").map(_ => b())
  def d() = Consoles.println("In d").map(_ => c())

  run {
    d()
  }

@main def directStackTraces(): Unit =
  def a() = throw new Exception("boom!")
  def b() = { println("In b"); a() }
  def c() = { println("In c"); b() }
  def d() = { println("In d"); c() }

  d()
