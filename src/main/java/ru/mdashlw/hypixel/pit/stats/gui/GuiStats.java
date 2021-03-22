package ru.mdashlw.hypixel.pit.stats.gui;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import ru.mdashlw.hypixel.api.data.GameType;
import ru.mdashlw.hypixel.api.data.Guild;
import ru.mdashlw.hypixel.api.data.Player;
import ru.mdashlw.hypixel.api.data.Player.Stats.Pit;
import ru.mdashlw.hypixel.api.data.Player.Stats.Pit.Profile;
import ru.mdashlw.hypixel.api.data.Player.Stats.Pit.Profile.Killstreak;
import ru.mdashlw.hypixel.api.data.Player.Stats.Pit.Profile.Perk;
import ru.mdashlw.hypixel.api.util.JsonUtils;
import ru.mdashlw.hypixel.api.util.NumberUtils;
import ru.mdashlw.hypixel.pit.stats.util.DurationUtils;
import ru.mdashlw.hypixel.pit.stats.util.ItemStackUtils;

public final class GuiStats extends GuiScreen {

  private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation(
      "textures/gui/container/inventory.png");
  private static final ResourceLocation CHEST_TEXTURE = new ResourceLocation(
      "textures/gui/container/generic_54.png");

  private final List<? extends Slot> slots;
  private final int xSize;
  private final int ySize;
  private int x;
  private int y;

  public GuiStats(final List<? extends Slot> slots) {
    this.slots = slots;
    this.xSize = 176;
    this.ySize = 222;
    this.allowUserInput = true;
  }

