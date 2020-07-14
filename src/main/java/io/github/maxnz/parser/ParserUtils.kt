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

const val HELP_INDENT_LEN = 18

fun <T, A> CommandParser<T, A>.RunnableCommand.action(action: T.(A, List<String>) -> Unit) {
    this.action = action
}

fun <T, A> CommandParser<T, A>.CommandGroup.command(
    identifier: String,
    init: CommandParser<T, A>.RunnableCommand.() -> Unit
): CommandParser<T, A>.RunnableCommand {
    val newCommand = parser.RunnableCommand(identifier, this)
    newCommand.init()
    commands.add(newCommand)
    return newCommand
}

fun <T, A> CommandParser<T, A>.command(
    identifier: String,
    init: CommandParser<T, A>.RunnableCommand.() -> Unit
): CommandParser<T, A>.RunnableCommand {
    val newCommand = this.RunnableCommand(identifier)
    newCommand.init()
    commands.add(newCommand)
    return newCommand
}

fun <T, A> CommandParser<T, A>.CommandGroup.commandGroup(
    identifier: String,
    init: CommandParser<T, A>.CommandGroup.() -> Unit
): CommandParser<T, A>.CommandGroup {
    val newCommand = parser.CommandGroup(identifier, this)
    newCommand.init()
    commands.add(newCommand)
    return newCommand
}

fun <T, A> CommandParser<T, A>.commandGroup(
    identifier: String,
    init: CommandParser<T, A>.CommandGroup.() -> Unit
): CommandParser<T, A>.CommandGroup {
    val newCommand = this.CommandGroup(identifier)
    newCommand.init()
    commands.add(newCommand)
    return newCommand
}
