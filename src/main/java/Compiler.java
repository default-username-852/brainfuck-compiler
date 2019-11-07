import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;

import static org.objectweb.asm.Opcodes.*;

public class Compiler {
	public static void main(String[] args) throws IOException {
		/*ClassPrinter cp = new ClassPrinter();
		ClassReader cr = new ClassReader("java.lang.String");
		cr.accept(cp, 0);*/
		
		PrintWriter printWriter = new PrintWriter(System.out);
		
		ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		
		TraceClassVisitor cw = new TraceClassVisitor(cv, printWriter);
		
		cw.visit(V11, ACC_PUBLIC,
				"Ut", null, "java/lang/Object", null);
		/*cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "LESS", "I",
				null, -1).visitEnd();
		cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "EQUAL", "I",
				null, 0).visitEnd();
		cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "GREATER", "I",
				null, 1).visitEnd();*/
		MethodVisitor mv1 = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		
		mv1.visitEnd();
		MethodVisitor mv2 = cw.visitMethod(ACC_PUBLIC, "<init>",
				"()V", null, null);
		
		mv2.visitCode();
		mv2.visitVarInsn(ALOAD, 0);
		mv2.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv2.visitInsn(RETURN);
		mv2.visitMaxs(1, 1);
		mv2.visitEnd();
		
		cw.visitEnd();
		byte[] b = cv.toByteArray();
		
		FileOutputStream fos = new FileOutputStream("Ut.class");
		
		fos.write(b);
		
		InputStream is = new FileInputStream("./Main.class");
		
		TraceClassVisitor tcv = new TraceClassVisitor(printWriter);
		
		ClassReader cr = new ClassReader(is);
		cr.accept(tcv, 0);
	}
}
