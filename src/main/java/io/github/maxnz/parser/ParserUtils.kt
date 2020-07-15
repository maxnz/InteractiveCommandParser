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

fun <R, A> CommandParser<R, A>.RunnableCommand.action(action: R.(A, List<String>) -> Unit) {
    this.action = action
}

fun <R, A> CommandParser<R, A>.CommandGroup.command(
    identifier: String,
    init: CommandParser<R, A>.RunnableCommand.() -> Unit
): CommandParser<R, A>.RunnableCommand {
    val newCommand = parser.RunnableCommand(identifier, this)
    newCommand.init()
    commands.add(newCommand)
    return newCommand
}

fun <R, A> CommandParser<R, A>.command(
    identifier: String,
    init: CommandParser<R, A>.RunnableCommand.() -> Unit
): CommandParser<R, A>.RunnableCommand {
    val newCommand = this.RunnableCommand(identifier)
    newCommand.init()
    commands.add(newCommand)
    return newCommand
}

fun <R, A> CommandParser<R, A>.CommandGroup.commandGroup(
    identifier: String,
    init: CommandParser<R, A>.CommandGroup.() -> Unit
): CommandParser<R, A>.CommandGroup {
    val newCommand = parser.CommandGroup(identifier, this)
    newCommand.init()
    commands.add(newCommand)
    return newCommand
}

fun <R, A> CommandParser<R, A>.commandGroup(
    identifier: String,
    init: CommandParser<R, A>.CommandGroup.() -> Unit
): CommandParser<R, A>.CommandGroup {
    val newCommand = this.CommandGroup(identifier)
    newCommand.init()
    commands.add(newCommand)
    return newCommand
}
