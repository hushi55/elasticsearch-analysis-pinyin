package org.elasticsearch.index.analysis;

import java.util.*;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Spellings {

    private static int MAX_SPELLING_SIZE = 1000;

    public static String[] toMobileKeyboardNumberSpellings(String chinese) {
        // Normalize parameters.

        chinese = (chinese == null) ? "" : chinese.trim();
        if (chinese.length() == 0) {
            return new String[0];
        }

        // Generate spellings.

        String[] abbreviatedSpellings = toAbbreviatedSpellings(chinese);
        if (abbreviatedSpellings == null || abbreviatedSpellings.length == 0) {
            return new String[0];
        }

        Set<String> spellings = new HashSet<String>();
        for (String abbreviatedSpelling : abbreviatedSpellings) {
            String spelling = toMobileKeyboardNumberSpelling(abbreviatedSpelling);
            if (spelling == null || spelling.trim().length() == 0) {
                continue;
            }

            spellings.add(spelling);
        }

        String[] result = new String[0];
        return spellings.toArray(result);
    }

    public static String toMobileKeyboardNumberSpelling(String input) {
        Map<String, String> letterToNumberMap = new HashMap<String, String>();
        letterToNumberMap.put("a", "2");
        letterToNumberMap.put("b", "2");
        letterToNumberMap.put("c", "2");
        letterToNumberMap.put("d", "3");
        letterToNumberMap.put("e", "3");
        letterToNumberMap.put("f", "3");
        letterToNumberMap.put("g", "4");
        letterToNumberMap.put("h", "4");
        letterToNumberMap.put("i", "4");
        letterToNumberMap.put("j", "5");
        letterToNumberMap.put("k", "5");
        letterToNumberMap.put("l", "5");
        letterToNumberMap.put("m", "6");
        letterToNumberMap.put("n", "6");
        letterToNumberMap.put("o", "6");
        letterToNumberMap.put("p", "7");
        letterToNumberMap.put("q", "7");
        letterToNumberMap.put("r", "7");
        letterToNumberMap.put("s", "7");
        letterToNumberMap.put("t", "8");
        letterToNumberMap.put("u", "8");
        letterToNumberMap.put("v", "8");
        letterToNumberMap.put("w", "9");
        letterToNumberMap.put("x", "9");
        letterToNumberMap.put("y", "9");
        letterToNumberMap.put("z", "9");

        // Normalize parameters.

        input = (input == null) ? "" : input.trim();
        if (input.length() == 0) {
            return "";
        }

        // Generate spellings.

        String spelling = "";
        for (int i = 0; i < input.length(); i++) {
            String key = input.substring(i, i + 1);
            String value = letterToNumberMap.get(key);
            if (value == null) {
                continue;
            }

            spelling += value;
        }

        return spelling;
    }

    public static String[] toMultipleSpellings(String chinese) {
        // Normalize parameters.

        chinese = (chinese == null) ? "" : chinese.trim();
        if (chinese.length() == 0) {
            return new String[0];
        }

        // Generate spellings.

        String[] fullSpellings = toFullSpellings(chinese);
        String[] abbreviatedSpellings = toAbbreviatedSpellings(chinese);
        String[] mixedSpellings = toMixedSpellings(chinese);

        Set<String> multipleSpellings = new HashSet<String>();
        multipleSpellings.addAll(Arrays.asList(fullSpellings));
        multipleSpellings.addAll(Arrays.asList(abbreviatedSpellings));
        multipleSpellings.addAll(Arrays.asList(mixedSpellings));

        String[] result = new String[0];
        return multipleSpellings.toArray(result);
    }

    public static String[] toAbbreviatedSpellings(String chinese) {
        Map<String, Set<String>> doubleInitialMap = new HashMap<String, Set<String>>();
        {
            Set<String> value = new HashSet<String>();
            value.add("zh");
            value.add("z");
            doubleInitialMap.put("zh", value);
        }
        {
            Set<String> value = new HashSet<String>();
            value.add("ch");
            value.add("c");
            doubleInitialMap.put("ch", value);
        }
        {
            Set<String> value = new HashSet<String>();
            value.add("sh");
            value.add("s");
            doubleInitialMap.put("sh", value);
        }

        // Normalize parameters.

        chinese = (chinese == null) ? "" : chinese.trim();
        if (chinese.length() == 0) {
            return new String[0];
        }

        // Generate spellings.

        long fullSpellingSize = 0;
        List<Set<String>> pinyinArrayList = new ArrayList<Set<String>>();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        for (int i = 0; i < chinese.length(); i++) {
            String[] pinyinArray = null;
            try {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(chinese.charAt(i), format);
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                continue;
            }

            if (pinyinArray == null || pinyinArray.length == 0) {
                continue;
            }

            Set<String> pinyinSet = new LinkedHashSet<String>();
            for (String pinyin : pinyinArray) {
                if (pinyin.length() > 2) {
                    String key = pinyin.substring(0, 2);
                    if (doubleInitialMap.containsKey(key)) {
                        pinyinSet.addAll(doubleInitialMap.get(key));
                        continue;
                    }
                }

                pinyinSet.add(String.valueOf(pinyin.charAt(0)));
            }

            pinyinArrayList.add(pinyinSet);
            fullSpellingSize = (fullSpellingSize == 0) ? pinyinSet.size() : fullSpellingSize * pinyinSet.size();
        }

        return generate(fullSpellingSize, pinyinArrayList);
    }

    public static String[] toFullSpellings(String chinese) {
        // Normalize parameters.

        chinese = (chinese == null) ? "" : chinese.trim();
        if (chinese.length() == 0) {
            return new String[0];
        }

        // Generate spellings.

        long fullSpellingSize = 0;
        List<Set<String>> pinyinArrayList = new ArrayList<Set<String>>();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        for (int i = 0; i < chinese.length(); i++) {
            String[] pinyinArray = null;
            try {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(chinese.charAt(i), format);
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                continue;
            }

            if (pinyinArray == null || pinyinArray.length == 0) {
                continue;
            }

            Set<String> pinyinSet = new LinkedHashSet<String>();
            for (String pinyin : pinyinArray) {
                pinyinSet.add(pinyin);
            }

            pinyinArrayList.add(pinyinSet);
            fullSpellingSize = (fullSpellingSize == 0) ? pinyinSet.size() : fullSpellingSize * pinyinSet.size();
        }

        return generate(fullSpellingSize, pinyinArrayList);
    }

    public static String[] toMixedSpellings(String chinese) {
        Map<String, Set<String>> doubleInitialMap = new HashMap<String, Set<String>>();
        {
            Set<String> value = new HashSet<String>();
            value.add("zh");
            value.add("z");
            doubleInitialMap.put("zh", value);
        }
        {
            Set<String> value = new HashSet<String>();
            value.add("ch");
            value.add("c");
            doubleInitialMap.put("ch", value);
        }
        {
            Set<String> value = new HashSet<String>();
            value.add("sh");
            value.add("s");
            doubleInitialMap.put("sh", value);
        }

        // Normalize parameters.

        chinese = (chinese == null) ? "" : chinese.trim();
        if (chinese.length() == 0) {
            return new String[0];
        }

        // Generate spellings.

        long fullSpellingSize = 0;
        List<Set<String>> pinyinArrayList = new ArrayList<Set<String>>();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        for (int i = 0; i < chinese.length(); i++) {
            String[] pinyinArray = null;
            try {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(chinese.charAt(i), format);
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                continue;
            }

            if (pinyinArray == null || pinyinArray.length == 0) {
                continue;
            }

            Set<String> pinyinSet = new LinkedHashSet<String>();
            for (String pinyin : pinyinArray) {
                if (i == 0) {
                    pinyinSet.add(pinyin);
                } else {
                    if (pinyin.length() > 2) {
                        String key = pinyin.substring(0, 2);
                        if (doubleInitialMap.containsKey(key)) {
                            pinyinSet.addAll(doubleInitialMap.get(key));
                            continue;
                        }
                    }

                    pinyinSet.add(String.valueOf(pinyin.charAt(0)));
                }
            }

            pinyinArrayList.add(pinyinSet);
            fullSpellingSize = (fullSpellingSize == 0) ? pinyinSet.size() : fullSpellingSize * pinyinSet.size();
        }

        return generate(fullSpellingSize, pinyinArrayList);
    }

    private static String[] generate(long fullSpellingSize, List<Set<String>> pinyinArrayList) {
        int spellingSize;
        if (fullSpellingSize > MAX_SPELLING_SIZE) spellingSize = MAX_SPELLING_SIZE;
        else spellingSize = (int)fullSpellingSize;

        String[] spellings = new String[spellingSize];
        long fillNumber = fullSpellingSize;
        for (Set<String> pinyinSet : pinyinArrayList) {
            fillNumber = fillNumber / pinyinSet.size();

            int i = 0;
            while (i < spellingSize) {
                for (String pinyin : pinyinSet) {
                    for (long j = 0; j < fillNumber; j++) {
                        if (i >= spellingSize) break;
                        if (spellings[i] == null) {
                            spellings[i] = pinyin;
                        } else {
                            spellings[i] = spellings[i] + pinyin;
                        }
                        i++;
                    }
                    if (i >= spellingSize) break;
                }
            }
        }

        return spellings;
    }
    
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }
}


