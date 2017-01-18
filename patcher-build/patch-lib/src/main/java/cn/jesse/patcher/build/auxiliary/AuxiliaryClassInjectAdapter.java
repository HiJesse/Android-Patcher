package cn.jesse.patcher.build.auxiliary;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Created by jesse on 09/01/2017.
 */

public class AuxiliaryClassInjectAdapter extends ClassVisitor{
    private final String auxiliaryClassDesc;
    private boolean isClInitExists;
    private boolean isInitExists;
    private boolean isTargetClass;
    private boolean isInjected;

    public AuxiliaryClassInjectAdapter(String auxiliaryClassName, ClassWriter cw) {
        super(Opcodes.ASM5, cw);
        this.auxiliaryClassDesc = fastClassNameToDesc(auxiliaryClassName);
    }

    private String fastClassNameToDesc(String className) {
        if (className.startsWith("L") && className.endsWith(";")) {
            return className;
        }
        if ("boolean".equals(className)) {
            return "Z";
        } else
        if ("byte".equals(className)) {
            return "B";
        } else
        if ("char".equals(className)) {
            return "C";
        } else
        if ("short".equals(className)) {
            return "S";
        } else
        if ("int".equals(className)) {
            return "I";
        } else
        if ("long".equals(className)) {
            return "J";
        } else
        if ("float".equals(className)) {
            return "F";
        } else
        if ("double".equals(className)) {
            return "D";
        } else {
            className = className.replace('.', '/');
            return "L" + className + ";";
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        // 在首个回调方法处初始化状态
        this.isClInitExists = false;
        this.isInitExists = false;
        this.isTargetClass = ((access & Opcodes.ACC_INTERFACE) == 0);
        this.isInjected = false;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        // 如何方法名是<init>或<clinit>则使用自定义的MethodVisitor插桩
        if (mv != null && this.isTargetClass && !this.isInjected) {
            if ("<clinit>".equals(name)) {
                this.isClInitExists = true;
                this.isInjected = true;
                mv = new InjectImplMethodVisitor(mv);
            } else
            if ("<init>".equals(name)) {
                this.isInitExists = true;
                this.isInjected = true;
                mv = new InjectImplMethodVisitor(mv);
            }
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        // 在ClassVisitor最后的回调方法处判断当前class是否有构造方法, 没有的话就创建出一个<clinit> 和classDesc的调用.
        if (!this.isClInitExists && !this.isInitExists) {
            MethodVisitor mv = super.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "lineSeparator", "()Ljava/lang/String;", false);
            Label lblSkipInvalidInsn = new Label();
            mv.visitJumpInsn(Opcodes.IFNONNULL, lblSkipInvalidInsn);
            mv.visitLdcInsn(Type.getType(this.auxiliaryClassDesc));
            mv.visitVarInsn(Opcodes.ASTORE, 0);
            mv.visitLabel(lblSkipInvalidInsn);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        super.visitEnd();
    }

    private class InjectImplMethodVisitor extends MethodVisitor {
        InjectImplMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            // 在方法末尾处插入代码
            if (opcode == Opcodes.RETURN) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "lineSeparator", "()Ljava/lang/String;", false);
                Label lblSkipInvalidInsn = new Label();
                super.visitJumpInsn(Opcodes.IFNONNULL, lblSkipInvalidInsn);
                super.visitLdcInsn(Type.getType(AuxiliaryClassInjectAdapter.this.auxiliaryClassDesc));
                super.visitVarInsn(Opcodes.ASTORE, 0);
                super.visitLabel(lblSkipInvalidInsn);
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            if (maxStack < 1) {
                maxStack = 1;
            }
            if (maxLocals < 1) {
                maxLocals = 1;
            }
            super.visitMaxs(maxStack, maxLocals);
        }
    }
}
