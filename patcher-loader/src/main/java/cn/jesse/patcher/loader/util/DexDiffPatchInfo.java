package cn.jesse.patcher.loader.util;

import java.util.ArrayList;

import cn.jesse.patcher.loader.Constants;
import cn.jesse.patcher.loader.PatcherRuntimeException;

/**
 * Created by jesse on 18/11/2016.
 */
public class DexDiffPatchInfo {
    public final String rawName;
    public final String destMd5InDvm;
    public final String destMd5InArt;
    public final String oldDexCrC;
    public final String dexDiffMd5;

    public final String path;

    public final String dexMode;

    public final boolean isJarMode;

    /**
     * if it is jar mode, and the name is end of .dex, we should repackage it with zip, with renaming name.dex.jar
     */
    public final String realName;


    public DexDiffPatchInfo(String name, String path, String destMd5InDvm, String destMd5InArt, String dexDiffMd5, String oldDexCrc, String dexMode) {
        // TODO Auto-generated constructor stub
        this.rawName = name;
        this.path = path;
        this.destMd5InDvm = destMd5InDvm;
        this.destMd5InArt = destMd5InArt;
        this.dexDiffMd5 = dexDiffMd5;
        this.oldDexCrC = oldDexCrc;
        this.dexMode = dexMode;
        if (dexMode.equals(Constants.DEXMODE_JAR)) {
            this.isJarMode = true;
            if (PatchFileUtil.isRawDexFile(name)) {
                realName = name + Constants.JAR_SUFFIX;
            } else {
                realName = name;
            }
        } else if (dexMode.equals(Constants.DEXMODE_RAW)) {
            this.isJarMode = false;
            this.realName = name;
        } else {
            throw new PatcherRuntimeException("can't recognize dex mode:" + dexMode);
        }
    }

    public static void parseDexDiffPatchInfo(String meta, ArrayList<DexDiffPatchInfo> dexList) {
        if (meta == null || meta.length() == 0) {
            return;
        }
        String[] lines = meta.split("\n");
        for (final String line : lines) {
            if (line == null || line.length() <= 0) {
                continue;
            }
            final String[] kv = line.split(",", 7);
            if (kv == null || kv.length < 7) {
                continue;
            }

            // key
            final String name = kv[0].trim();
            final String path = kv[1].trim();
            final String destMd5InDvm = kv[2].trim();
            final String destMd5InArt = kv[3].trim();
            final String dexDiffMd5 = kv[4].trim();
            final String oldDexCrc = kv[5].trim();
            final String dexMode = kv[6].trim();

            DexDiffPatchInfo dexInfo = new DexDiffPatchInfo(name, path, destMd5InDvm, destMd5InArt, dexDiffMd5, oldDexCrc, dexMode);
            dexList.add(dexInfo);
        }

    }

    public static boolean checkDexDiffPatchInfo(DexDiffPatchInfo info) {
        if (info == null) {
            return false;
        }
        String name = info.rawName;
        String md5 = (PatcherInternals.isVmArt() ? info.destMd5InArt : info.destMd5InDvm);
        if (name == null || name.length() <= 0 || md5 == null || md5.length() != Constants.MD5_LENGTH) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(rawName);
        sb.append(",");
        sb.append(path);
        sb.append(",");
        sb.append(destMd5InDvm);
        sb.append(",");
        sb.append(destMd5InArt);
        sb.append(",");
        sb.append(oldDexCrC);
        sb.append(",");
        sb.append(dexDiffMd5);
        sb.append(",");
        sb.append(dexMode);
        return sb.toString();
    }
}
