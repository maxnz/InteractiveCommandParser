# Interactive Command Line Parser

Parse commands from an interactive command line.
When a valid command is entered, the associated "action" will be run.
Commands can be abbreviated as much as desired provided they still identify a unique command.

## Usage

### Creating the Parser

A parser takes two template arguments:
- The type of the receiver that the actions will be run on
- The type of the argument that will be passed to all actions 

```kotlin
class Receiver          // Example class

val r = Receiver()

val parser = CommandParser<Receiver, Int>(r)
```

### Creating Commands

To specify a command that can be entered, use the `command` builder.
```kotlin
parser.command("cmd") {}
```

Use the `action` builder to set what happens when the command is run.
The action is passed two arguments:
- The argument that was passed to the parser
- A list of the remaining arguments on the command line

*Note that the action must be defined before the command is called, otherwise an `UninitializedPropertyAccessException` will be thrown.*

```kotlin
parser.command("cmd") {
    
    // arg is the argument that was passed to the parser
    // args is a list of the remaining arguments on the command line
    action { arg, args ->
        // do something
    }
}
```

To specify a command that has sub-commands, create a command group with the `commandGroup` builder.
Then use the `command` builder to specify the sub-commands.

```kotlin
parser.commandGroup("grp") {
    command("sub") {}           // run using "grp sub"
}
```

### Parsing Commands

To parse a command, pass the string containing the command to `parseString()`, along with the value to pass to the action.
Any remaining arguments will be passed to the action in a list.

```kotlin
parser.parseString("cmd", 5)            // The 5 will be passed to the action as arg
parser.parseString("cmd arg1 arg2", 5)  // arg1 and arg2 will be passed as part of args
parser.parseString("grp sub", 5)        // Call a sub-command
```

### Parsing Errors

There are four outcomes from parsing identified in the `ParseResult` enum:
- `SUCCESS`: A command was found and its action was executed
- `BAD_COMMAND`: A command could not be found that matched the arguments given
- `COMMAND_NEEDS_SUBCOMMAND`: The command called is a command group and requires a sub-command
- `TOO_MANY_MATCHING_COMMANDS`: The command called does not match a unique command (e.g. given two defined commands `test` and `tea`, passing `te` does not identify a unique command, while passing `tes` would identify `test`)


#### Parsing Error Callbacks

The three error cases have callbacks that are used to print or send the error message to the right place.
It is up to the user to set these callbacks, whether it is printing to the terminal or sending to a socket or something else.

The callbacks are passed two arguments:
- The argument that was passed to the parser that would have been passed to a command's action
- The message detailing the error

The callbacks are as follows:
- `BAD_COMMAND` -> `badCommandAction`
- `COMMAND_NEEDS_SUBCOMMAND` -> `commandNeedsSubCommandAction`
- `TOO_MANY_MATCHING_COMMANDS` -> `tooManyMatchingCommandsAction`

##### Help Message Callback

There is a fourth callback for printing the help message called `helpMessageAction`.

This is done because the help command is pre-defined and this allows the user to specify what that command will actually do with the message.

### Help Messages

The library can automatically generate a help message for your parser when you parse the command `help`.
The commands are sorted alphabetically in the help message.

To set a description for a command, set the `description` property.

```kotlin
parser.command("ex") {
    description = "Describe the command here"
}

// This also works for command groups
parser.commandGroup("ex-grp") {
    description = "Describe the group of commands here"
}
```

If your command takes one or more arguments, you can specify that using the `argHelpStr` property.
*Note that the library does NOT check the number of or type of arguments, that is left to the user.*

```kotlin
parser.command("i-have-args") {
    argHelpStr = "ARG"
}
```

A paragraph can be added at the beginning of the help message by setting the parser's `helpParagraph` property.

```kotlin
parser.helpParagraph = "Help paragraph for this parser"
```

To specify where to send the help message, set the `helpMessageAction` (see [Help Message Callback](https://github.com/maxnz/InteractiveCommandParser#help-message-callback)).

#### Example

The following parser definition would create the following help message:

```kotlin
parser.apply {
    helpParagraph = "Test Commands"    

    command("example1") {
        description = "Test command 1"
    }
    command("testTestTestTestTest2") {
        description = "Test command 2"
    }
    command("tttttttttttttttttt") {
        description = "Test command 3"
    }
    command("test4") {
        description = "Test command 4"
        argHelpStr = "T"
    }
    command("testTestTest5") {
        description = "Test command 5"
        argHelpStr = "TTTTTT"
    }
    commandGroup("test6") {
        description = "Test command 6"

        command("sub6-1") {
            description = "SubCommand 6-1"
        }
        command("sub6-2") {
            description = "SubCommand 6-2"
        }
    }
    commandGroup("tt") {
        description = "Test command 7"

        command("ttt") {
            description = "SubCommand 7"
        }
    }
}
```

```
Test Commands

Commands:
example1          Test command 1
help              Print this help message
test4 T           Test command 4
test6 {sub6-1, sub6-2}
                  Test command 6
test6 sub6-1      SubCommand 6-1
test6 sub6-2      SubCommand 6-2
testTestTest5 TTTTTT
                  Test command 5
testTestTestTestTest2
                  Test command 2
tt {ttt}          Test command 7
tt ttt            SubCommand 7
tttttttttttttttttt
                  Test command 3
```
