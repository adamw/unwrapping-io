package pres

import kyo.*
import zio.{Cause, Scope, ZIO}

import java.io.FileInputStream
import scala.util.Using

object Resources:
  object Zio:
    val file: ZIO[Scope, Throwable, FileInputStream] = ZIO.acquireRelease(ZIO.attempt(new FileInputStream("file.txt")))(in =>
      ZIO.attempt(in.close()).catchAll(e => ZIO.logErrorCause("Exception when closing file", Cause.fail(e)))
    )

    val firstByte: ZIO[Any, Throwable, Int] = ZIO.scoped {
      file.flatMap(is => ZIO.attempt(is.read()))
    }

  object ZioMulti:
    def file(name: String): ZIO[Scope, Throwable, FileInputStream] =
      ZIO.acquireRelease(ZIO.attempt(new FileInputStream(name)))(in =>
        ZIO.attempt(in.close()).catchAll(e => ZIO.logErrorCause("Exception when closing file", Cause.fail(e)))
      )

    val sum: ZIO[Any, Throwable, Int] = ZIO.scoped {
      for {
        f1 <- file("file1.txt")
        f2 <- file("file2.txt")
      } yield f1.read() + f2.read()
    }

  object Kyo:
    val file: FileInputStream < (IOs & Resources) = kyo.Resources.acquire(new FileInputStream("file.txt"))

    val firstByte: Int < IOs = kyo.Resources.run(file.map(_.read()))

  object Direct:
    val file = new FileInputStream("file.txt")
    try
      val firstByte = file.read()
    finally file.close()

  object DirectUsing:
    Using(new FileInputStream("file.txt")) { file =>
      val firstByte = file.read()
    }

  object DirectMulti:
    Using(new FileInputStream("file1.txt")) { file1 =>
      Using(new FileInputStream("file2.txt")) { file2 =>
        file1.read() + file2.read()
      }
    }

  object DirectLeak:
    val file = new FileInputStream("file.txt")
    val firstByte = file.read()
