package common;

import compiler.CompilerException;
import common.tree.*;

import java.util.ArrayList;

public class Parser {
    public static LoopCommand parse(String file) throws CompilerException {
        ArrayList<CommandType> commands = new ArrayList<>();

        String everything = file.replaceAll("[^<>\\+\\-\\[\\]\\.\\,]+", "");

        //Parse the file
        for (char c : everything.toCharArray()) {
            switch (c) {
                case '<':
                    commands.add(CommandType.LEFT);
                    break;
                case '>':
                    commands.add(CommandType.RIGHT);
                    break;
                case '+':
                    commands.add(CommandType.INCREMENT);
                    break;
                case '-':
                    commands.add(CommandType.DECREMENT);
                    break;
                case ',':
                    commands.add(CommandType.READ);
                    break;
                case '.':
                    commands.add(CommandType.WRITE);
                    break;
                case '[':
                    commands.add(CommandType.LOOPSTART);
                    break;
                case ']':
                    commands.add(CommandType.LOOPEND);
                    break;
                default:
                    throw new CompilerException("Invalid token '" + c + "'");
            }
        }

        return Parser.parse(commands.toArray(new CommandType[0]));
    }

    private static LoopCommand parse(CommandType[] commandsToProcess) {
        ArrayList<Command> commands = new ArrayList<>();

        int depth = 0;
        ArrayList<CommandType> depthCommands = new ArrayList<>();
        for (CommandType c : commandsToProcess) {
            if (c == CommandType.LOOPEND) {
                depth--;
                if (depth <= 0) {
                    commands.add(Parser.parse(depthCommands.toArray(new CommandType[0])));
                    depthCommands.clear();
                    continue;
                }
            }

            if (c == CommandType.LOOPSTART) {
                depth++;
                if (depth == 1) {
                    continue;
                }
            }

            if (depth > 0) {
                depthCommands.add(c);
            } else {
                commands.add(new NormalCommand(c));
            }
        }

        ArrayList<Command> reducedCommands = new ArrayList<>();
        CommandType currentType = null;
        for (Command c : commands) {
            if (c instanceof NormalCommand) {
                if (!((NormalCommand) c).getValue().equals(CommandType.READ) && !((NormalCommand) c).getValue().equals(CommandType.WRITE)) {
                    if (currentType == null || currentType != ((NormalCommand) c).getValue()) {
                        currentType = ((NormalCommand) c).getValue();
                        reducedCommands.add(new MultipleCommand(((NormalCommand) c).getValue(), 1));
                    } else {
                        ((MultipleCommand) reducedCommands.get(reducedCommands.size() - 1)).incrementCount();
                    }
                } else {
                    currentType = null;
                    reducedCommands.add(c);
                }
            } else {
                currentType = null;
                reducedCommands.add(c);
            }
        }

        return new LoopCommand(reducedCommands.toArray(new Command[0]));
    }
}
