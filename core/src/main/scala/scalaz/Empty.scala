package scalaz

////
/**
 *
 */
////
trait Empty[F[_]] extends Plus[F] { self =>
  ////
  def empty[A]: F[A]

  // derived functions
  trait EmptyLaw extends PlusLaw {
    def emptyMap[A](f1: A => A)(implicit FA: Equal[F[A]]): Boolean =
      FA.equal(map(empty[A])(f1), empty[A])

    def rightPlusIdentity[A](f1: F[A])(implicit FA: Equal[F[A]]): Boolean =
      FA.equal(plus(f1, empty[A]), f1)

    def leftPlusIdentity[A](f1: F[A])(implicit FA: Equal[F[A]]): Boolean =
      FA.equal(plus(empty[A], f1), f1)
  }

  def emptyLaw = new EmptyLaw {}

  ////
  val emptySyntax = new scalaz.syntax.EmptySyntax[F] {}
}

object Empty {
  @inline def apply[F[_]](implicit F: Empty[F]): Empty[F] = F

  ////

  ////
}

