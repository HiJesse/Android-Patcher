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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import cn.jesse.patcher.commons.ziputils.Streams;

/**
 * Created by jesse on 22/12/2016.
 */

public class AuxiliaryClassInjector {
    public static final String NOT_EXISTS_CLASSNAME
            = "pAtChEr.pReVEnT.PrEVErIfIEd.STuBCLaSS";

    public interface ProcessJarCallback {
        boolean onProcessClassEntry(String entryName);
    }

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

    public static void processJar(File jarIn, File jarOut, ProcessJarCallback cb) throws IOException {
        try {
            processJarHelper(jarIn, jarOut, cb, Charset.forName("UTF-8"), Charset.forName("UTF-8"));
        } catch (IllegalArgumentException e) {
            if ("MALFORMED".equals(e.getMessage())) {
                processJarHelper(jarIn, jarOut, cb, Charset.forName("GBK"), Charset.forName("UTF-8"));
            } else {
                throw e;
            }
        }
    }

    private static void processJarHelper(File jarIn, File jarOut, ProcessJarCallback cb, Charset charsetIn, Charset charsetOut) throws IOException {
        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(jarIn)), charsetIn);
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(jarOut)), charsetOut);
            ZipEntry entryIn = null;
            Map<String, Integer> processedEntryNamesMap = new HashMap<>();
            while ((entryIn = zis.getNextEntry()) != null) {
                final String entryName = entryIn.getName();
                if (!processedEntryNamesMap.containsKey(entryName)) {
                    ZipEntry entryOut = new ZipEntry(entryIn);
                    entryOut.setCompressedSize(-1);
                    zos.putNextEntry(entryOut);
                    if (!entryIn.isDirectory()) {
                        if (entryName.endsWith(".class")) {
                            if (cb == null || cb.onProcessClassEntry(entryName)) {
                                processClass(zis, zos);
                            } else {
                                Streams.copy(zis, zos);
                            }
                        } else {
                            Streams.copy(zis, zos);
                        }
                    }
                    zos.closeEntry();
                    processedEntryNamesMap.put(entryName, 1);
                }
            }
        } finally {
            closeQuietly(zos);
            closeQuietly(zis);
        }
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
