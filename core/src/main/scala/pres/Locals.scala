package pres

object ZioLocals extends zio.ZIOAppDefault:
  import zio.*

  def program1(local: FiberRef[Int]): Task[Unit] = for {
    _ <- ZIO.sleep(1.second)
    v <- local.get
    _ <- Console.printLine(s"Value (p1): $v")
  } yield ()

  def program2(local: FiberRef[Int]): Task[Unit] = for {
    _ <- local.set(20)
    _ <- ZIO.sleep(1.second)
    v <- local.get
    _ <- Console.printLine(s"Value (p2): $v")
    f <- program1(local).fork
    _ <- f.join
  } yield ()

  override def run = for {
    local <- FiberRef.make[Int](0)
    f1 <- program1(local).fork
    f2 <- program2(local).fork
    _ <- f1.join
    _ <- f2.join
  } yield ()

@main def oxLocals(): Unit =
  import ox.*

  def program1(local: ForkLocal[Int]): Unit =
    Thread.sleep(1000)
    val v = local.get()
    println(s"Value (p1): $v")

  def program2(local: ForkLocal[Int]): Unit =
    local.scopedWhere(20) {
      Thread.sleep(1000)
      val v = local.get()
      println(s"Value (p2): $v")
      fork(program1(local)).join()
    }

  val v = ForkLocal(0)
  supervised {
    val f1 = fork(program1(v))
    val f2 = fork(program2(v))
    f1.join()
    f2.join()
  }
