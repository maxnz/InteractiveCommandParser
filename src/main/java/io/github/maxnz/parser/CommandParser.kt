/*
 *  Copyright (c) 2020 Max Narvaez
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package io.github.maxnz.parser

import org.pmw.tinylog.Logger

/**
 * The command parser
 *
 * @param R The receiver type for all actions
 * @param A The type of the argument that will be passed to each action
 * @property receiver The receiver to run all actions on
 */
class CommandParser<R, A>(internal val receiver: R) {

    abstract inner class Command(val identifier: String, group: CommandGroup? = null) {

        /**
         * The full identifier of this command, meaning it includes it's parent's
         * full identifier (if it has a parent), e.g. the sub-command "list" of
         * "connections" would have an identifier of "list" and a fullIdentifier
         * of "connections list", while the sub-command "all" of "list" would have
         * a full identifier of "connections list all"
         */
        val fullIdentifier: String =
            if (group != null) "${group.fullIdentifier} $identifier" else identifier

        var description: String = ""

        abstract fun helpMessage(): String

        abstract fun parseArgs(args: List<String>, arg: A): ParseResult
    }

    /**
     * Represents a command
     *
     * @property identifier The string that the user passes to run this command
     * @param group The command that this command is a sub-command of
     * (null means it is a top-level command)
     */
    inner class RunnableCommand(
        identifier: String,
        group: CommandGroup? = null
    ) : Command(identifier, group) {
        /**
         * The action to run when this command is called.
         * If this command only has sub-commands, leave this null.
         */
        lateinit var action: R.(A, List<String>) -> Unit

        /**
         * The string that represents the arguments to pass in the help message
         */
        var argHelpStr: String? = null

        /**
         * If the arguments are required or not
         */
        var requireArg: Boolean = false

        /**
         * The parser this command or sub-command is a part of
         */
        val parser: CommandParser<R, A>
            get() = this@CommandParser

        /**
         * Create the help message for this command and its sub-commands
         */
        override fun helpMessage(): String {
            var msg = fullIdentifier

            when {
                argHelpStr == null -> {}
                argHelpStr != null && requireArg -> {
                    msg += " $argHelpStr"
                }
                argHelpStr != null && !requireArg -> {
                    msg += " [$argHelpStr]"
                }
            }

            if (msg.length < HELP_INDENT_LEN)
                msg = msg.padEnd(HELP_INDENT_LEN) + description
            else
                msg += "\n${" ".repeat(HELP_INDENT_LEN)}$description"

            return msg
        }

        /**
         * Parse a string of space-delimited identifiers and call the
         * appropriate sub-command or execute the `action`
         *
         * @param args The identifiers and arguments to parse
         * @param arg The argument of type `A` that will be passed to the action
         * @return The result of parsing the command (see [ParseResult])
         */
        override fun parseArgs(args: List<String>, arg: A): ParseResult {
            // TODO: Check args

            action.invoke(receiver, arg, args)
            return ParseResult.SUCCESS
        }
    }

    inner class CommandGroup(
        identifier: String,
        group: CommandGroup? = null
    ) : Command(identifier, group) {
        val parser: CommandParser<R, A>
            get() = this@CommandParser

        val commands: MutableList<Command> = mutableListOf()

        /**
         * A list of the identifiers of the sub-commands associated with this command
         */
        val commandIdentifiers: List<String>
            get() = commands.map { it.identifier }

        override fun helpMessage(): String {
            var msg = "$fullIdentifier {${commandIdentifiers.joinToString(", ")}}"

            if (msg.length < HELP_INDENT_LEN)
                msg = msg.padEnd(HELP_INDENT_LEN) + description
            else
                msg += "\n${" ".repeat(HELP_INDENT_LEN)}$description"

            msg += "\n" + commands.sortedBy { it.identifier }.joinToString("\n") { it.helpMessage() }

            return msg
        }

        override fun parseArgs(args: List<String>, arg: A): ParseResult {
            val firstIdentifier = args.getOrNull(0) ?: return ParseResult.COMMAND_NEEDS_SUBCOMMAND

            val possibleCommands =
                commands.filter { it.identifier.startsWith(firstIdentifier) }
            return try {
                possibleCommands.single().parseArgs(args.drop(1), arg)
            } catch (e: NoSuchElementException) {
                ParseResult.BAD_COMMAND
            } catch (e: IllegalArgumentException) {
                ParseResult.TOO_MANY_MATCHING_COMMANDS
            }
        }

    }

