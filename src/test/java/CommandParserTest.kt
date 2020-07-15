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

        assertTrue { ParseResult.SUCCESS.message().isEmpty() }
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


    @Test
    fun testBadCommand() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        var testStr = ""

        parser.parseCommand("bad", 2)

        parser.badCommandAction = { _, msg ->
            testStr = msg
        }

        parser.parseCommand("bad", 2)

        assertTrue { testStr == "Bad Command: bad" }
    }

    @Test
    fun testBadSubCommand() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        var testStr = ""

        parser.commandGroup("test") {
            command("sub") {}
        }

        parser.parseCommand("test sub3", 2)

        parser.badCommandAction = { _, msg ->
            testStr = msg
        }

        parser.parseCommand("test sub3", 2)

        assertTrue { testStr == "Bad Command: test sub3" }
    }

    @Test
    fun testTooManyCommands() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        var testStr = ""

        parser.command("test") {}
        parser.command("tea") {}

        parser.parseCommand("te", 2)

        parser.tooManyMatchingCommandsAction = { _, msg ->
            testStr = msg
        }

        parser.parseCommand("te", 2)

        assertTrue { testStr == "Partial command te matches more than one command: [tea, test]" }
    }

    @Test
    fun testTooManySubCommands() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        var testStr = ""

        parser.commandGroup("test") {
            command("test") {}
            command("tea") {}
        }

        parser.parseCommand("test te", 2)

        parser.tooManyMatchingCommandsAction = { _, msg ->
            testStr = msg
        }

        parser.parseCommand("test te", 2)

        assertTrue { testStr == "Partial command test te matches more than one command: [tea, test]" }
    }

    @Test
    fun testNeedsSubCommand() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        var testStr = ""

        parser.commandGroup("test") {
            command("sub1") {}
            command("sub2") {}
        }

        parser.parseCommand("test", 2)

        parser.commandNeedsSubCommandAction = { _, msg ->
            testStr = msg
        }

        parser.parseCommand("test", 2)

        assertTrue { testStr == "Command test requires a sub-command: [sub1, sub2]" }
    }

    @Test
    fun testHelp() {
        val r = ReceiverClass()
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
        }

        assertTrue {
            test4.helpMessage() ==
                    "test4 T${" ".repeat(HELP_INDENT_LEN - 7)}Test command 4"
        }


        val test5 = parser.command("testTestTest5") {
            description = "Test command 5"
            argHelpStr = "TTTTTT"
        }

        assertTrue {
            test5.helpMessage() ==
                    "testTestTest5 TTTTTT\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 5"
        }


        lateinit var sub61: CommandParser<ReceiverClass, Int>.RunnableCommand
        lateinit var sub62: CommandParser<ReceiverClass, Int>.RunnableCommand

        val test6 = parser.commandGroup("test6") {
            description = "Test command 6"

            sub61 = command("sub6-1") {
                description = "SubCommand 6-1"
            }
            sub62 = command("sub6-2") {
                description = "SubCommand 6-2"
            }
        }

        assertTrue {
            sub61.helpMessage() ==
                    "test6 sub6-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 6-1"
        }
        assertTrue {
            sub62.helpMessage() ==
                    "test6 sub6-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 6-2"
        }
        assertTrue {
            test6.helpMessage() ==
                    "test6 {sub6-1, sub6-2}\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 6\n" +
                    "test6 sub6-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 6-1\n" +
                    "test6 sub6-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 6-2"
        }


        lateinit var sub7: CommandParser<ReceiverClass, Int>.RunnableCommand

        val test7 = parser.commandGroup("tt") {
            description = "Test command 7"

            sub7 = command("ttt") {
                description = "SubCommand 7"
            }
        }

        assertTrue {
            sub7.helpMessage() == "tt ttt${" ".repeat(HELP_INDENT_LEN - 6)}SubCommand 7"
        }

        assertTrue {
            test7.helpMessage() ==
                    "tt {ttt}${" ".repeat(HELP_INDENT_LEN - 8)}Test command 7\n" +
                    "tt ttt${" ".repeat(HELP_INDENT_LEN - 6)}SubCommand 7"
        }

        var testStr = ""

        parser.helpParagraph = "Test Commands"

        parser.parseCommand("help", 1)

        parser.helpMessageAction = {_, msg ->
            testStr = msg
        }

        parser.parseCommand("help", 2)

        assertTrue {
            testStr ==
                    "Test Commands\n\n" +
                    "Commands:\n" +
                    "help${" ".repeat(HELP_INDENT_LEN - 4)}Print this help message\n" +
                    "test1${" ".repeat(HELP_INDENT_LEN - 5)}Test command 1\n" +
                    "test4 T${" ".repeat(HELP_INDENT_LEN - 7)}Test command 4\n" +
                    "test6 {sub6-1, sub6-2}\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 6\n" +
                    "test6 sub6-1${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 6-1\n" +
                    "test6 sub6-2${" ".repeat(HELP_INDENT_LEN - 12)}SubCommand 6-2\n" +
                    "testTestTest5 TTTTTT\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 5\n" +
                    "testTestTestTestTest2\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 2\n" +
                    "tt {ttt}${" ".repeat(HELP_INDENT_LEN - 8)}Test command 7\n" +
                    "tt ttt${" ".repeat(HELP_INDENT_LEN - 6)}SubCommand 7\n" +
                    "${"t".repeat(HELP_INDENT_LEN)}\n" +
                    "${" ".repeat(HELP_INDENT_LEN)}Test command 3"
        }

    }

}
