package ru.mdashlw.hypixel.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.MinecraftServer;
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

  public CompletableFuture<Player> getPlayerAsync(final String name) {
    return CompletableFuture.supplyAsync(() -> this.getPlayer(name), this.executor);
  }

  public Player getPlayer(final String name) {
    final String url;

    if (name.length() == 32 || name.length() == 36) {
      url = "https://api.hypixel.net/player?key=" + this.apiKey + "&uuid=" + name;
    } else {
      final String cachedUUID = this.cacheNameToUUID.getIfPresent(name.toLowerCase(Locale.ENGLISH));

      if (cachedUUID != null && cachedUUID.equals("null")) {
        return null;
      }

      if (cachedUUID != null) {
        url = "https://api.hypixel.net/player?key=" + this.apiKey + "&uuid=" + cachedUUID;
      } else {
        url = "https://api.hypixel.net/player?key=" + this.apiKey + "&name=" + name;
      }
    }

    final HttpGet request = new HttpGet(url);

    try (final CloseableHttpResponse response = (CloseableHttpResponse) this.httpClient.execute(request)) {
      final JsonNode data = this.objectMapper.readTree(response.getEntity().getContent());

      if (!data.get("success").asBoolean()) {
        final String cause = JsonUtils.getOptionalText(data, "cause", "no cause");

        if (cause.equals("You have already looked up this name recently")) {
          final GameProfile gameProfile = MinecraftServer.getServer().getPlayerProfileCache()
              .getGameProfileForUsername(name);

          if (gameProfile != null) {
            return this.getPlayer(gameProfile.getId().toString());
          }
        }

        throw new HypixelApiException(cause);
      }

      final JsonNode playerData = data.get("player");

      if (playerData == null || playerData.isNull()) {
        this.cacheNameToUUID.put(name.toLowerCase(Locale.ENGLISH), "null");
        return null;
      }

      final Player player = new Player(playerData);

      this.cacheNameToUUID.put(player.getName().toLowerCase(Locale.ENGLISH), player.getUUID());
      return player;
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
