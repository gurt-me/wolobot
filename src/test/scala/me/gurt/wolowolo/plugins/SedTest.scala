package me.gurt.wolowolo.plugins

import org.scalatest._
import flatspec._
import matchers._
import me.gurt.wolowolo.dsl.PrivMsg
import org.jibble.pircbot.Colors.BOLD

class SedTest extends AnyFlatSpec with should.Matchers with BasePluginTest {
  val alice = "Alice"

  def replyOf(nick: String, msg: String): String =
    handlePrivMsg(nick, msg).get
      .asInstanceOf[PrivMsg]
      .message
      .filterNot(_.toString == BOLD)

  def sedSelf(nick: String, expect: String, sedExpr: String): Assertion =
    assertResult(s"<$nick> $expect")(replyOf(nick, sedExpr))

  alice can "sed her lines with or without" in {
    sedSelf(alice, "v", "s/tWo/v/")
  }
  alice can "or without trailing slash." in {
    sedSelf(alice, "v", "s/tWo/v")
  }
  alice can "edit case-insensitively with /i" in {
    sedSelf(alice, "v", "s/two/v/i")
  }
  alice can "perform global substitutions with /g" in {
    handlePrivMsg(alice, "aaa")
    sedSelf(alice, "bbbbbbbbb", "s/a/bbb/g")
  }
  "Sed" should "keep more than 1 line" in {
    handlePrivMsg(alice, "aaa")
    handlePrivMsg(alice, "bbb")
    sedSelf(alice, "ccc", "s/A/c/gi")
  }

  val bob = "Bob"
  bob can s"change $alice's lines with" in {
    assertResult(s"$bob thinks $alice meant: i suck")(replyOf(bob, s"$alice: s/tWo/i suck/"))
  }

  alice can "sed iteratively" in {
    handlePrivMsg(alice, "s/tWo/three")
    sedSelf(alice, "four", "s/three/four/")
  }

  override def withFixture(test: NoArgTest) = { // Define a shared fixture
    // Shared setup (run at beginning of each test)
    plugin = new Sed
    handlePrivMsg(alice, "zero")
    handlePrivMsg(alice, "one")
    handlePrivMsg(alice, "tWo")
    try test()
    finally {
      // Shared cleanup (run at end of each test)
      plugin = null
    }
  }
}
