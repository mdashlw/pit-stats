package ru.mdashlw.hypixel.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import ru.mdashlw.hypixel.api.data.Guild;
import ru.mdashlw.hypixel.api.data.Player;
import ru.mdashlw.hypixel.api.exception.HypixelApiException;
import ru.mdashlw.hypixel.api.util.JsonUtils;

public final class HypixelAPI {

  private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private final HttpClient httpClient = HttpClients.custom()
      .setMaxConnTotal(24)
      .setMaxConnPerRoute(24)
      .build();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Cache<String, String> cacheNameToUUID = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(30, TimeUnit.MINUTES)
      .build();
  private String apiKey;

  public HypixelAPI(final String apiKey) {
    this.apiKey = apiKey;
  }

  public CompletableFuture<Player> getPlayerByNameAsync(final String name) {
    return CompletableFuture.supplyAsync(() -> this.getPlayerByName(name), this.executor);
  }

  public Player getPlayerByName(final String name) {
    final String lowerName = name.toLowerCase(Locale.ENGLISH);
    final String cachedUUID = this.cacheNameToUUID.getIfPresent(lowerName);

    if (cachedUUID != null && cachedUUID.equals("null")) {
      return null;
    }

    final String url;

    if (cachedUUID != null) {
      url = "https://api.hypixel.net/player?key=" + this.apiKey + "&uuid=" + cachedUUID;
    } else {
      url = "https://api.hypixel.net/player?key=" + this.apiKey + "&name=" + name;
    }

    final HttpGet request = new HttpGet(url);

    try (final CloseableHttpResponse response = (CloseableHttpResponse) this.httpClient.execute(request)) {
      final JsonNode data = this.objectMapper.readTree(response.getEntity().getContent());

      if (!data.get("success").asBoolean()) {
        throw new HypixelApiException(JsonUtils.getOptionalText(data, "cause", "no cause"));
      }

      final JsonNode playerData = data.get("player");

      if (playerData == null || playerData.isNull()) {
        this.cacheNameToUUID.put(lowerName, "null");
        return null;
      }

      this.cacheNameToUUID.put(lowerName, playerData.get("uuid").asText());
      return new Player(playerData);
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  public CompletableFuture<Guild> getGuildByPlayerAsync(final String player) {
    return CompletableFuture.supplyAsync(() -> this.getGuildByPlayer(player), this.executor);
  }

  public Guild getGuildByPlayer(final String player) {
    final HttpGet request = new HttpGet("https://api.hypixel.net/guild?key=" + this.apiKey + "&player=" + player);

    try (final CloseableHttpResponse response = (CloseableHttpResponse) this.httpClient.execute(request)) {
      final JsonNode data = this.objectMapper.readTree(response.getEntity().getContent());

      if (!data.get("success").asBoolean()) {
        throw new HypixelApiException(JsonUtils.getOptionalText(data, "cause", "no cause"));
      }

      final JsonNode guildData = data.get("guild");

      if (guildData == null || guildData.isNull()) {
        return null;
      }

      return new Guild(guildData);
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  public String getApiKey() {
    return this.apiKey;
  }

  public void setApiKey(final String apiKey) {
    this.apiKey = apiKey;
  }
}
