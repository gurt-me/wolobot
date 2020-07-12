package me.gurt.wolowolo.plugins

import cats.data.Chain
import me.gurt.wolowolo.Handler

class Useless extends Plugin {

  def handle(net: String): Handler =
    Chain[Handler](
      Command("bots", (_: String) => "Reporting in!!! [Scala]"),
      Command(
        "source",
        (_: String) => "Wolobot is a piece of shit see https://github.com/gurt-me/wolobot",
      ),
    ) ++ Chain(
      Seq("eterius", "etarius") ->
        "shut up lonely virgin retard gypsy trannylover mentally disordered thin skinned pedo broken mom'd dehop'd bridgejumping nofamily nogf nohugs nogod",
      Seq("ponyo", "anayx") ->
        "shut up pajeetress fishfucker mermaid wannabe nobf nosex kamasutra compulsive ops abuser NEET who spends her lonely days trying to fish for attention from dudes on Internets Relay Chat but gets ignored",
      Seq("japex") ->
        "retard raped boiled cant-taco PLZ-NO-OUTSIDE THE-FBI-WANTED-ME childhoodless noparents nogf-except-for-the-rapistgf skinny autist pedo BAWWWW MUH LIFE SO HARD",
    ).map[Handler] {
      case (cmds, str) =>
        Command(
          cmds,
          (args: String) => if (args.isEmpty) None else Some(s"$str $args"),
          Some("<nick of loser>"),
        )
    }

}
