package cn.jesse.patcher.build.info;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import cn.jesse.patcher.build.patch.Configuration;

/**
 * Created by jesse on 19/12/2016.
 */

public class InfoWriter {
    protected final Configuration config;
    /**
     * infoFile, output info
     */
    protected final String infoPath;
    protected final File infoFile;

    /**
     * 首次使用时初始化
     */
    protected Writer infoWrite;

    public InfoWriter(Configuration config, String infoPath) throws IOException {
        this.config = config;
        this.infoPath = infoPath;

        if (infoPath != null) {
            this.infoFile = new File(infoPath);
            if (!infoFile.getParentFile().exists()) {
                infoFile.getParentFile().mkdirs();
            }
        } else {
            this.infoFile = null;
        }

    }

    public Configuration getConfig() {
        return config;
    }

    public void writeLinesToInfoFile(List<String> lines) throws IOException {
        for (String line : lines) {
            writeLineToInfoFile(line);
        }
    }

    public void writeLineToInfoFile(String line) {
        if (infoPath == null || line == null || line.length() == 0) {
            return;
        }
        try {
            checkWriter();
            infoWrite.write(line);
            infoWrite.write("\n");
            infoWrite.flush();
        } catch (Exception e) {
            throw new RuntimeException("write info file error, infoPath:" + infoPath + " content:" + line, e);
        }
    }

    private void checkWriter() throws IOException {
        if (infoWrite == null) {
            this.infoWrite = new BufferedWriter(new FileWriter(infoFile, false));
        }

    }

    public void close() {
        try {
            if (infoWrite != null) infoWrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
