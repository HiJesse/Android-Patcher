package cn.jesse.patcher.loader.util;

import java.util.ArrayList;

import cn.jesse.patcher.loader.Constants;

/**
 * Created by jesse on 06/12/2016.
 */

public class BsDiffPatchInfo {
    public String name;
    public String md5;
    public String rawCrc;
    public String patchMd5;

    public String path;

    public BsDiffPatchInfo(String name, String md5, String path, String raw, String patch) {
        // TODO Auto-generated constructor stub
        this.name = name;
        this.md5 = md5;
        this.rawCrc = raw;
        this.patchMd5 = patch;
        this.path = path;
    }

    public static void parseDiffPatchInfo(String meta, ArrayList<BsDiffPatchInfo> diffList) {
        if (meta == null || meta.length() == 0) {
            return;
        }
        String[] lines = meta.split("\n");
        for (final String line : lines) {
            if (line == null || line.length() <= 0) {
                continue;
            }
            final String[] kv = line.split(",", 5);
            if (kv == null || kv.length < 5) {
                continue;
            }
            // key
            final String name = kv[0].trim();
            final String path = kv[1].trim();
            final String md5 = kv[2].trim();
            final String rawCrc = kv[3].trim();
            final String patchMd5 = kv[4].trim();

            BsDiffPatchInfo dexInfo = new BsDiffPatchInfo(name, md5, path, rawCrc, patchMd5);
            diffList.add(dexInfo);
        }

    }

    public static boolean checkDiffPatchInfo(BsDiffPatchInfo info) {
        if (info == null) {
            return false;
        }
        String name = info.name;
        String md5 = info.md5;
        if (name == null || name.length() <= 0 || md5 == null || md5.length() != Constants.MD5_LENGTH) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append(",");
        sb.append(path);
        sb.append(",");
        sb.append(md5);
        sb.append(",");
        sb.append(rawCrc);
        sb.append(",");
        sb.append(patchMd5);
        return sb.toString();
    }
}