  public static GuiStats create(final Player player, final Guild guild) {
    final List<Slot> slots = new ArrayList<>();
    final Pit stats = player.getStats().getPit();
    final Profile profile = stats.getProfile();
    final JsonNode statistics = stats.getStatistics();

    final List<ItemStack> enderChest = profile.getEnderChest();

    for (int column = 0; column < 6; column++) {
      for (int row = 0; row < 9; row++) {
        final int index = row + column * 9;
        final ItemStack itemStack = index < enderChest.size() ? enderChest.get(index) : null;

        slots.add(new Slot(8 + row * 18, 18 + column * 18, itemStack));
      }
    }

    final List<ItemStack> inventory = profile.getInventory();

    for (int column = 0; column < 3; column++) {
      for (int row = 0; row < 9; row++) {
        final int index = 9 + row + column * 9;
        final ItemStack itemStack = index < inventory.size() ? inventory.get(index) : null;

        slots.add(new Slot(8 + row * 18, 139 + column * 18, itemStack));
      }
    }

    for (int row = 0; row < 9; row++) {
      final int index = row;
      final ItemStack itemStack = index < inventory.size() ? inventory.get(index) : null;

      slots.add(new Slot(8 + row * 18, 197, itemStack));
    }

    final List<ItemStack> armor = profile.getArmor();

    for (int index = 0; index < 4; index++) {
      final ItemStack itemStack = index < armor.size() ? armor.get(index) : null;

      slots.add(new SlotArmor(3 - index, -16, 141 + (3 - index) * 18, itemStack));
    }

    final List<ItemStack> mysticWell = new ArrayList<>();

    mysticWell.add(ItemStackUtils.withDisplay(
        new ItemStack(Blocks.enchanting_table),
        "§dMystic Well",
        Collections.emptyList()
    ));
    mysticWell.addAll(profile.getMysticWellItem());
    mysticWell.addAll(profile.getMysticWellPants());

    for (int index = 0; index < 3; index++) {
      final ItemStack itemStack = index < mysticWell.size() ? mysticWell.get(index) : null;

      slots.add(new Slot(176, 141 + index * 18, itemStack));
    }

    final List<ItemStack> customItems = new ArrayList<>();

    customItems.add(ItemStackUtils.withDisplay(
        ItemStackUtils.withLazySkullOwner(
            new ItemStack(Items.skull, 1, 3),
            player.getName()
        ),
        player.getFormattedName(),
        Arrays.asList(
            String.format(Locale.US, "§7Hypixel Level: §6%.2f", player.getLevel()),
            String.format(Locale.US, "§7Achievement Points: §e%,d", player.getAchievementPoints()),
            "§7Guild: §b" + (guild == null ? "None" : guild.getName()),
            String.format(Locale.US, "§7Karma: §d%,d", player.getKarma()),
            "",
            "§7Status: " + (player.getLastLogin() == 0 || player.getLastLogout() == 0
                ? "§cUnknown"
                : (player.isOnline() ? "§aOnline" : "§cOffline")),
            player.isOnline() ? ("§7For " + (player.getLastLogin() == 0 ? "§cUnknown" :
                "§b" + DurationUtils.format(Duration.ofMillis(System.currentTimeMillis() - player.getLastLogin()))))
                : ("§7Last Online: " +
                    (player.getLastLogout() == 0 ? "§cUnknown"
                        : "§b" + DurationUtils.format(
                            Duration.ofMillis(System.currentTimeMillis() - player.getLastLogout()))
                            + " ago")),
            (player.isOnline()
                ? ("§7Game: §9")
                : ("§7Last Game: §9")) + player.getMostRecentGameType().getDisplayName()
        )
    ));
    customItems.add(ItemStackUtils.withDisplay(
        new ItemStack(Items.name_tag),
        profile.getFormattedLevel() + ' ' + player.getColoredName(),
        Arrays.asList(
            String.format(Locale.US, "§7Gold: §6%,dg", profile.getCash()),
            String.format(Locale.US, "§7Total XP: §b%,d XP", profile.getXP()),
            "",
            String.format(Locale.US, "§7Kills: §c%,d", JsonUtils.getOptionalInt(statistics, "kills")),
            String.format(Locale.US, "§7Hours played: §e%,d",
                JsonUtils.getOptionalInt(statistics, "playtime_minutes") / 60),
            "",
            profile.getBounty() == 0 ? null : String.format(Locale.US, "§7Bounty: §6%,dg", profile.getBounty()),
            profile.getBounty() == 0 ? null : "",
            String.format(Locale.US, "§7Renown: §e%,d", profile.getRenown()),
            "",
            "§7Last Seen: " + (profile.getLastSave() == 0
                ? "§cUnknown"
                : "§b" + DurationUtils.format(Duration.ofMillis(System.currentTimeMillis() - profile.getLastSave()))
                    + " ago")
        )
    ));

    final List<String> perks = new ArrayList<>();

    for (int i = 0; i < 4; i++) {
      final String key = profile.getSelectedPerk(i);

      if (key == null || key.equals("null")) {
        perks.add("§e#" + (i + 1) + ": §cNone");
      } else {
        final Perk perk = Pit.getPerks().get(key);

        perks.add("§e#" + (i + 1) + ": §a" + perk.getName());
        perks.addAll(perk.getDescription(profile));
      }

      perks.add("");
    }

    perks.remove(perks.size() - 1);
    customItems.add(ItemStackUtils.withDisplay(
        new ItemStack(Items.diamond),
        "§bPerks",
        perks
    ));

    final Map<String, Integer> unlocks = profile.getUnlocks();
    final List<String> passives = new ArrayList<>();

    Pit.getUpgrades().forEach((key, upgrade) -> {
      final int tier = unlocks.getOrDefault(key, -1);

      if (tier == -1) {
        passives.add("§9" + upgrade.getName() + ": §cLocked");
      } else {
        passives.add("§9" + upgrade.getName() + ": §e" + NumberUtils.toRomanNumeral(tier + 1));
        passives.addAll(upgrade.getDescriptions().get(tier));
      }

      passives.add("");
    });

    passives.remove(passives.size() - 1);
    customItems.add(ItemStackUtils.withDisplay(
        new ItemStack(Items.cake),
        "§aPassives",
        passives
    ));

    final List<String> killstreaks = new ArrayList<>();

    for (int i = 0; i < 4; i++) {
      final String key = profile.getSelectedKillstreak(i);

      if (key == null || key.equals("null")) {
        killstreaks.add("§e#" + (i + 1) + ": §cNone");
      } else {
        final Killstreak killstreak = Pit.getKillstreaks().get(key);

        killstreaks.add("§e#" + (i + 1) + ": §a" + killstreak.getName());
        killstreaks.add(
            killstreak.getInterval() == -1 ? "§7Megastreak" : ("§7Every §c" + killstreak.getInterval() + " §7kills"));
      }

      killstreaks.add("");
    }

    killstreaks.remove(killstreaks.size() - 1);
    customItems.add(ItemStackUtils.withDisplay(
        new ItemStack(Items.golden_boots),
        "§aKillstreaks",
        killstreaks
    ));
    customItems.add(ItemStackUtils.withDisplay(
        ItemStackUtils.withNoAttributeModifiers(new ItemStack(Items.iron_sword)),
        "§cOffensive Stats",
        Arrays.asList(
            String.format(Locale.US, "§7Kills: §a%,d", JsonUtils.getOptionalInt(statistics, "kills")),
            String.format(Locale.US, "§7Assists: §a%,d", JsonUtils.getOptionalInt(statistics, "assists")),
            String.format(Locale.US, "§7Sword Hits: §a%,d", JsonUtils.getOptionalInt(statistics, "sword_hits")),
            String.format(Locale.US, "§7Arrows Shot: §a%,d", JsonUtils.getOptionalInt(statistics, "arrows_fired")),
            String.format(Locale.US, "§7Arrow Hits: §a%,d", JsonUtils.getOptionalInt(statistics, "arrow_hits")),
            "",
            String.format(Locale.US, "§7Damage Dealt: §a%,d", JsonUtils.getOptionalInt(statistics, "damage_dealt")),
            String.format(Locale.US, "§7Melee Damage Dealt: §a%,d",
                JsonUtils.getOptionalInt(statistics, "melee_damage_dealt")),
            String.format(Locale.US, "§7Bow Damage Dealt: §a%,d",
                JsonUtils.getOptionalInt(statistics, "bow_damage_dealt")),
            "",
            String.format(Locale.US, "§7Highest Streak: §a%,d", JsonUtils.getOptionalInt(statistics, "max_streak"))
        )
    ));
    customItems.add(ItemStackUtils.withDisplay(
        new ItemStack(Items.iron_chestplate),
        "§9Defensive Stats",
        Arrays.asList(
            String.format(Locale.US, "§7Deaths: §a%,d", JsonUtils.getOptionalInt(statistics, "deaths")),
            "",
            String.format(Locale.US, "§7Damage Taken: §a%,d", JsonUtils.getOptionalInt(statistics, "damage_received")),
            String.format(Locale.US, "§7Melee Damage Taken: §a%,d",
                JsonUtils.getOptionalInt(statistics, "melee_damage_received")),
            String.format(Locale.US, "§7Bow Damage Taken: §a%,d",
                JsonUtils.getOptionalInt(statistics, "bow_damage_received"))
        )
    ));
    customItems.add(ItemStackUtils.withDisplay(
        new ItemStack(Items.wheat),
        "§ePerformance Stats",
        Arrays.asList(
            String.format(Locale.US, "§7XP: §b%,d", profile.getXP()),
            "",
            String.format(Locale.US, "§7K/D: §a%.3f",
                NumberUtils.ratio(
                    JsonUtils.getOptionalInt(statistics, "kills"),
                    JsonUtils.getOptionalInt(statistics, "deaths")
                )),
            String.format(Locale.US, "§7K+A/D: §a%.3f",
                NumberUtils.ratio(
                    JsonUtils.getOptionalInt(statistics, "kills") + JsonUtils.getOptionalInt(statistics, "assists"),
                    JsonUtils.getOptionalInt(statistics, "deaths")
                )),
            "",
            String.format(Locale.US, "§7Damage dealt/taken: §a%.3f",
                NumberUtils.ratio(
                    JsonUtils.getOptionalInt(statistics, "damage_dealt"),
                    JsonUtils.getOptionalInt(statistics, "damage_received")
                )),
            String.format(Locale.US, "§7Arrows hit/shot: §a%.3f",
                NumberUtils.ratio(
                    JsonUtils.getOptionalInt(statistics, "arrow_hits"),
                    JsonUtils.getOptionalInt(statistics, "arrows_fired")
                )),
            "",
            String.format(Locale.US, "§7Hours played: §a%,d",
                JsonUtils.getOptionalInt(statistics, "playtime_minutes") / 60),
            String.format(Locale.US, "§7Gold/hour: §a%,.3f",
                NumberUtils.ratio(
                    JsonUtils.getOptionalInt(statistics, "cash_earned"),
                    JsonUtils.getOptionalInt(statistics, "playtime_minutes") / 60
                )),
            String.format(Locale.US, "§7K+A/hour: §a%.3f",
                NumberUtils.ratio(
                    JsonUtils.getOptionalInt(statistics, "kills") + JsonUtils.getOptionalInt(statistics, "assists"),
                    JsonUtils.getOptionalInt(statistics, "playtime_minutes") / 60
                ))
        )
    ));
    customItems.add(ItemStackUtils.withDisplay(
        new ItemStack(Blocks.obsidian),
        "§dMiscellaneous Stats",
        Arrays.asList(
            String.format(Locale.US, "§7Left Clicks: §a%,d", JsonUtils.getOptionalInt(statistics, "left_clicks")),
            String.format(Locale.US, "§7Gold Earned: §a%,d", JsonUtils.getOptionalInt(statistics, "cash_earned")),
            String.format(Locale.US, "§7Diamond Items Purchased: §a%,d",
                JsonUtils.getOptionalInt(statistics, "diamond_items_purchased")),
            String.format(Locale.US, "§7Chat Messages: §a%,d", JsonUtils.getOptionalInt(statistics, "chat_messages")),
            "",
            String.format(Locale.US, "§7Blocks Placed: §a%,d", JsonUtils.getOptionalInt(statistics, "blocks_placed")),
            String.format(Locale.US, "§7Blocks Broken: §a%,d", JsonUtils.getOptionalInt(statistics, "blocks_broken")),
            "",
            String.format(Locale.US, "§7Jumps into Pit: §a%,d",
                JsonUtils.getOptionalInt(statistics, "jumped_into_pit")),
            String.format(Locale.US, "§7Launcher Launches: §a%,d",
                JsonUtils.getOptionalInt(statistics, "launched_by_launchers")),
            "",
            String.format(Locale.US, "§7Golden Apples Eaten: §a%,d",
                JsonUtils.getOptionalInt(statistics, "gapple_eaten")),
            String.format(Locale.US, "§7Golden Heads Eaten: §a%,d",
                JsonUtils.getOptionalInt(statistics, "ghead_eaten")),
            "",
            String.format(Locale.US, "§7Lava Buckets Emptied: §a%,d",
                JsonUtils.getOptionalInt(statistics, "lava_bucket_emptied")),
            String.format(Locale.US, "§7Fishing Rods Launched: §a%,d",
                JsonUtils.getOptionalInt(statistics, "fishing_rod_launched")),
            "",
            String.format(Locale.US, "§7Contracts Completed: §a%,d",
                JsonUtils.getOptionalInt(statistics, "contracts_completed")),
            "",
            String.format(Locale.US, "§7Wheat Farmed: §a%,d", JsonUtils.getOptionalInt(statistics, "wheat_farmed")),
            String.format(Locale.US, "§7Gold from Farming: §a%,d",
                JsonUtils.getOptionalInt(statistics, "gold_from_farming")),
            "§7Gold from Selling Fish: §cN/A", // todo
            "",
            String.format(Locale.US, "§7King's Quest Completed: §a%,d",
                JsonUtils.getOptionalInt(statistics, "king_quest_completion")),
            String.format(Locale.US, "§7Sewer Treasures Found: §a%,d",
                JsonUtils.getOptionalInt(statistics, "sewer_treasures_found")),
            "",
            String.format(Locale.US, "§7Night Quest Completed: §a%,d",
                JsonUtils.getOptionalInt(statistics, "night_quests_completed")),
            "",
            String.format(Locale.US, "§7Fished Anything: §a%,d",
                JsonUtils.getOptionalInt(statistics, "fished_anything")),
            String.format(Locale.US, "§7Fished Fish: §a%,d", JsonUtils.getOptionalInt(statistics, "fishes_fished"))
        )
    ));

    for (int row = 0; row < 9; row++) {
      final int index = row;
      final ItemStack itemStack = index < customItems.size() ? customItems.get(index) : null;

      slots.add(new Slot(8 + row * 18, -13, itemStack));
    }

    return new GuiStats(slots);
  }

