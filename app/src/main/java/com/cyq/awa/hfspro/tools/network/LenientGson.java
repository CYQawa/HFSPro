package com.cyq.awa.hfspro.tools.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.Locale;

/**
 * 宽容的数字/布尔解析器。
 *
 * <p>服务端同一个字段可能返回 749.96（小数）、"179"（字符串）、"**"（占位符）或 null，
 * 而模型里声明的是 int/boolean 时，Gson 会抛异常并让整个响应解析失败（表现为"网络请求失败！"）。
 *
 * <p>这里统一接管基本类型与包装类型的反序列化：
 *
 * <ul>
 *   <li>数字：原样取值，int/long 四舍五入
 *   <li>数字字符串：去掉空白、千分位逗号、百分号后解析
 *   <li>null / 空串 / "**" / "-" 等占位符：基本类型降级为 0，包装类型保持 null
 *   <li>无法解析的字符串：降级为 0 并打印警告日志，不再中断整个解析
 *   <li>boolean：兼容 true/false、0/1、"true"/"false"、"yes"/"no"
 * </ul>
 */
public final class LenientGson {

  private static final String TAG = "LenientGson";

  private static Gson instance;

  private LenientGson() {}

  public static synchronized Gson get() {
    if (instance == null) {
      instance =
          new GsonBuilder()
              .registerTypeAdapter(int.class, new NumberAdapter(Kind.INT, true))
              .registerTypeAdapter(Integer.class, new NumberAdapter(Kind.INT, false))
              .registerTypeAdapter(long.class, new NumberAdapter(Kind.LONG, true))
              .registerTypeAdapter(Long.class, new NumberAdapter(Kind.LONG, false))
              .registerTypeAdapter(double.class, new NumberAdapter(Kind.DOUBLE, true))
              .registerTypeAdapter(Double.class, new NumberAdapter(Kind.DOUBLE, false))
              .registerTypeAdapter(float.class, new NumberAdapter(Kind.FLOAT, true))
              .registerTypeAdapter(Float.class, new NumberAdapter(Kind.FLOAT, false))
              .registerTypeAdapter(boolean.class, new BooleanAdapter(true))
              .registerTypeAdapter(Boolean.class, new BooleanAdapter(false))
              .create();
    }
    return instance;
  }

  private enum Kind {
    INT,
    LONG,
    DOUBLE,
    FLOAT
  }

  /** 把任意 JSON 值尽量转成 double；无法识别时返回 0。 */
  private static double asDouble(JsonElement json) {
    if (json == null || json.isJsonNull()) return 0d;
    if (!json.isJsonPrimitive()) return 0d;

    JsonPrimitive primitive = json.getAsJsonPrimitive();
    if (primitive.isNumber()) {
      try {
        return primitive.getAsDouble();
      } catch (NumberFormatException e) {
        Log.w(TAG, "无法解析数字: " + primitive, e);
        return 0d;
      }
    }
    if (primitive.isBoolean()) {
      return primitive.getAsBoolean() ? 1d : 0d;
    }

    String text = primitive.getAsString().trim();
    if (text.isEmpty() || isPlaceholder(text)) return 0d;

    // 去掉千分位、百分号、常见的分数分隔符
    text = text.replace(",", "").replace("%", "").trim();
    if (text.endsWith("分")) {
      text = text.substring(0, text.length() - 1).trim();
    }
    try {
      return Double.parseDouble(text);
    } catch (NumberFormatException e) {
      Log.w(TAG, "字段值无法转为数字，已按 0 处理: \"" + text + "\"");
      return 0d;
    }
  }

  private static boolean isPlaceholder(String text) {
    return "**".equals(text)
        || "*".equals(text)
        || "-".equals(text)
        || "--".equals(text)
        || "null".equalsIgnoreCase(text)
        || "N/A".equalsIgnoreCase(text);
  }

  private static final class NumberAdapter implements JsonDeserializer<Number> {
    private final Kind kind;
    private final boolean primitive;

    NumberAdapter(Kind kind, boolean primitive) {
      this.kind = kind;
      this.primitive = primitive;
    }

    @Override
    public Number deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
      if (json == null || json.isJsonNull()) {
        return primitive ? zero() : null;
      }
      double value = asDouble(json);
      switch (kind) {
        case INT:
          return (int) Math.round(value);
        case LONG:
          return Math.round(value);
        case FLOAT:
          return (float) value;
        case DOUBLE:
        default:
          return value;
      }
    }

    private Number zero() {
      switch (kind) {
        case INT:
          return 0;
        case LONG:
          return 0L;
        case FLOAT:
          return 0f;
        case DOUBLE:
        default:
          return 0d;
      }
    }
  }

  private static final class BooleanAdapter implements JsonDeserializer<Boolean> {
    private final boolean primitive;

    BooleanAdapter(boolean primitive) {
      this.primitive = primitive;
    }

    @Override
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
      if (json == null || json.isJsonNull()) {
        return primitive ? Boolean.FALSE : null;
      }
      if (!json.isJsonPrimitive()) {
        return primitive ? Boolean.FALSE : null;
      }

      JsonPrimitive primitiveValue = json.getAsJsonPrimitive();
      if (primitiveValue.isBoolean()) return primitiveValue.getAsBoolean();
      if (primitiveValue.isNumber()) return primitiveValue.getAsDouble() != 0d;

      String text = primitiveValue.getAsString().trim().toLowerCase(Locale.US);
      if (text.isEmpty()) return primitive ? Boolean.FALSE : null;
      return "1".equals(text)
          || "true".equals(text)
          || "yes".equals(text)
          || "y".equals(text)
          || "t".equals(text);
    }
  }
}
