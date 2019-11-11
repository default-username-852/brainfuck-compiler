package tree;

import org.objectweb.asm.Label;

public class LoopCommand extends Command {
    private Command[] children;
    private Label startLabel;
    private Label endLabel;

    public Command[] getChildren() {
        return children;
    }

    public Label getStartLabel() {
        return startLabel;
    }

    public Label getEndLabel() {
        return endLabel;
    }

    public LoopCommand(Command[] children) {
        this.children = children;
        this.startLabel = new Label();
        this.endLabel = new Label();
    }
}
