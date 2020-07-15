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

enum class ParseResult {
    SUCCESS{
        override fun message() = ""
    },
    BAD_COMMAND {
        override fun message() =
            "Bad Command: $cmd"
    },
    COMMAND_NEEDS_SUBCOMMAND {
        override fun message() =
            "Command ${cmdGroup.fullIdentifier} requires a sub-command: ${cmdGroup.commandIdentifiers}"
    },
    TOO_MANY_MATCHING_COMMANDS {
        override fun message() =
            "Partial command $cmd matches more than one command: $possibleCommands"
    };

    var cmd = ""
    lateinit var cmdGroup: CommandParser<*, *>.CommandGroup
    lateinit var possibleCommands: List<String>

    abstract fun message(): String
}