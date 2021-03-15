package ru.mdashlw.hypixel.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public final class JsonUtils {

  private JsonUtils() {
  }

  public static JsonNode getOptionalObject(final JsonNode data, final String field) {
    final JsonNode node = data.get(field);

    if (node == null) {
      return JsonNodeFactory.instance.objectNode();
    }

    return node;
  }

  public static String getOptionalText(final JsonNode data, final String field) {
    return getOptionalText(data, field, null);
  }

  public static String getOptionalText(final JsonNode data, final String field, final String fallback) {
    final JsonNode node = data.get(field);

    if (node == null) {
      return fallback;
    }

    return node.asText();
  }

  public static int getOptionalInt(final JsonNode data, final String field) {
    final JsonNode node = data.get(field);

    if (node == null) {
      return 0;
    }

    return node.asInt();
  }

  public static long getOptionalLong(final JsonNode data, final String field) {
    final JsonNode node = data.get(field);

    if (node == null) {
      return 0;
    }

    return node.asLong();
  }

  public static double getOptionalDouble(final JsonNode data, final String field) {
    final JsonNode node = data.get(field);

    if (node == null) {
      return 0;
    }

    return node.asDouble();
  }

  public static byte[] getByteArray(final JsonNode data, final String field) {
    final JsonNode node = data.get(field);

    if (node == null || !node.isArray()) {
      return null;
    }

    final byte[] bytes = new byte[node.size()];

    for (int i = 0; i < node.size(); i++) {
      bytes[i] = (byte) node.get(i).asInt();
    }

    return bytes;
  }
}