  @Override
  public void initGui() {
    super.initGui();
    this.x = (this.width - this.xSize) / 2;
    this.y = (this.height - this.ySize) / 2;
  }

  @Override
  public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
    this.drawDefaultBackground();
    GlStateManager.color(1F, 1F, 1F, 1F);

    this.mc.getTextureManager().bindTexture(GuiStats.INVENTORY_TEXTURE);
    this.drawTexturedModalRect(this.x - 24, this.y + 133, 0, 0, 25, 80);
    this.drawTexturedModalRect(this.x - 24, this.y + 213, 0, 160, 25, 6);
    this.drawTexturedModalRect(this.x + this.xSize - 1 + 18, this.y + 133, 169, 0, 7, 61);
    this.drawTexturedModalRect(this.x + this.xSize - 1, this.y + 133, 7, 0, 18, 61);
    this.drawTexturedModalRect(this.x + this.xSize - 1, this.y + 194, 151, 159, 25, 7);
    this.mc.getTextureManager().bindTexture(GuiStats.CHEST_TEXTURE);
    this.drawTexturedModalRect(this.x, this.y + 4, 0, 4, this.xSize, 121);
    this.drawTexturedModalRect(this.x, this.y + 125, 0, 126, this.xSize, 96);
    this.drawTexturedModalRect(this.x, this.y - 31, 0, 0, this.xSize, 35);

