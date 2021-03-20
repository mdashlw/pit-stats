package ru.mdashlw.hypixel.pit.stats;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import ru.mdashlw.hypixel.api.HypixelAPI;
import ru.mdashlw.hypixel.pit.stats.command.impl.PitCommand;
import ru.mdashlw.hypixel.pit.stats.config.Settings;
import ru.mdashlw.hypixel.pit.stats.listeners.ChatListener;

@Mod(modid = "pitstats", name = "Hypixel Pit Stats", version = "1.3.3", clientSideOnly = true)
public final class HypixelPitStats {

  @Mod.Instance
  private static HypixelPitStats INSTANCE;
  private static Logger LOGGER;

  private Settings settings;
  private HypixelAPI hypixelAPI;

  public static HypixelPitStats getInstance() {
    return INSTANCE;
  }

  public static Logger getLogger() {
    return LOGGER;
  }

  @EventHandler
  public void onPreInit(final FMLPreInitializationEvent event) {
    HypixelPitStats.LOGGER = event.getModLog();
    this.loadSettings(event.getSuggestedConfigurationFile());
    this.initHypixelAPI();
    this.registerListeners();
    this.registerCommands();
  }

  public void loadSettings(final File file) {
    this.settings = new Settings(new Configuration(file));
    this.settings.load();
  }

  public void initHypixelAPI() {
    this.hypixelAPI = new HypixelAPI(this.settings.getApiKey());
  }

  public void registerListeners() {
    new ChatListener().register();
  }

  public void registerCommands() {
    new PitCommand().register();
  }

  public Settings getSettings() {
    return this.settings;
  }

  public HypixelAPI getHypixelAPI() {
    return this.hypixelAPI;
  }
}
