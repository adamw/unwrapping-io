package pres

import java.util.concurrent.atomic.AtomicInteger

@main def loom(): Unit =
  timed {
    val counter = new AtomicInteger(0)
    val threads = Array.ofDim[Thread](10_000_000)
    for (i <- threads.indices)
      threads(i) = Thread.startVirtualThread(() => counter.incrementAndGet())
    for (i <- threads.indices)
      threads(i).join()
    println(counter.get())
  }

def timed(t: => Unit): Unit =
  val s = System.currentTimeMillis()
  try t
  finally println(s"${System.currentTimeMillis() - s}ms")
