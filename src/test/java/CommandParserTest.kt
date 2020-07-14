import io.github.maxnz.parser.*
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CommandParserTest {

    class ReceiverClass(val x: Int = 0)

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
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)

        val test = parser.command("test") {
            action { _, _ -> }
        }

        assertTrue { parser.commandIdentifiers == listOf("help", "test") }
        assertTrue { test.identifier == "test" }
        assertTrue { test.fullIdentifier == "test" }
    }

    @Test
    fun testCreateCommandGroup() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)

        val test = parser.commandGroup("test") {}

        assertTrue { parser.commandIdentifiers == listOf("help", "test") }
        assertTrue { test.identifier == "test" }
        assertTrue { test.fullIdentifier == "test" }
    }

    @Test
    fun testCreateSubCommand() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        lateinit var sub: CommandParser<ReceiverClass, Int>.RunnableCommand
        val test = parser.commandGroup("test") {
            sub = command("sub") {
                action { _, _ -> }
            }
        }

        assertTrue { parser.commandIdentifiers == listOf("help", "test") }
        assertTrue { test.commandIdentifiers == listOf("sub") }
        assertTrue { sub.identifier == "sub" }
        assertTrue { sub.fullIdentifier == "test sub" }

    }

    @Test
    fun testCreateSubSubCommand() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        lateinit var sub: CommandParser<ReceiverClass, Int>.CommandGroup
        lateinit var subsub: CommandParser<ReceiverClass, Int>.RunnableCommand

        val test = parser.commandGroup("test") {
            sub = commandGroup("sub") {
                subsub = command("sub-sub") {
                    action { _, _ -> }
                }
            }
        }

        assertTrue { parser.commandIdentifiers == listOf("help", "test") }
        assertTrue { test.commandIdentifiers == listOf("sub") }
        assertTrue { sub.commandIdentifiers == listOf("sub-sub") }
        assertTrue { subsub.identifier == "sub-sub" }
        assertTrue { subsub.fullIdentifier == "test sub sub-sub" }
    }



