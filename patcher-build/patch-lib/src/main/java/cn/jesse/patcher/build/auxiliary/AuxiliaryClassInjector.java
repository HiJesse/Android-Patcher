package cn.jesse.patcher.build.auxiliary;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jesse on 22/12/2016.
 */

public class AuxiliaryClassInjector {
    public static final String NOT_EXISTS_CLASSNAME
            = "pAtChEr.pReVEnT.PrEVErIfIEd.STuBCLaSS";

    public static void processClass(File classIn, File classOut) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(new FileInputStream(classIn));
            os = new BufferedOutputStream(new FileOutputStream(classOut));
            processClass(is, os);
        } finally {
            closeQuietly(os);
            closeQuietly(is);
        }
    }

    private static void processClass(InputStream classIn, OutputStream classOut) throws IOException {
        ClassReader cr = new ClassReader(classIn);
        ClassWriter cw = new ClassWriter(0);
        AuxiliaryClassInjectAdapter aia = new AuxiliaryClassInjectAdapter(NOT_EXISTS_CLASSNAME, cw);
        cr.accept(aia, 0);
        classOut.write(cw.toByteArray());
        classOut.flush();
    }

    private static void closeQuietly(Closeable target) {
        if (target != null) {
            try {
                target.close();
            } catch (Exception e) {
                // Ignored.
            }
        }
    }
}