    GlStateManager.disableRescaleNormal();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.disableDepth();
    super.drawScreen(mouseX, mouseY, partialTicks);
    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.pushMatrix();
    GlStateManager.translate((float) this.x, (float) this.y, 0F);
    GlStateManager.color(1F, 1F, 1F, 1F);
    GlStateManager.enableRescaleNormal();
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
    GlStateManager.color(1F, 1F, 1F, 1F);

    Slot hoveredSlot = null;

    for (final Slot slot : this.slots) {
      this.drawSlot(slot);

      if (hoveredSlot == null && this.isMouseOverSlot(slot, mouseX, mouseY)) {
        hoveredSlot = slot;

        final int x = slot.x;
        final int y = slot.y;

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        this.drawGradientRect(x, y, x + 16, y + 16, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      }
    }

    RenderHelper.disableStandardItemLighting();
    this.fontRendererObj.drawString(GameType.PIT.getDisplayName(), 8, -25, 4210752);
    this.fontRendererObj.drawString(I18n.format("container.enderchest"), 8, 6, 4210752);
    this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 94, 4210752);
    RenderHelper.enableGUIStandardItemLighting();

    GlStateManager.popMatrix();

    if (hoveredSlot != null && hoveredSlot.hasItemStack()) {
      this.renderToolTip(hoveredSlot.getItemStack(), mouseX, mouseY);
    }

