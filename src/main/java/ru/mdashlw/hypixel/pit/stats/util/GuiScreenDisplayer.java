package ru.mdashlw.hypixel.pit.stats.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public final class GuiScreenDisplayer {

  private final GuiScreen screen;

  public GuiScreenDisplayer(final GuiScreen screen) {
    this.screen = screen;
  }

  public static void display(final GuiScreen screen) {
    new GuiScreenDisplayer(screen).display();
  }

  @SubscribeEvent
  public void onClientTick(final ClientTickEvent event) {
    if (event.phase != Phase.END) {
      return;
    }

    MinecraftForge.EVENT_BUS.unregister(this);
    Minecraft.getMinecraft().displayGuiScreen(this.screen);
  }

  public void display() {
    MinecraftForge.EVENT_BUS.register(this);
  }
}
