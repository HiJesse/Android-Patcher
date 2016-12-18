package cn.jesse.patcher.build.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Created by jesse on 18/12/2016.
 */

public class Utils {
    public static boolean isPresent(String str) {
        return str != null && str.length() > 0;
    }

    public static boolean isBlank(String str) {
        return !isPresent(str);
    }

    public static boolean isPresent(Iterator iterator) {
        return iterator != null && iterator.hasNext();
    }

    public static boolean isBlank(Iterator iterator) {
        return !isPresent(iterator);
    }

    public static String convertToPatternString(String input) {
        //convert \\.
        if (input.contains(".")) {
            input = input.replaceAll("\\.", "\\\\.");
        }
        //convert ？to .
        if (input.contains("?")) {
            input = input.replaceAll("\\?", "\\.");
        }
        //convert * to.*
        if (input.contains("*")) {
            input = input.replace("*", ".*");
        }
        return input;
    }

    public static boolean isStringMatchesPatterns(String str, Collection<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(str).matches()) {
                return true;
            }
        }
        return false;
    }

    public static <T> String collectionToString(Collection<T> collection) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean isFirstElement = true;
        for (T element : collection) {
            if (isFirstElement) {
                isFirstElement = false;
            } else {
                sb.append(',');
            }
            sb.append(element);
        }
        sb.append('}');
        return sb.toString();
    }

    public static boolean checkFileInPattern(HashSet<Pattern> patterns, String key) {
        if (!patterns.isEmpty()) {
            for (Iterator<Pattern> it = patterns.iterator(); it.hasNext();) {
                Pattern p = it.next();
                if (p.matcher(key).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
}
