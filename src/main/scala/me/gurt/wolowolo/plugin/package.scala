package me.gurt.wolowolo

import me.gurt.wolowolo.dsl._

package object plugin {
  // for command:      input message trailing the command  vvvvvv
  private[plugin] type MessageResponder = (Source, Target, String) => Option[Sendable]
  // for pattern:      full input message that matched it  ^^^^^^
  // it's a happy coincidence that we can share types
}
