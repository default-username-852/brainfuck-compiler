package common.tree;

import common.CommandType;

public class NormalCommand extends Command {
    private CommandType value;

    public NormalCommand(CommandType value) {
        this.value = value;
    }

    public CommandType getValue() {
        return value;
    }
}
