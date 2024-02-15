package pres

import kyo.*
import zio.{Console, ZIO, ZIOAppDefault}

import scala.annotation.targetName
import scala.util.boundary.{break, Label}
import scala.util.{boundary, Failure, NotGiven, Try}

abstract class AppException extends Exception
class UserNotFoundException extends AppException
class InvalidPasswordException extends AppException

case class User()

object ZioHandleErrors extends ZIOAppDefault:
  override def run: ZIO[Any, Exception, Any] =
    val failingProgram: ZIO[Any, AppException, String] = ZIO.fail(UserNotFoundException())

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
    import zio.direct.*
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

object ZioComposeErrors:
  def lookupUser(id: Int): ZIO[Any, UserNotFoundException, User] = ???
  def validatePassword(user: User, password: String): ZIO[Any, InvalidPasswordException, Unit] = ???

  val result: ZIO[Any, AppException, User] = for {
    user <- lookupUser(1)
    _ <- validatePassword(user, "password")
  } yield user

@main def directHandleErrors(): Unit =
  def failingProgram: String = throw UserNotFoundException()

  def result1: String = try failingProgram
  catch case _: UserNotFoundException => "Caught"

  def result2: String = try failingProgram
  catch case _: InvalidPasswordException => "Caught"

  println(result1)
  println(result2)

object DirectComposeErrors:
  def lookupUser(id: Int): User = ???

  def validatePassword(user: User, password: String): Unit = ???

  val user = lookupUser(1)
  validatePassword(user, "password")

@main def directEithersErrors(): Unit =
  def failingProgram: Either[AppException, String] = Left(UserNotFoundException())

  def result1: String = failingProgram match
    case Left(_: UserNotFoundException) => "Caught"
    case Right(v)                       => v

  def result2: Either[AppException, String] = failingProgram match
    case Left(_: InvalidPasswordException) => Right("Caught")
    case Left(e)                           => Left(e)
    case Right(v)                          => Right(v)

  println(result1)
  println(result2)

@main def directEithersErrorsCompose(): Unit =
  def lookupUser(id: Int): Either[UserNotFoundException, User] = ???
  def validatePassword(user: User, password: String): Either[InvalidPasswordException, Unit] = ???

  val result: Either[AppException, User] = for {
    user <- lookupUser(1)
    _ <- validatePassword(user, "password")
  } yield user

@main def directBoundaryBreakCompose =
  def lookupUser(id: Int): Either[UserNotFoundException, User] = ???
  def validatePassword(user: User, password: String): Either[InvalidPasswordException, Unit] = ???

  import getEither.?
  val result: Either[AppException, User] = getEither {
    val user = lookupUser(1).?
    val _ = validatePassword(user, "password").?
    user
  }

object KyoHandleErrors extends KyoApp: // direct - no throw support
  val failingProgram: String < Tries = Tries.fail(UserNotFoundException())
  val result1: String < Any = Tries.handle(failingProgram) { case _: UserNotFoundException => "Caught" }

  // not RT
  def result2: String < Any = Tries.handle(failingProgram) { case _: InvalidPasswordException => "Caught" }

  run {
    Consoles.println(result1).map(_ => Consoles.println(result2))
  }

// boundary-break for either, from: https://gist.github.com/johnhungerford/d6a50d31c7b9e05729b801d276977a6b

object getEither:
  import GetAble.Fail

  inline def apply[L, A](
      inline body: Label[Fail[L] | A] ?=> Fail[L] | A
  )(using ng: NotGiven[Fail[Nothing] <:< A], fe: ToEither[A]): Either[L | fe.L, fe.R] =
    boundary(body) match
      case Fail(value) =>
        Left(value.asInstanceOf[L])
      case success =>
        fe.toEither(success.asInstanceOf[A])

  extension [V, AV, FV](t: V)(using getable: GetAble[V] { type F = FV; type A = AV }, b: boundary.Label[Fail[FV]])
    /** Exits with `Fail` to next enclosing `getEither` boundary */
    @targetName("questionMark")
    inline def ? : AV =
      getable.get(t) match
        case either: Fail[FV] =>
          break(either)
        case value =>
          value.asInstanceOf[AV]

/** Type class for extracting boxed values while preserving failures
  *
  * @tparam T
  *   extractable type
  */
sealed trait GetAble[T]:
  import GetAble.Fail
  type F
  type A
  def get(value: T): Fail[F] | A

object GetAble:
  sealed case class Fail[+F](failure: F)

  transparent inline def apply[F](using GetAble[F]): GetAble[F] = summon[GetAble[F]]

  given opt[AV, O <: Option[AV]]: GetAble[O] with
    type F = Unit; type A = AV
    override def get(value: O): Fail[Unit] | AV = value.getOrElse(Fail(()))

  given tr[AV, T <: Try[AV]]: GetAble[T] with
    type F = Throwable; type A = AV
    override def get(value: T): Fail[Throwable] | AV = value.getOrElse(Fail(value.asInstanceOf[Failure[Nothing]].exception))

  given either[FV, AV, E <: Either[FV, AV]]: GetAble[E] with
    type F = FV; type A = AV
    override def get(value: E): Fail[FV] | AV = value match
      case value: Left[FV, AV]  => Fail(value.value)
      case value: Right[FV, AV] => value.value

/** Type class for converting different optional or failable types to an `Either`.
  *
  * @tparam A
  *   value that is convertable to Either
  */
sealed trait ToEither[A]:
  type L
  type R
  def toEither(a: A): Either[L, R]

trait LowPriorityToEither:
  given any[A]: ToEither[A] with
    type L = Nothing; type R = A
    override def toEither(a: A): Either[Nothing, A] = Right(a)

object ToEither extends LowPriorityToEither:
  given opt[A, O <: Option[A]]: ToEither[O] with
    type L = Unit; type R = A
    def toEither(a: O): Either[Unit, A] =
      a.fold(Left(()))(Right.apply)

  given tr[A, T <: Try[A]]: ToEither[T] with
    type L = Throwable; type R = A
    override def toEither(a: T): Either[Throwable, A] =
      a.toEither

  given either[LV, RV, E <: Either[LV, RV]]: ToEither[E] with
    type L = LV; type R = RV

    override def toEither(a: E): Either[LV, RV] = a
