package com.cyq.awa.hfspro.tools;

import java.util.Locale;

/**
 * 分数/满分展示格式化。
 *
 * <p>满分、得分改为 double 后，整数会显示成 "750.0"。这里统一成：
 *
 * <ul>
 *   <li>整数 → "750"
 *   <li>小数 → 最多两位，去掉尾随 0（"749.96"、"149.9"）
 *   <li>NaN / Infinity / 负数兜底 → "0"
 * </ul>
 */
public final class ScoreFormat {

  private ScoreFormat() {}

  public static String format(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) return "0";
    if (value == Math.rint(value) && Math.abs(value) < 1e15) {
      return String.valueOf((long) value);
    }
    String text = String.format(Locale.US, "%.2f", value);
    while (text.endsWith("0")) {
      text = text.substring(0, text.length() - 1);
    }
    if (text.endsWith(".")) {
      text = text.substring(0, text.length() - 1);
    }
    return text;
  }
}