//    @Test
//    fun testHelp() {
//        val r = ReceiverClass(5)
//        val parser = CommandParser<ReceiverClass, Int>(r)
//
//        val test1 = parser.command("test1") {
//            description = "Test command 1"
//        }
//
//        assertTrue {
//            test1.helpMessage() ==
//                    "test1${" ".repeat(HELP_INDENT_LEN - 5)}Test command 1"
//        }
//
//
//        val test2 = parser.command("testTestTestTestTest2") {
//            description = "Test command 2"
//        }
//
//        assertTrue {
//            test2.helpMessage() ==
//                    "testTestTestTestTest2\n" +
//                    "${" ".repeat(HELP_INDENT_LEN)}Test command 2"
//        }
//
//
//        val test3 = parser.command("t".repeat(HELP_INDENT_LEN)) {
//            description = "Test command 3"
//        }
//
//        assertTrue {
//            test3.helpMessage() ==
//                    "${"t".repeat(HELP_INDENT_LEN)}\n" +
//                    "${" ".repeat(HELP_INDENT_LEN)}Test command 3"
//        }
//
//
//        val test4 = parser.command("test4") {
//            description = "Test command 4"
//            argHelpStr = "T"
//            requireArg = true
//        }
//
//        assertTrue {
//            test4.helpMessage() ==
//                    "test4 T${" ".repeat(HELP_INDENT_LEN - 7)}Test command 4"
//        }
//
//
//        val test5 = parser.command("testTestTest5") {
//            description = "Test command 5"
//            argHelpStr = "TTTTTT"
//            requireArg = true
//        }
//
//        assertTrue {
//            test5.helpMessage() ==
//                    "testTestTest5 TTTTTT\n" +
//                    "${" ".repeat(HELP_INDENT_LEN)}Test command 5"
//        }
//
//
//        val test6 = parser.command("test6") {
//            description = "Test command 6"
//            argHelpStr = "TT"
//        }
//
//        assertTrue {
//            test6.helpMessage() ==
//                    "test6 [TT]${" ".repeat(HELP_INDENT_LEN - 10)}Test command 6"
//        }
//
//
//        lateinit var sub71: CommandParser<ReceiverClass, Int>.RunnableCommand
//        lateinit var sub72: CommandParser<ReceiverClass, Int>.RunnableCommand
//
//        val test7 = parser.command("test7") {
//            description = "Test command 7"
//
//            sub71 = subCommand("sub7-1") {
//                description = "SubCommand 7-1"
//            }
//            sub72 = subCommand("sub7-2") {
//                description = "SubCommand 7-2"
//            }
//        }
//
//        assertTrue {
//            sub71.helpMessage() ==
//                    "test7 sub7-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 7-1"
//        }
//        assertTrue {
//            sub72.helpMessage() ==
//                    "test7 sub7-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 7-2"
//        }
//        assertTrue {
//            test7.helpMessage() ==
//                    "test7 {sub7-1, sub7-2}\n" +
//                    "${" ".repeat(HELP_INDENT_LEN)}Test command 7\n" +
//                    "test7 sub7-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 7-1\n" +
//                    "test7 sub7-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 7-2"
//        }
//
//
//        lateinit var sub81: CommandParser<ReceiverClass, Int>.RunnableCommand
//        lateinit var sub82: CommandParser<ReceiverClass, Int>.RunnableCommand
//
//        val test8 = parser.command("test8") {
//            description = "Test command 8"
//            argHelpStr = "TT"
//            requireArg = true
//
//            action { _, _ -> }
//
//            sub81 = subCommand("sub8-1") {
//                description = "SubCommand 8-1"
//            }
//            sub82 = subCommand("sub8-2") {
//                description = "SubCommand 8-2"
//            }
//        }
//
//        assertTrue {
//            sub81.helpMessage() ==
//                    "test8 sub8-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 8-1"
//        }
//        assertTrue {
//            sub82.helpMessage() ==
//                    "test8 sub8-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 8-2"
//        }
//        assertTrue {
//            test8.helpMessage() ==
//                    "test8 {TT, sub8-1, sub8-2}\n" +
//                    "${" ".repeat(HELP_INDENT_LEN)}Test command 8\n" +
//                    "test8 sub8-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 8-1\n" +
//                    "test8 sub8-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 8-2"
//        }
//
//
//        lateinit var sub91: CommandParser<ReceiverClass, Int>.RunnableCommand
//        lateinit var sub92: CommandParser<ReceiverClass, Int>.RunnableCommand
//
//        val test9 = parser.command("test9") {
//            description = "Test command 9"
//            argHelpStr = "TT"
//            requireArg = false
//
//            action { _, _ -> }
//
//            sub91 = subCommand("sub9-1") {
//                description = "SubCommand 9-1"
//            }
//            sub92 = subCommand("sub9-2") {
//                description = "SubCommand 9-2"
//            }
//        }
//
//        assertTrue {
//            sub91.helpMessage() ==
//                    "test9 sub9-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 9-1"
//        }
//        assertTrue {
//            sub92.helpMessage() ==
//                    "test9 sub9-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 9-2"
//        }
//        assertTrue {
//            test9.helpMessage() ==
//                    "test9 [TT, sub9-1, sub9-2]\n" +
//                    "${" ".repeat(HELP_INDENT_LEN)}Test command 9\n" +
//                    "test9 sub9-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 9-1\n" +
//                    "test9 sub9-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 9-2"
//        }
//
//
//        lateinit var subA1: CommandParser<ReceiverClass, Int>.RunnableCommand
//        lateinit var subA2: CommandParser<ReceiverClass, Int>.RunnableCommand
//
//        val testA = parser.command("testA") {
//            description = "Test command A"
//
//            action { _, _ -> }
//
//            subA1 = subCommand("subA-1") {
//                description = "SubCommand A-1"
//            }
//            subA2 = subCommand("subA-2") {
//                description = "SubCommand A-2"
//            }
//        }
//
//        assertTrue {
//            subA1.helpMessage() ==
//                    "testA subA-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand A-1"
//        }
//        assertTrue {
//            subA2.helpMessage() ==
//                    "testA subA-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand A-2"
//        }
//        assertTrue {
//            testA.helpMessage() ==
//                    "testA [subA-1, subA-2]\n" +
//                    "${" ".repeat(HELP_INDENT_LEN)}Test command A\n" +
//                    "testA subA-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand A-1\n" +
//                    "testA subA-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand A-2"
//        }
//    }

    @Test
    fun testParseCommand() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        var testBoolean = false

        parser.command("test") {
            action { _, _ -> testBoolean = true }
        }

        parser.parseCommand("test", 3)

        assertTrue { testBoolean }
    }

    @Test
    fun testParseCommandBadCommandDefinition() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)

        parser.command("test") {}

        assertFailsWith<UninitializedPropertyAccessException> {
            parser.parseCommand("test", 4)
        }
    }

    @Test
    fun testParseArgsSubCommand() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        var testBoolean = false

        parser.commandGroup("test") {
            command("sub-test") {
                action { _, _ -> testBoolean = true }
            }
        }

        parser.parseCommand("test sub-test", 2)

        assertTrue { testBoolean }
    }


}
