import io.github.maxnz.parser.*
import org.junit.Test
import kotlin.test.assertTrue

class CommandParserTest {

    class ReceiverClass(val x: Int)

    @Test
    fun testCreateCommandParser() {
        val r = ReceiverClass(5)
        val parser = CommandParser<ReceiverClass, Int>(r)

        assertTrue { parser.receiver === r }
        assertTrue { parser.commandIdentifiers == listOf("help") }
        assertTrue { parser.receiver.x == 5 }
    }

    @Test
    fun testCreateCommand() {
        val r = ReceiverClass(5)
        val parser = CommandParser<ReceiverClass, Int>(r)

        val test = parser.command("test") {
            action { _, _ -> }
        }

        assertTrue { parser.commandIdentifiers == listOf("help", "test") }
        assertTrue { test.identifier == "test" }
        assertTrue { test.fullIdentifier == "test" }
    }

    @Test
    fun testCreateSubCommand() {
        val r = ReceiverClass(5)
        val parser = CommandParser<ReceiverClass, Int>(r)
        lateinit var sub: CommandParser<ReceiverClass, Int>.Command
        val test = parser.command("test") {
            sub = subCommand("sub") {

                action { _, _ -> }
            }
        }

        assertTrue { parser.commandIdentifiers == listOf("help", "test") }
        assertTrue { test.subCommandIdentifiers == listOf("sub") }
        assertTrue { sub.identifier == "sub" }
        assertTrue { sub.fullIdentifier == "test sub" }

    }

    @Test
    fun testHelp() {
        val r = ReceiverClass(5)
        val parser = CommandParser<ReceiverClass, Int>(r)

        val test1 = parser.command("test1") {
            description = "Test command 1"
        }

        assertTrue {
            test1.helpMessage() ==
                    "test1${" ".repeat(HELP_INDENT_LEN - 5)}Test command 1"
        }


        val test2 = parser.command("testTestTestTestTest2") {
            description = "Test command 2"
        }

        assertTrue {
            test2.helpMessage() ==
                    "testTestTestTestTest2\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 2"
        }


        val test3 = parser.command("t".repeat(HELP_INDENT_LEN)) {
            description = "Test command 3"
        }

        assertTrue {
            test3.helpMessage() ==
                    "${"t".repeat(HELP_INDENT_LEN)}\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 3"
        }


        val test4 = parser.command("test4") {
            description = "Test command 4"
            argHelpStr = "T"
            requireArg = true
        }

        assertTrue {
            test4.helpMessage() ==
                    "test4 T${" ".repeat(HELP_INDENT_LEN - 7)}Test command 4"
        }


        val test5 = parser.command("testTestTest5") {
            description = "Test command 5"
            argHelpStr = "TTTTTT"
            requireArg = true
        }

        assertTrue {
            test5.helpMessage() ==
                    "testTestTest5 TTTTTT\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 5"
        }


        val test6 = parser.command("test6") {
            description = "Test command 6"
            argHelpStr = "TT"
        }

        assertTrue {
            test6.helpMessage() ==
                    "test6 [TT]${" ".repeat(HELP_INDENT_LEN - 10)}Test command 6"
        }


        lateinit var sub71: CommandParser<ReceiverClass, Int>.Command
        lateinit var sub72: CommandParser<ReceiverClass, Int>.Command

        val test7 = parser.command("test7") {
            description = "Test command 7"

            sub71 = subCommand("sub7-1") {
                description = "SubCommand 7-1"
            }
            sub72 = subCommand("sub7-2") {
                description = "SubCommand 7-2"
            }
        }

        assertTrue {
            sub71.helpMessage() ==
                    "test7 sub7-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 7-1"
        }
        assertTrue {
            sub72.helpMessage() ==
                    "test7 sub7-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 7-2"
        }
        assertTrue {
            test7.helpMessage() ==
                    "test7 {sub7-1, sub7-2}\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 7\n" +
                    "test7 sub7-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 7-1\n" +
                    "test7 sub7-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 7-2"
        }


        lateinit var sub81: CommandParser<ReceiverClass, Int>.Command
        lateinit var sub82: CommandParser<ReceiverClass, Int>.Command

        val test8 = parser.command("test8") {
            description = "Test command 8"
            argHelpStr = "TT"
            requireArg = true

            action { _, _ -> }

            sub81 = subCommand("sub8-1") {
                description = "SubCommand 8-1"
            }
            sub82 = subCommand("sub8-2") {
                description = "SubCommand 8-2"
            }
        }

        assertTrue {
            sub81.helpMessage() ==
                    "test8 sub8-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 8-1"
        }
        assertTrue {
            sub82.helpMessage() ==
                    "test8 sub8-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 8-2"
        }
        assertTrue {
            test8.helpMessage() ==
                    "test8 {TT, sub8-1, sub8-2}\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 8\n" +
                    "test8 sub8-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 8-1\n" +
                    "test8 sub8-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 8-2"
        }


        lateinit var sub91: CommandParser<ReceiverClass, Int>.Command
        lateinit var sub92: CommandParser<ReceiverClass, Int>.Command

        val test9 = parser.command("test9") {
            description = "Test command 9"
            argHelpStr = "TT"
            requireArg = false

            action { _, _ -> }

            sub91 = subCommand("sub9-1") {
                description = "SubCommand 9-1"
            }
            sub92 = subCommand("sub9-2") {
                description = "SubCommand 9-2"
            }
        }

        assertTrue {
            sub91.helpMessage() ==
                    "test9 sub9-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 9-1"
        }
        assertTrue {
            sub92.helpMessage() ==
                    "test9 sub9-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 9-2"
        }
        assertTrue {
            test9.helpMessage() ==
                    "test9 [TT, sub9-1, sub9-2]\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 9\n" +
                    "test9 sub9-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 9-1\n" +
                    "test9 sub9-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 9-2"
        }
    }
}
