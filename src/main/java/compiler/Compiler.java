package compiler;

import common.EOFMode;
import common.Parser;
import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;
import common.tree.Command;
import common.tree.LoopCommand;
import common.tree.MultipleCommand;
import common.tree.NormalCommand;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.objectweb.asm.Opcodes.*;

public class Compiler {
    
    public static void main(String[] args) throws IOException, CompilerException {
        PrintWriter printWriter = new PrintWriter(System.out);
        
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
                throw new CompilerException("Unexpected argument: " + args[0]);
        }
        CompilerOptions options = new CompilerOptions(eofMode);
        
        ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        
        TraceClassVisitor cw = new TraceClassVisitor(cv, printWriter);
        
        cw.visit(V11, ACC_PUBLIC + ACC_SUPER,
                "Kompilerad", null, "java/lang/Object", null);
        
        //Generate the main method
        //The magic happens here
        MethodVisitor mv1 = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
                "([Ljava/lang/String;)V", null, null);
        mv1.visitCode();
        
        //Set up memory state
        mv1.visitIntInsn(SIPUSH, 30000); //add 1000 to the stack
        mv1.visitIntInsn(NEWARRAY, T_INT); //Create a new int array of length 1000 and push a reference onto the stack?
        mv1.visitVarInsn(ASTORE, 1); //Store the array in local variable slot 1
        mv1.visitInsn(ICONST_0); //Push 0 onto the stack
        mv1.visitVarInsn(ISTORE, 2); //Store the pointer value in slot 2
        mv1.visitTypeInsn(NEW, "java/util/ArrayList");
        mv1.visitInsn(DUP);
        mv1.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
        mv1.visitVarInsn(ASTORE, 3);
        mv1.visitVarInsn(ALOAD, 0);
        mv1.visitInsn(ARRAYLENGTH);
        
        Label l0 = new Label();
        mv1.visitJumpInsn(IFLE, l0);
        
        mv1.visitVarInsn(ALOAD, 0);
        mv1.visitInsn(ICONST_0);
        mv1.visitInsn(AALOAD);
        mv1.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
        mv1.visitVarInsn(ASTORE, 4);
        mv1.visitVarInsn(ALOAD, 4);
        mv1.visitInsn(ARRAYLENGTH);
        mv1.visitVarInsn(ISTORE, 5);
        mv1.visitInsn(ICONST_0);
        mv1.visitVarInsn(ISTORE, 6);
        
        Label l8 = new Label();
        mv1.visitLabel(l8);
        
        mv1.visitVarInsn(ILOAD, 6);
        mv1.visitVarInsn(ILOAD, 5);
        mv1.visitJumpInsn(IF_ICMPGE, l0);
        mv1.visitVarInsn(ALOAD, 4);
        mv1.visitVarInsn(ILOAD, 6);
        mv1.visitInsn(CALOAD);
        mv1.visitVarInsn(ISTORE, 7);
        mv1.visitVarInsn(ALOAD, 3);
        mv1.visitVarInsn(ILOAD, 7);
        mv1.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf",
                "(C)Ljava/lang/Character;", false);
        mv1.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add",
                "(Ljava/lang/Object;)Z", false);
        mv1.visitInsn(POP);
        mv1.visitIincInsn(6, 1);
        mv1.visitJumpInsn(GOTO, l8);
        
        mv1.visitLabel(l0);
        
        Compiler.generateCode(root.getChildren(), mv1, options);
        
        //Finish method definition
        mv1.visitInsn(RETURN);
        mv1.visitMaxs(0, 0);
        mv1.visitEnd();
        
        
        //Generate a constructor for the "Kompilerad" class
        MethodVisitor mv2 = cw.visitMethod(ACC_PUBLIC, "<init>",
                "()V", null, null);
        mv2.visitCode();
        mv2.visitVarInsn(ALOAD, 0);
        mv2.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv2.visitInsn(RETURN);
        mv2.visitMaxs(0, 0);
        mv2.visitEnd();
        
        
        cw.visitEnd();
        byte[] b = cv.toByteArray();
        
        FileOutputStream fos = new FileOutputStream("Kompilerad.class");
        
        fos.write(b);
        
    }
    
    private static void generateCode(Command[] commands, MethodVisitor mv1, CompilerOptions options) throws CompilerException {
        for (Command c : commands) {
            Compiler.generateCode(c, mv1, options);
        }
    }
    
    private static void generateCode(Command command, MethodVisitor mv1, CompilerOptions options) throws CompilerException {
        if (command instanceof LoopCommand) {
            mv1.visitLabel(((LoopCommand) command).getStartLabel()); //Add the label
            mv1.visitVarInsn(ALOAD, 1); //Load a reference to the tape
            mv1.visitVarInsn(ILOAD, 2); //Load the value of the tape pointer
            mv1.visitInsn(IALOAD); //Load value at head
            mv1.visitJumpInsn(IFEQ, ((LoopCommand) command).getEndLabel()); //Jump if head points at 0
            
            for (Command c : ((LoopCommand) command).getChildren()) {
                Compiler.generateCode(c, mv1, options);
            }
            
            mv1.visitJumpInsn(GOTO, ((LoopCommand) command).getStartLabel());
            mv1.visitLabel(((LoopCommand) command).getEndLabel()); //Add the label
        } else if (command instanceof MultipleCommand) {
            switch (((MultipleCommand) command).getValue()) {
                case INCREMENT:
                    mv1.visitVarInsn(ALOAD, 1); //Load a reference to the tape
                    mv1.visitVarInsn(ILOAD, 2); //Load the value of the tape pointer
                    mv1.visitInsn(DUP2); //Duplicate both values on the stack
                    mv1.visitInsn(IALOAD); //Load the value that the head is pointing at
                    mv1.visitLdcInsn(((MultipleCommand) command).getAmount()); //Push the number to increment by to the stack
                    mv1.visitInsn(IADD); //Add the numbers
                    mv1.visitInsn(IASTORE); //Store the result in the memory tape
                    break;
                case DECREMENT:
                    mv1.visitVarInsn(ALOAD, 1); //Load a reference to the tape
                    mv1.visitVarInsn(ILOAD, 2); //Load the value of the tape pointer
                    mv1.visitInsn(DUP2); //Duplicate both values on the stack
                    mv1.visitInsn(IALOAD); //Load the value that the head is pointing at
                    mv1.visitLdcInsn(-((MultipleCommand) command).getAmount()); //Push the number to decrement by to the stack
                    mv1.visitInsn(IADD); //Add the numbers
                    mv1.visitInsn(IASTORE); //Store the result in the memory tape
                    break;
                case LEFT:
                    mv1.visitIincInsn(2, -((MultipleCommand) command).getAmount()); //Decrement the pointer value by
                    // the amount of times '<' appears
                    break;
                case RIGHT:
                    mv1.visitIincInsn(2, ((MultipleCommand) command).getAmount()); //Increment the pointer value by
                    // the amount of times '>' appears
                    break;
                default:
                    throw new CompilerException("uh oh stinky");
            }
        } else if (command instanceof NormalCommand) {
            switch (((NormalCommand) command).getValue()) {
                case INCREMENT:
                    mv1.visitVarInsn(ALOAD, 1); //Load a reference to the tape
                    mv1.visitVarInsn(ILOAD, 2); //Load the value of the tape pointer
                    mv1.visitInsn(DUP2); //Duplicate both values on the stack
                    mv1.visitInsn(IALOAD); //Load the value that the head is pointing at
                    mv1.visitLdcInsn(1); //Push the number to increment by to the stack
                    mv1.visitInsn(IADD); //Add the numbers
                    mv1.visitInsn(IASTORE); //Store the result in the memory tape
                    break;
                case DECREMENT:
                    mv1.visitVarInsn(ALOAD, 1); //Load a reference to the tape
                    mv1.visitVarInsn(ILOAD, 2); //Load the value of the tape pointer
                    mv1.visitInsn(DUP2); //Duplicate both values on the stack
                    mv1.visitInsn(IALOAD); //Load the value that the head is pointing at
                    mv1.visitLdcInsn(-1); //Push the number to decrement by to the stack
                    mv1.visitInsn(IADD); //Add the numbers
                    mv1.visitInsn(IASTORE); //Store the result in the memory tape
                    break;
                case LEFT:
                    mv1.visitIincInsn(2, -1); //Decrement the pointer value by the amount of times '<' appears
                    break;
                case RIGHT:
                    mv1.visitIincInsn(2, 1); //Increment the pointer value by the amount of times '>' appears
                    break;
                case READ:
                    Label t = new Label();
                    Label te = new Label();
                    Label c = new Label();
                    Label e = new Label();
                    mv1.visitTryCatchBlock(t, te, c, "java/lang/IndexOutOfBoundsException");
                    mv1.visitLabel(t);
                    mv1.visitVarInsn(ALOAD, 1);
                    mv1.visitVarInsn(ILOAD, 2);
                    mv1.visitVarInsn(ALOAD, 3);
                    mv1.visitInsn(ICONST_0);
                    mv1.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "remove",
                            "(I)Ljava/lang/Object;", false);
                    mv1.visitTypeInsn(CHECKCAST, "java/lang/Character"); //This apparently does something important
                    mv1.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                    mv1.visitInsn(IASTORE);
                    
                    mv1.visitLabel(te);
                    
                    mv1.visitJumpInsn(GOTO, e);
                    
                    mv1.visitLabel(c);
                    mv1.visitInsn(POP);
                    if (options.eofMode != EOFMode.UNCHANGED) {
                        mv1.visitVarInsn(ALOAD, 1);
                        mv1.visitVarInsn(ILOAD, 2);
                        if (options.eofMode == EOFMode.ZERO) {
                            mv1.visitInsn(ICONST_0);
                        } else if (options.eofMode == EOFMode.MINUS_ONE) {
                            mv1.visitInsn(ICONST_M1);
                        }
                        mv1.visitInsn(IASTORE);
                    }
                    
                    mv1.visitLabel(e);
                    break;
                case WRITE:
                    mv1.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                            "Ljava/io/PrintStream;"); //Load a reference to System.out to the stack
                    mv1.visitVarInsn(ALOAD, 1); //Load a reference to the tape
                    mv1.visitVarInsn(ILOAD, 2); //Load the value of the tape pointer
                    mv1.visitInsn(IALOAD); //Load value at head
                    //mv1.visitInsn(I2C); //Cast the value to a char. N.B. cast is not needed, the JVM does it anyway?
                    mv1.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print",
                            "(C)V", false); //Print it
                    break;
                case LOOPSTART:
                case LOOPEND:
                    throw new CompilerException("uh oh stinky");
            }
        }
    }
}
