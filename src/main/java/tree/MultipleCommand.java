package tree;

import compiler.CommandType;

public class MultipleCommand extends NormalCommand {
    private int amount;

    public int getAmount() {
        return amount;
    }

    public MultipleCommand(CommandType value, int amount) {
        super(value);
        this.amount = amount;
    }

    public void incrementCount() {
        this.amount += 1;
    }
}
