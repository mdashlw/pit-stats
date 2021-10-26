package ru.mdashlw.hypixel.pit.stats.command.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import ru.mdashlw.hypixel.api.exception.HypixelApiException;
import ru.mdashlw.hypixel.pit.stats.HypixelPitStats;
import ru.mdashlw.hypixel.pit.stats.command.Command;
import ru.mdashlw.hypixel.pit.stats.config.Settings;
import ru.mdashlw.hypixel.pit.stats.gui.GuiStats;
import ru.mdashlw.hypixel.pit.stats.util.GuiScreenDisplayer;

public final class PitCommand extends Command {

  @Override
  public String getCommandName() {
    return "pit";
  }

  @Override
  public List<String> getCommandAliases() {
    return Arrays.asList("pitstats", "pitinfo");
  }

  @Override
  public void processCommand(final ICommandSender sender, final String[] args) {
    if (args.length == 0) {
      sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7Usage: ")
          .appendSibling(new ChatComponentText("§b/pit <player name>")
              .setChatStyle(new ChatStyle()
                  .setChatClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/pit ")))));
      return;
    }

    final HypixelPitStats mod = HypixelPitStats.getInstance();
    final String playerName = args[0];

    if (playerName.length() == 36) {
      final Settings settings = mod.getSettings();

      settings.setApiKey(playerName);
      settings.save();
      mod.getHypixelAPI().setApiKey(playerName);
      sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7Set API key to §9" + playerName));
      return;
    }

    if (playerName.equalsIgnoreCase("config") ||
        playerName.equalsIgnoreCase("cfg") ||
        playerName.equalsIgnoreCase("settings")) {
      final Settings settings = mod.getSettings();

      if (args.length == 1) {
        sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7Mod Configuration (click to toggle on/off)\n")
            .appendSibling(new ChatComponentText(" §8- §fDisplay required pants for mystic items: " +
                (settings.isDisplayRequiredPants() ? "§aON" : "§cOFF") + "\n")
                .setChatStyle(new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText("§7Click to toggle this setting on/off")))
                    .setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/pit config pants"))))
            .appendSibling(new ChatComponentText(" §8- §fAdd enchant glint for pit items: " +
                (settings.isBetterGlint() ? "§aON" : "§cOFF"))
                .setChatStyle(new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText("§7Click to toggle this setting on/off")))
                    .setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/pit config glint")))));
        return;
      }

      final String option = args[1];

      if (option.equalsIgnoreCase("pants") || option.equalsIgnoreCase("pant")) {
        settings.setDisplayRequiredPants(!settings.isDisplayRequiredPants());
        settings.save();
        sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7Display required pants for mystic items is now " +
            (settings.isDisplayRequiredPants() ? "§aenabled" : "§cdisabled") + "§7."));
      } else if (option.equalsIgnoreCase("glint")) {
        settings.setBetterGlint(!settings.isBetterGlint());
        sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7Add enchant glint for pit items now " +
            (settings.isBetterGlint() ? "§aenabled" : "§cdisabled") + "§7."));
        settings.save();
      } else {
        sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7Invalid config option."));
      }

      return;
    }

    if (mod.getSettings().getApiKey().isEmpty()) {
      sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7You have no API key. ")
          .appendSibling(new ChatComponentText("§c[Click here] §7to run §b/api new")
              .setChatStyle(new ChatStyle()
                  .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                      new ChatComponentText("§7Regenerate your personal Hypixel API key")))
                  .setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/api new")))));
      return;
    }

    sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7Requesting stats of ")
        .appendSibling(new ChatComponentText("§d" + playerName)
            .setChatStyle(new ChatStyle()
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ChatComponentText("§7Click to open §bPitPanda")))
                .setChatClickEvent(new ClickEvent(Action.OPEN_URL, "https://pitpanda.rocks/players/" + playerName))))
        .appendText("§7..."));

    mod.getHypixelAPI().getPlayerAsync(playerName)
        .thenAccept(player -> {
          if (player == null) {
            sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7Player §c" + playerName + " §7does not exist."));
            return;
          }

          final String uuid = player.getUuid();

          mod.getHypixelAPI().getGuildByPlayerAsync(uuid)
              .exceptionally(exception -> {
                HypixelPitStats.getLogger().error("Failed to fetch guild of player {}", playerName, exception);
                return null;
              })
              .thenAccept(guild -> {
                sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] " + player.getFormattedName() + " §8- ")
                    .appendSibling(new ChatComponentText("§7[Plancke] ")
                        .setChatStyle(new ChatStyle()
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText("§7Check general stats of the player on §bPlancke")))
                            .setChatClickEvent(new ClickEvent(Action.OPEN_URL,
                                "https://plancke.io/hypixel/player/stats/" + uuid))))
                    .appendSibling(new ChatComponentText("§7[PitPanda] ")
                        .setChatStyle(new ChatStyle()
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText("§7Check The Pit stats of the player on §bPitPanda")))
                            .setChatClickEvent(new ClickEvent(Action.OPEN_URL,
                                "https://pitpanda.rocks/players/" + uuid))))
                    .appendSibling(new ChatComponentText("§7[SkyCrypt] ")
                        .setChatStyle(new ChatStyle()
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText("§7Check SkyBlock stats of the player on §bSkyCrypt")))
                            .setChatClickEvent(new ClickEvent(Action.OPEN_URL,
                                "https://sky.shiiyu.moe/stats/" + uuid))))
                    .appendSibling(new ChatComponentText("§7[NameMC] ")
                        .setChatStyle(new ChatStyle()
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText("§7Check name history of the player on §bNameMC")))
                            .setChatClickEvent(new ClickEvent(Action.OPEN_URL,
                                "https://namemc.com/profile/" + uuid)))));

                final GuiStats screen;

                try {
                  screen = GuiStats.create(player, guild);
                } catch (final Throwable exception) {
                  HypixelPitStats.getLogger().error("Failed to create gui stats for player {}", playerName, exception);
                  sender.addChatMessage(
                      new ChatComponentText("§9[§6PIT§9] §7Failed to create a stats gui. §c§lREPORT THIS!"));
                  return;
                }

                GuiScreenDisplayer.display(screen);
              });
        })
        .exceptionally(exception -> {
          if (exception instanceof CompletionException && exception.getCause() != null) {
            exception = exception.getCause();
          }

          HypixelPitStats.getLogger().error("Failed to fetch player data by name {}", playerName, exception);

          if (exception instanceof HypixelApiException) {
            sender.addChatMessage(
                new ChatComponentText("§9[§6PIT§9] §7Unexpected API error: §c" + exception.getMessage()));
          } else {
            sender.addChatMessage(new ChatComponentText("§9[§6PIT§9] §7Unexpected error: §c" + exception));
          }

          return null;
        });
  }

  @Override
  public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
    if (args.length != 1) {
      return null;
    }

    final NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
    final List<String> players;

    if (netHandler == null) {
      players = Collections.emptyList();
    } else {
      players = netHandler.getPlayerInfoMap().stream()
          .map(info -> info.getGameProfile().getName())
          .collect(Collectors.toList());
    }

    return CommandBase.getListOfStringsMatchingLastWord(args, players);
  }

  @Override
  public boolean isUsernameIndex(final String[] args, final int index) {
    return index == 0;
  }
}
