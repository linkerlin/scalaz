package scalaz

import scalacheck.ScalazProperties

class TraverseTest extends Spec {

  import scalaz._
  import std.AllInstances._
  import std.AllFunctions._
  import syntax.traverse._

  "list" should {
    // ghci> import Data.Traversable
    // ghci> import Control.Monad.Writer
    // ghci> let (|>) = flip ($)
    // ghci> traverse (\x -> writer (x, x)) ["1", "2", "3"] |> runWriter
    // (["1","2","3"],"123")
    "apply effects in order" in {
      val s: Writer[String, List[Int]] = List(1, 2, 3).traverseU(x => Writer(x.toString, x))
      s.run must be_===(("123", List(1, 2, 3)))
    }

    "traverse through option effect" in {
      val s: Option[List[Int]] = List(1, 2, 3).traverseU((x: Int) => if (x < 3) some(x) else none)
      s must be_===(none[List[Int]])
    }

    "not blow the stack" in {
      val s: Option[List[Int]] = List.range(0, 32 * 1024).traverseU(x => some(x))
      s.map(_.take(3)) must be_===(some(List(0, 1, 2)))
    }
  }

  "stream" should {
    "apply effects in order" in {
      val s: Writer[String, Stream[Int]] = Stream(1, 2, 3).traverseU(x => Writer(x.toString, x))
      s.run must be_===(("123", Stream(1, 2, 3)))
    }

    // ghci> import Data.Traversable
    // ghci> traverse (\x -> if x < 3 then Just x else Nothing) [1 ..]
    // Nothing
    "allow partial traversal" in {
      val stream = Stream.from(1)
      val s: Option[Stream[Int]] = stream.traverseU((x: Int) => if (x < 3) some(x) else none)
      s must be_===(none)
    }
  }

  "combos" should {
    "traverse large stream over trampolined StateT including IO" in {
      // Example usage from Eric Torreborre
      import scalaz.effect._

      val as = Stream.range(0, 100000)
      val state: State[Int, IO[Stream[Int]]] = as.traverseSTrampoline(a => State((s: Int) => (IO(a - s), a)))
      state.eval(0).unsafePerformIO.take(3) must be_===(Stream(0, 1, 1))
    }
  }

  import ScalazProperties.traverse

  checkAll("List", traverse.laws[List])
  checkAll("Stream", traverse.laws[Stream])
  checkAll("Option", traverse.laws[Option])
  checkAll("Id", traverse.laws[Id])

  // checkTraverseLaws[NonEmptyList, Int]
  // checkTraverseLaws[({type λ[α]=Validation[Int, α]})#λ, Int]
  // checkTraverseLaws[Zipper, Int]
  // checkTraverseLaws[LazyOption, Int]

}