    GlStateManager.enableLighting();
    GlStateManager.enableDepth();
    RenderHelper.enableStandardItemLighting();
  }

  @Override
  protected void keyTyped(final char typedChar, final int keyCode) {
    if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
      this.mc.displayGuiScreen(null);
    }
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  public void drawSlot(final Slot slot) {
    final int x = slot.x;
    final int y = slot.y;
    final ItemStack itemStack = slot.getItemStack();
    boolean render = true;

    this.zLevel = 100F;
    this.itemRender.zLevel = 100F;

    if (itemStack == null) {
      final TextureAtlasSprite sprite = slot.getBackgroundSprite();

      if (sprite != null) {
        GlStateManager.disableLighting();
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        this.drawTexturedModalRect(x, y, sprite, 16, 16);
        GlStateManager.enableLighting();
        render = false;
      }
    }

    if (render) {
      GlStateManager.enableDepth();
      this.itemRender.renderItemAndEffectIntoGUI(itemStack, x, y);
      this.itemRender.renderItemOverlays(this.fontRendererObj, itemStack, x, y);
    }

    this.itemRender.zLevel = 0F;
    this.zLevel = 0F;
  }

  public boolean isMouseOverSlot(final Slot slot, final int mouseX, final int mouseY) {
    return this.isPointInRegion(slot.x, slot.y, 16, 16, mouseX, mouseY);
  }

  public boolean isPointInRegion(final int left, final int top, final int right, final int bottom,
      int pointX, int pointY) {
    pointX = pointX - this.x;
    pointY = pointY - this.y;
    return pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1;
  }

  public static class Slot {

    private final int x;
    private final int y;
    private final ItemStack itemStack;

    public Slot(final int x, final int y, final ItemStack itemStack) {
      this.x = x;
      this.y = y;
      this.itemStack = itemStack;
    }

    public String getTexture() {
      return null;
    }

    public final TextureAtlasSprite getBackgroundSprite() {
      final String textureName = this.getTexture();

      if (textureName == null) {
        return null;
      }

      return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(textureName);
    }

    public final boolean hasItemStack() {
      return this.itemStack != null;
    }

    public final int getX() {
      return this.x;
    }

    public final int getY() {
      return this.y;
    }

    public final ItemStack getItemStack() {
      return this.itemStack;
    }
  }

  public static final class SlotArmor extends Slot {

    private final int index;

    public SlotArmor(final int index, final int x, final int y, final ItemStack itemStack) {
      super(x, y, itemStack);
      this.index = index;
    }

    @Override
    public String getTexture() {
      return ItemArmor.EMPTY_SLOT_NAMES[this.index];
    }

    public int getIndex() {
      return this.index;
    }
  }
}
