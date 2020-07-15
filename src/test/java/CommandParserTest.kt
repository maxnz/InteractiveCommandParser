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

        parser.badCommandAction = { _, msg ->
            testStr = msg
        }

        parser.parseCommand("bad", 2)

        assertTrue { testStr == "Bad Command: bad" }
    }

    @Test
    fun testTooManyCommands() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        var testStr = ""

        parser.tooManyMatchingCommandsAction = { _, msg ->
            testStr = msg
        }

        parser.command("test") {}
        parser.command("tea") {}

        parser.parseCommand("te", 2)

        assertTrue { testStr == "Partial command te matches more than one command: [tea, test]" }
    }

    @Test
    fun testNeedsSubCommand() {
        val r = ReceiverClass()
        val parser = CommandParser<ReceiverClass, Int>(r)
        var testStr = ""

        parser.commandNeedsSubCommandAction = { _, msg ->
            testStr = msg
        }

        parser.commandGroup("test") {
            command("sub1") {}
            command("sub2") {}
        }

        parser.parseCommand("test", 2)

        assertTrue { testStr == "Command test requires a sub-command: [sub1, sub2]" }
    }

}
