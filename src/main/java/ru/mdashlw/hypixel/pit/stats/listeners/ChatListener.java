package ru.mdashlw.hypixel.pit.stats.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ChatListener {

  private static final String NEW_KEY_PATTERN = "Your new API key is ";

  @SubscribeEvent(receiveCanceled = true)
  public void onChatMessageReceived(final ClientChatReceivedEvent event) {
    final String text = event.message.getUnformattedText();

    if (text.isEmpty()) {
      return;
    }

    if (text.startsWith(ChatListener.NEW_KEY_PATTERN)) {
      final String key = text.substring(ChatListener.NEW_KEY_PATTERN.length());
      final EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

      thePlayer.addChatMessage(new ChatComponentText("§9[§6PIT§9] ")
          .appendSibling(new ChatComponentText("§a[Click here] §7to use §9" + key)
              .setChatStyle(new ChatStyle()
                  .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                      new ChatComponentText("§7Update a Hypixel API key in §6Hypixel Pit Stats")))
                  .setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/pit " + key)))));
    }
  }

  public void register() {
    MinecraftForge.EVENT_BUS.register(this);
  }
}
