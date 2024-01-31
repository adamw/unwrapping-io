package pres

import kyo.*
import zio.{Console, ZIO, ZIOAppDefault}

abstract class AppException extends Exception
class UserNotFoundException extends AppException
class InvalidPasswordException extends AppException

object ZioHandleErrors extends ZIOAppDefault:
  override def run: ZIO[Any, Exception, Any] =
    val failingProgram: ZIO[Any, AppException, Nothing] = ZIO.fail(UserNotFoundException())

    val result1: ZIO[Any, Nothing, String] = failingProgram.catchAll { case _: UserNotFoundException => ZIO.succeed("Caught") }
    val result2: ZIO[Any, AppException, String] = failingProgram.catchSome { case _: InvalidPasswordException => ZIO.succeed("Caught") }

    for {
      r1 <- result1
      _ <- Console.printLine(r1)
      r2 <- result2
      _ <- Console.printLine(r2)
    } yield ()

object ZioDefects extends ZIOAppDefault:
  override def run: ZIO[Any, Exception, Any] =
    val failingProgram: ZIO[Any, AppException, Int] = ZIO.succeed(1 / 0)
    val result1: ZIO[Any, Nothing, Int] = failingProgram.catchAll { case _: Exception => ZIO.succeed(42) }
    val result2: ZIO[Any, Nothing, Int] = result1.resurrect.catchAll { case _: Exception => ZIO.succeed(43) }

    for {
      r <- result2
      _ <- Console.printLine(r)
    } yield ()

object ZioDirectHandleErrors extends ZIOAppDefault:
  override def run: ZIO[Any, Exception, Any] =
    import zio.direct._
    // throwing doesn't seem to be supported directly; plus the "happy branch" is always required
    val failingProgram1: ZIO[Any, UserNotFoundException, Int] = defer {
      ZIO.fail(UserNotFoundException()).run
      ZIO.succeed(0).run
    }

    def doThrow: Int = throw UserNotFoundException()
    val failingProgram2: ZIO[Any, Nothing, Int] = defer {
      doThrow
    }

    failingProgram1 *> failingProgram2

object OxHandleErrors extends App:
  def failingProgram: Nothing = throw UserNotFoundException()

  def result1 = try failingProgram
  catch case _: UserNotFoundException => "Caught"

  def result2 = try failingProgram
  catch case _: InvalidPasswordException => "Caught"

  println(result1)
  println(result2)

object KyoHandleErrors extends KyoApp: // direct - no throw support
  val failingProgram: String < Tries = Tries.fail(UserNotFoundException())
  val result1: String < Any = Tries.handle(failingProgram) { case _: UserNotFoundException => "Caught" }

  // not RT
  def result2: String < Any = Tries.handle(failingProgram) { case _: InvalidPasswordException => "Caught" }

  run {
    Consoles.println(result1).map(_ => Consoles.println(result2))
  }
