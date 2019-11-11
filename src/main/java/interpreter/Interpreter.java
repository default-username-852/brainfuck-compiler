package interpreter;

import common.EOFMode;
import common.ParseException;
import common.Parser;
import common.tree.Command;
import common.tree.LoopCommand;
import common.tree.MultipleCommand;
import common.tree.NormalCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Interpreter {
	public static void main(String[] args) throws ParseException, IOException, RuntimeException {
		long startTime = 0;
		startTime -= System.currentTimeMillis();
		
		LoopCommand root = Parser.parse(new String(Files.readAllBytes(Paths.get("./in.bf"))));
		
		EOFMode eofMode;
		switch (args[0]) {
			case "":
			case "zero":
			case "ZERO":
				eofMode = EOFMode.ZERO;
				break;
			case "minus_one":
			case "MINUS_ONE":
			case "one":
			case "ONE":
				eofMode = EOFMode.MINUS_ONE;
				break;
			case "unchanged":
			case "UNCHANGED":
				eofMode = EOFMode.UNCHANGED;
				break;
			default:
				throw new ParseException("Unexpected argument: " + args[0]);
		}
		
		InterpreterOptions options = new InterpreterOptions(eofMode);
		
		RuntimeState state = new RuntimeState(args.length >= 2 ? args[1].toCharArray() : new char[0]);
		
		Interpreter.interpret(root.getChildren(), options, state);
		
		System.out.println("Interpreted in " + ((double) startTime + System.currentTimeMillis()) / 1000 + "s");
	}
	
	private static void interpret(Command[] commands, InterpreterOptions options, RuntimeState state) throws RuntimeException {
		for (Command c : commands) {
			Interpreter.interpret(c, options, state);
		}
	}
	
	private static void interpret(Command command, InterpreterOptions options, RuntimeState state) throws RuntimeException {
		if (command instanceof LoopCommand) {
			while (state.readTape() != 0) {
				Interpreter.interpret(((LoopCommand) command).getChildren(), options, state);
			}
		} else if (command instanceof MultipleCommand) {
			switch (((MultipleCommand) command).getValue()) {
				case INCREMENT:
					state.changeCell(((MultipleCommand) command).getAmount());
					break;
				case DECREMENT:
					state.changeCell(-((MultipleCommand) command).getAmount());
					break;
				case LEFT:
					state.movePointer(-((MultipleCommand) command).getAmount());
					break;
				case RIGHT:
					state.movePointer(((MultipleCommand) command).getAmount());
					break;
				default:
					throw new RuntimeException("uh oh stinky");
			}
		} else if (command instanceof NormalCommand) {
			switch (((NormalCommand) command).getValue()) {
				case READ:
					state.readAndStore();
					break;
				case WRITE:
					System.out.print((char) state.readTape());
					break;
				default:
					throw new RuntimeException("uh oh stinky");
			}
		} else {
			throw new RuntimeException("uh oh stinky");
		}
	}
}
