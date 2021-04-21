package ru.mdashlw.hypixel.pit.stats.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class Settings {

  private final Configuration configuration;
  private String apiKey;
  private boolean displayRequiredPants;
  private boolean betterGlint;

  public Settings(final Configuration configuration) {
    this.configuration = configuration;
  }

  public void load() {
    this.configuration.load();
    this.update(true);
    this.configuration.save();
  }

  public void save() {
    this.update(false);
    this.configuration.save();
  }

  public void update(final boolean load) {
    Property property = this.configuration.get("general", "api_key", "");

    if (load) {
      this.apiKey = property.getString();
    } else {
      property.set(this.apiKey);
    }

    property = this.configuration.get("general", "display_required_pants", true);

    if (load) {
      this.displayRequiredPants = property.getBoolean();
    } else {
      property.set(this.displayRequiredPants);
    }

    property = this.configuration.get("general", "better_glint", true);

    if (load) {
      this.betterGlint = property.getBoolean();
    } else {
      property.set(this.betterGlint);
    }
  }

  public String getApiKey() {
    return this.apiKey;
  }

  public void setApiKey(final String apiKey) {
    this.apiKey = apiKey;
  }

  public boolean isDisplayRequiredPants() {
    return this.displayRequiredPants;
  }

  public void setDisplayRequiredPants(final boolean displayRequiredPants) {
    this.displayRequiredPants = displayRequiredPants;
  }

  public boolean isBetterGlint() {
    return this.betterGlint;
  }

  public void setBetterGlint(final boolean betterGlint) {
    this.betterGlint = betterGlint;
  }
}
