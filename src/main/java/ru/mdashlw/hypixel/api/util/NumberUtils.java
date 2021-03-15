package ru.mdashlw.hypixel.api.util;

import java.util.TreeMap;

public final class NumberUtils {

  private static final TreeMap<Integer, String> ROMAN_NUMERALS = new TreeMap<>();

  static {
    ROMAN_NUMERALS.put(1000, "M");
    ROMAN_NUMERALS.put(900, "CM");
    ROMAN_NUMERALS.put(500, "D");
    ROMAN_NUMERALS.put(400, "CD");
    ROMAN_NUMERALS.put(100, "C");
    ROMAN_NUMERALS.put(90, "XC");
    ROMAN_NUMERALS.put(50, "L");
    ROMAN_NUMERALS.put(40, "XL");
    ROMAN_NUMERALS.put(10, "X");
    ROMAN_NUMERALS.put(9, "IX");
    ROMAN_NUMERALS.put(5, "V");
    ROMAN_NUMERALS.put(4, "IV");
    ROMAN_NUMERALS.put(1, "I");
  }

  private NumberUtils() {
  }

  public static String toRomanNumeral(final int i) {
    final int l = ROMAN_NUMERALS.floorKey(i);

    if (i == l) {
      return ROMAN_NUMERALS.get(i);
    }

    return ROMAN_NUMERALS.get(l) + toRomanNumeral(i - l);
  }

  public static float ratio(final int a, final int b) {
    if (b == 0) {
      return a;
    }

    return (float) a / b;
  }

  public static String plural(final long i, final String s) {
    if (i == 1 || i == -1) {
      return i + " " + s;
    }

    return i + " " + s + 's';
  }
}