    /**
     * A list of the top-level commands
     */
    internal val commands = mutableListOf<Command>()

    /**
     * A list of the identifiers of the top-level commands
     */
    val commandIdentifiers: List<String>
        get() = commands.map { it.identifier }

    // Create the "help" command
    init {
        command("help") {
            description = "Print this help message"

            action { arg, _ ->
                val msg =
                    commands
                        .sortedBy { it.identifier }
                        .joinToString("\n") { it.helpMessage() }

                helpMessageAction(arg, msg)
            }
        }
    }

    /**
     * The action to run to print the help message
     */
    var helpMessageAction: R.(A, String) -> Unit = { _, msg ->
        Logger.info(msg)
    }

    /**
     * Create the string that alerts the user that they typed a bad command
     *
     * @param cmd The string they gave
     */
    fun badCommandString(cmd: String): String = "Bad Command: $cmd"

    /**
     * The action to run to alert the user that they typed a bad command
     */
    var badCommandAction: R.(A, String) -> Unit = { _, cmd ->
        Logger.error(badCommandString(cmd))
    }

    /**
     * Create the string that alerts the user that they need to specify a
     * sub-command after their command
     *
     * @param cmd The command that needs a sub-command
     */
    fun commandNeedsSubCommandString(cmd: CommandGroup): String =
        "Command ${cmd.fullIdentifier} requires a sub-command: ${cmd.commandIdentifiers}"

    /**
     * The action to run to alert the user that they need a sub-command after
     * their command
     */
    var commandNeedsSubCommandAction: R.(A, CommandGroup) -> Unit = { _, cmd ->
        Logger.error(commandNeedsSubCommandString(cmd))
    }

    fun tooManyMatchingCommandsString(cmd: String, possible: List<String>): String =
        "Partial command $cmd matches more than one command: $possible"

    var tooManyMatchingCommandsAction: R.(A, String, List<String>) -> Unit =
        { _, cmd, possible ->
            Logger.error(tooManyMatchingCommandsString(cmd, possible))
        }

    /**
     * Parse a string and run the appropriate command
     *
     * @param command The string to parse
     * @param arg The argument to pass to whatever action is run
     */
    fun parseCommand(command: String, arg: A) {
        parseCommand(command.split(" ").filterNot { it == "" }, arg)
    }

    private fun parseCommand(args: List<String>, arg: A) {
        val firstIdentifier = args[0]
        val possibleCommands =
            commands.filter { it.identifier.startsWith(firstIdentifier) }

        val result: ParseResult = try {
            possibleCommands.single().parseArgs(args.drop(1), arg)
        } catch (e: NoSuchElementException) {
            ParseResult.BAD_COMMAND
        } catch (e: IllegalArgumentException) {
            ParseResult.TOO_MANY_MATCHING_COMMANDS
        }

        when (result) {
            ParseResult.SUCCESS -> return
            ParseResult.BAD_COMMAND -> badCommandAction(receiver, arg, firstIdentifier)
            ParseResult.COMMAND_NEEDS_SUBCOMMAND ->
                commandNeedsSubCommandAction(receiver, arg, possibleCommands.single() as CommandGroup)
            ParseResult.TOO_MANY_MATCHING_COMMANDS ->
                tooManyMatchingCommandsAction(receiver, arg, firstIdentifier,
                    possibleCommands.map { it.identifier })
        }
    }
}
