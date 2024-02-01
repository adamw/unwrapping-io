package pres

object HighLevel:
  object Zio:
    import zio.*

    def lookupInCache(): Task[String] = ???
    def lookupInDb(): Task[String] = ???
    def updateMetrics(): Task[Unit] = ???

    updateMetrics().zipParRight(lookupInCache().race(lookupInDb()).timeout(1.second))

  object Kyo:
    import kyo.*
    import scala.concurrent.duration.*

    def lookupInCache(): String < IOs = ???
    def lookupInDb(): String < IOs = ???
    def updateMetrics(): Unit < IOs = ???

    Fibers.parallel(
      updateMetrics(),
      Fibers.timeout(1.second)(Fibers.race(lookupInCache(), lookupInDb()))
    )

  object Ox:
    import ox.*
    import scala.concurrent.duration.*

    def lookupInCache(): String = ???
    def lookupInDb(): String = ???
    def updateMetrics(): Unit = ???

    par(updateMetrics())(timeout(1.second)(raceSuccess(lookupInCache())(lookupInDb())))._2
