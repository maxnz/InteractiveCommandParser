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

    /**
     * Represents a command
     *
     * @property identifier The string that the user passes to run this command
     * @param parentCommand The command that this command is a sub-command of
     * (null means it is a top-level command)
     */
    inner class Command(
        var identifier: String,
        parentCommand: Command? = null
    ) {
        var shortIdentifier: String? = null

        /**
         * The action to run when this command is called.
         * If this command only has sub-commands, leave this null.
         */
        internal var action: (R.(A, List<String>) -> Unit)? = null

        /**
         * The description that is printed in the help message
         */
        var description: String? = null

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
         * The sub-commands associated with this command
         */
        internal val subCommands: MutableList<Command> = mutableListOf()

        /**
         * A list of the identifiers of the sub-commands associated with this command
         */
        val subCommandIdentifiers: List<String>
            get() = subCommands.map { it.identifier }

        /**
         * The full identifier of this command, meaning it includes it's parent's
         * full identifier (if it has a parent), e.g. the sub-command "list" of
         * "connections" would have an identifier of "list" and a fullIdentifier
         * of "connections list", while the sub-command "all" of "list" would have
         * a full identifier of "connections list all"
         */
        val fullIdentifier: String =
            if (parentCommand != null)
                "${parentCommand.fullIdentifier} $identifier"
            else identifier

        /**
         * Create the help message for this command and its sub-commands
         */
        fun helpMessage(): String {
            var msg = ""

            fun addDescription() {
                if (msg.length < HELP_INDENT_LEN)
                    msg = msg.padEnd(HELP_INDENT_LEN) + "$description"
                else
                    msg += "\n${" ".repeat(HELP_INDENT_LEN)}$description"
            }

            fun addSubCommands() {
                msg += "\n" + subCommands.sortedBy { it.identifier }
                    .joinToString("\n") {
                        it.helpMessage()
                    }
            }

            msg += fullIdentifier

            when {
                subCommands.isEmpty() && argHelpStr == null -> {
                    addDescription()
                }
                subCommands.isEmpty() && argHelpStr != null && requireArg -> {
                    msg += " $argHelpStr"
                    addDescription()
                }
                subCommands.isEmpty() && argHelpStr != null && !requireArg -> {
                    msg += " [$argHelpStr]"
                    addDescription()
                }
                action == null -> {
                    msg += " {${subCommands.sortedBy { it.identifier }.joinToString(", ") { it.identifier }}}"

                    addDescription()
                    addSubCommands()
                }
                action != null -> {
                    msg += when {
                        argHelpStr != null && requireArg ->
                            " {$argHelpStr, ${subCommands.sortedBy { it.identifier }
                                .joinToString(", ") { it.identifier }}}"
                        argHelpStr != null && !requireArg ->
                            " [$argHelpStr, ${subCommands.sortedBy { it.identifier }
                                .joinToString(", ") { it.identifier }}]"
                        else ->
                            " [${subCommands.sortedBy { it.identifier }.joinToString(", ") { it.identifier }}]"
                    }

                    addDescription()
                    addSubCommands()
                }
            }

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
        fun parseArgs(args: List<String>, arg: A): ParseResult {
            check(action != null || subCommands.isNotEmpty()) { "Bad Command Definition" }
            val firstIdentifier = args.getOrNull(0)
            if (firstIdentifier != null && subCommands.isNotEmpty()) {
                val subCommand = subCommands.firstOrNull {
                    firstIdentifier.equals(it.identifier, ignoreCase = true)
                            || firstIdentifier.equals(it.shortIdentifier, ignoreCase = true)
                }
                if (subCommand != null) return subCommand.parseArgs(args.drop(1), arg)
                if (action == null) return ParseResult.NEEDS_SUBCOMMAND
            }

            return if (action == null) ParseResult.INVALID_COMMAND
            else {
                action?.invoke(receiver, arg, args)
                ParseResult.SUCCESS
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
                val msg = commands.sortedBy { it.identifier }.joinToString("\n") {
                    it.helpMessage()
                }
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
    fun invalidCommandString(cmd: String): String = "Bad Command: $cmd"

    /**
     * The action to run to alert the user that they typed a bad command
     */
    var invalidCommandAction: R.(A, String) -> Unit = { _, cmd ->
        Logger.error(invalidCommandString(cmd))
    }

    /**
     * Create the string that alerts the user that they need to specify a
     * sub-command after their command
     *
     * @param cmd The command that needs a sub-command
     */
    fun commandNeedsSubCommandString(cmd: Command): String =
        "Command ${cmd.fullIdentifier} requires a sub-command: ${cmd.subCommandIdentifiers}"

    /**
     * The action to run to alert the user that they need a sub-command after
     * their command
     */
    var commandNeedsSubCommandAction: R.(A, Command) -> Unit = { _, cmd ->
        Logger.error(commandNeedsSubCommandString(cmd))
    }

    /**
     * Parse a string and run the appropriate command
     *
     * @param command The string to parse
     * @param arg The argument to pass to whatever action is run
     */
    fun parseCommand(command: String, arg: A) {
        parseCommand(command.removePrefix("CMD :").toUpperCase().split(" "), arg)
    }

    private fun parseCommand(args: List<String>, arg: A) {
        val firstIdentifier = args.getOrNull(0) ?: return
        val cmd =
            commands
                .firstOrNull {
                    firstIdentifier.equals(it.identifier, ignoreCase = true)
                            || firstIdentifier.equals(it.shortIdentifier, ignoreCase = true)
                }
                ?: run { invalidCommandAction.invoke(receiver, arg, firstIdentifier); return }
        when (cmd.parseArgs(args.drop(1), arg)) {
            ParseResult.SUCCESS -> return
            ParseResult.NEEDS_SUBCOMMAND -> commandNeedsSubCommandAction(receiver, arg, cmd)
            ParseResult.INVALID_COMMAND -> invalidCommandAction(receiver, arg, firstIdentifier)
        }
    }
}
