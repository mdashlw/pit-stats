package ru.mdashlw.hypixel.api.data;

import com.fasterxml.jackson.databind.JsonNode;
import ru.mdashlw.hypixel.api.util.JsonUtils;

public final class Guild {

  private final JsonNode data;

  public Guild(final JsonNode data) {
    this.data = data;
  }

  public String getName() {
    return JsonUtils.getOptionalText(this.data, "name");
  }
}
