package pres

import kyo._
import zio.{Cause, Scope, ZIO}

import java.io.FileInputStream

object Resources:
  object Zio:
    val file: ZIO[Scope, Throwable, FileInputStream] = ZIO.acquireRelease(ZIO.attempt(new FileInputStream("file.txt")))(in =>
      ZIO.attempt(in.close()).catchAll(e => ZIO.logErrorCause("Exception when closing file", Cause.fail(e)))
    )

    val firstByte: ZIO[Any, Throwable, Int] = ZIO.scoped {
      file.flatMap(is => ZIO.attempt(is.read()))
    }

  object Kyo:
    val file: FileInputStream < (IOs & Resources) = kyo.Resources.acquire(new FileInputStream("file.txt"))

    val firstByte: Int < IOs = kyo.Resources.run(file.map(_.read()))

  object Direct:
    val file = new FileInputStream("file.txt")
    try
      val firstByte = file.read()
    finally file.close()

  object DirectLeak:
    val file = new FileInputStream("file.txt")
    val firstByte = file.read()
