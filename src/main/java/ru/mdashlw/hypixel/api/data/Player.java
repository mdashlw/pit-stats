package ru.mdashlw.hypixel.api.data;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.hypixel.api.util.ILeveling;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import ru.mdashlw.hypixel.api.data.Player.Stats.Pit.Profile.Killstreak;
import ru.mdashlw.hypixel.api.data.Player.Stats.Pit.Profile.Perk;
import ru.mdashlw.hypixel.api.data.Player.Stats.Pit.Profile.Upgrade;
import ru.mdashlw.hypixel.api.util.JsonUtils;
import ru.mdashlw.hypixel.api.util.NumberUtils;
import ru.mdashlw.hypixel.api.util.PitLeveling;
import ru.mdashlw.hypixel.pit.stats.HypixelPitStats;

public final class Player {

  private final JsonNode data;

  public Player(final JsonNode data) {
    this.data = data;
  }

  public String getUuid() {
    return this.data.get("uuid").asText();
  }

  public String getName() {
    String name = JsonUtils.getOptionalText(this.data, "displayname");

    if (name != null) {
      return name;
    }

    name = JsonUtils.getOptionalText(this.data, "playername");

    if (name != null) {
      return name;
    }

    name = JsonUtils.getOptionalText(this.data, "username");

    if (name != null) {
      return name;
    }

    return "UnknownName";
  }

  public Rank getRank() {
    String rank = JsonUtils.getOptionalText(this.data, "rank");

    if (rank != null && !rank.equals("NORMAL")) {
      return Rank.valueOf(rank);
    }

    rank = JsonUtils.getOptionalText(this.data, "monthlyPackageRank");

    if (rank != null && !rank.equals("NONE")) {
      return Rank.valueOf(rank);
    }

    rank = JsonUtils.getOptionalText(this.data, "newPackageRank");

    if (rank != null && !rank.equals("NONE")) {
      return Rank.valueOf(rank);
    }

    rank = JsonUtils.getOptionalText(this.data, "packageRank");

    if (rank != null && !rank.equals("NONE")) {
      return Rank.valueOf(rank);
    }

    return Rank.NORMAL;
  }

  public String getFormattedName() {
    final String name = this.getName();
    final String customPrefix = JsonUtils.getOptionalText(this.data, "prefix");

    if (customPrefix != null) {
      return customPrefix + ' ' + name;
    }

    final Rank rank = this.getRank();
    final String rankColor = EnumChatFormatting.valueOf(
        JsonUtils.getOptionalText(this.data, "monthlyRankColor", "GOLD")).toString();
    final String rankPlusColor = EnumChatFormatting.valueOf(
        JsonUtils.getOptionalText(this.data, "rankPlusColor", "RED")).toString();
    final String prefix = rank.getPrefix()
        .replace("@", rankColor)
        .replace("$", rankPlusColor);

    return prefix + name;
  }

  public String getColoredName() {
    final String name = this.getName();
    final Rank rank = this.getRank();

    return rank.getColor() + name;
  }

  public double getLevel() {
    final int networkExp = JsonUtils.getOptionalInt(this.data, ILeveling.EXP_FIELD);
    final int networkLevel = JsonUtils.getOptionalInt(this.data, ILeveling.LVL_FIELD);
    final double exp = networkExp + ILeveling.getTotalExpToFullLevel(networkLevel + 1);

    return ILeveling.getExactLevel(exp);
  }

  public int getAchievementPoints() {
    return JsonUtils.getOptionalInt(this.data, "achievementPoints");
  }

  public int getKarma() {
    return JsonUtils.getOptionalInt(this.data, "karma");
  }

  public long getLastLogin() {
    return JsonUtils.getOptionalLong(this.data, "lastLogin");
  }

  public long getLastLogout() {
    return JsonUtils.getOptionalLong(this.data, "lastLogout");
  }

  public boolean isOnline() {
    final long lastLogin = this.getLastLogin();
    final long lastLogout = this.getLastLogout();

    return lastLogin != 0 && lastLogout != 0 && lastLogin > lastLogout;
  }

  public GameType getMostRecentGameType() {
    try {
      return GameType.valueOf(JsonUtils.getOptionalText(this.data, "mostRecentGameType", "UNKNOWN"));
    } catch (final IllegalArgumentException ignored) {
      return GameType.UNKNOWN;
    }
  }

  public Stats getStats() {
    return new Stats(JsonUtils.getOptionalObject(this.data, "stats"));
  }

  public enum Rank {
    NORMAL("§7", "§7"),
    VIP("§a[VIP] ", "§a"),
    VIP_PLUS("§a[VIP§6+§a] ", "§a"),
    MVP("§b[MVP] ", "§b"),
    MVP_PLUS("§b[MVP$+§b] ", "§b"),
    SUPERSTAR("@[MVP$++@] ", "§6"),
    YOUTUBER("§c[§fYOUTUBE§c] ", "§c"),
    JR_HELPER("§9[JR HELPER] ", "§9"),
    HELPER("§9[HELPER] ", "§9"),
    MODERATOR("§2[MOD] ", "§2"),
    GAME_MASTER("§2[GM] ", "§2"),
    ADMIN("§c[ADMIN] ", "§c");

    private final String prefix;
    private final String color;

    Rank(final String prefix, final String color) {
      this.prefix = prefix;
      this.color = color;
    }

    public String getPrefix() {
      return this.prefix;
    }

    public String getColor() {
      return this.color;
    }
  }

  public static final class Stats {

    private final JsonNode data;

    public Stats(final JsonNode data) {
      this.data = data;
    }

    public Pit getPit() {
      return new Pit(JsonUtils.getOptionalObject(this.data, "Pit"));
    }

    public static final class Pit {

      private static final Map<String, Upgrade> UPGRADES = new LinkedHashMap<>();
      private static final Map<String, Perk> PERKS = new HashMap<>();
      private static final Map<String, Killstreak> KILLSTREAKS = new HashMap<>();
      private static final String[] LEVEL_COLORS = {"§7", "§9", "§3", "§2", "§a", "§e", "§6§l", "§c§l", "§4§l", "§5§l",
          "§d§l", "§f§l", "§b§l"};
      private static final String[] PRESTIGE_COLORS = {"§7", "§9", "§9", "§9", "§9", "§e", "§e", "§e", "§e", "§e", "§6",
          "§6", "§6", "§6", "§6", "§c", "§c", "§c", "§c", "§c", "§5", "§5", "§5", "§5", "§5", "§d", "§d", "§d", "§d",
          "§d", "§f", "§f", "§f", "§f", "§f", "§b"};

      static {
        UPGRADES.put("xp_boost", new Upgrade("XP Boost", Arrays.asList(
            Arrays.asList("§7Earn §b+10% XP §7from all", "§7sources."),
            Arrays.asList("§7Earn §b+20% XP §7from all", "§7sources."),
            Arrays.asList("§7Earn §b+30% XP §7from all", "§7sources."),
            Arrays.asList("§7Earn §b+40% XP §7from all", "§7sources."),
            Arrays.asList("§7Earn §b+50% XP §7from all", "§7sources."),
            Arrays.asList("§7Earn §b+60% XP §7from all", "§7sources.")
        )));
        UPGRADES.put("cash_boost", new Upgrade("Gold Boost", Arrays.asList(
            Arrays.asList("§7Earn §6+10% gold (g) §7from", "§7kills and coin pickups."),
            Arrays.asList("§7Earn §6+20% gold (g) §7from", "§7kills and coin pickups."),
            Arrays.asList("§7Earn §6+30% gold (g) §7from", "§7kills and coin pickups."),
            Arrays.asList("§7Earn §6+40% gold (g) §7from", "§7kills and coin pickups."),
            Arrays.asList("§7Earn §6+50% gold (g) §7from", "§7kills and coin pickups."),
            Arrays.asList("§7Earn §6+60% gold (g) §7from", "§7kills and coin pickups.")
        )));
        UPGRADES.put("melee_damage", new Upgrade("Melee Damage", Arrays.asList(
            Collections.singletonList("§7Deal §c+1% §7melee damage."),
            Collections.singletonList("§7Deal §c+2% §7melee damage."),
            Collections.singletonList("§7Deal §c+3% §7melee damage."),
            Collections.singletonList("§7Deal §c+4% §7melee damage."),
            Collections.singletonList("§7Deal §c+5% §7melee damage."),
            Collections.singletonList("§7Deal §c+6% §7melee damage.")
        )));
        UPGRADES.put("bow_damage", new Upgrade("Bow Damage", Arrays.asList(
            Collections.singletonList("§7Deal §c+3% §7bow damage."),
            Collections.singletonList("§7Deal §c+6% §7bow damage."),
            Collections.singletonList("§7Deal §c+9% §7bow damage."),
            Collections.singletonList("§7Deal §c+12% §7bow damage."),
            Collections.singletonList("§7Deal §c+15% §7bow damage."),
            Collections.singletonList("§7Deal §c+18% §7bow damage.")
        )));
        UPGRADES.put("damage_reduction", new Upgrade("Damage Reduction", Arrays.asList(
            Collections.singletonList("§7Receive §9-1% §7damage."),
            Collections.singletonList("§7Receive §9-2% §7damage."),
            Collections.singletonList("§7Receive §9-3% §7damage."),
            Collections.singletonList("§7Receive §9-4% §7damage."),
            Collections.singletonList("§7Receive §9-5% §7damage."),
            Collections.singletonList("§7Receive §9-6% §7damage.")
        )));
        UPGRADES.put("build_battler", new Upgrade("Build Battler", Arrays.asList(
            Arrays.asList("§7Your blocks stay §a+60%", "§7longer."),
            Arrays.asList("§7Your blocks stay §a+120%", "§7longer."),
            Arrays.asList("§7Your blocks stay §a+180%", "§7longer."),
            Arrays.asList("§7Your blocks stay §a+240%", "§7longer."),
            Arrays.asList("§7Your blocks stay §a+300%", "§7longer."),
            Arrays.asList("§7Your blocks stay §a+360%", "§7longer.")
        )));
        UPGRADES.put("el_gato", new Upgrade("El Gato", Arrays.asList(
            Arrays.asList("§dFirst kill §7each life", "§7rewards §6+§65g §b+5 XP§7."),
            Arrays.asList("§dFirst 2 kills §7each life", "§7reward §6+§65g §b+5 XP§7."),
            Arrays.asList("§dFirst 3 kills §7each life", "§7reward §6+§65g §b+5 XP§7."),
            Arrays.asList("§dFirst 4 kills §7each life", "§7reward §6+§65g §b+5 XP§7."),
            Arrays.asList("§dFirst 5 kills §7each life", "§7reward §6+§65g §b+5 XP§7."),
            Arrays.asList("§dFirst 6 kills §7each life", "§7reward §6+§65g §b+5 XP§7.")
        )));
        PERKS.put("golden_heads", new Perk("Golden Heads",
            Arrays.asList("§7Golden apples you earn turn", "§7into §6Golden Heads§7.")));
        PERKS.put("fishing_rod", new Perk("Fishing Rod",
            Collections.singletonList("§7Spawn with a fishing rod.")));
        PERKS.put("lava_bucket", new Perk("Lava Bucket",
            Collections.singletonList("§7Spawn with a lava bucket.")));
        PERKS.put("strength_chaining", new Perk("Strength-Chaining",
            Arrays.asList("§c+8% damage §7for 7s", "§7stacking on kill.")));
        PERKS.put("endless_quiver", new Perk("Endless Quiver",
            Arrays.asList("§7Get §f3 arrows §7on arrow", "§7hit.")));
        PERKS.put("free_blocks", new Perk("Mineman",
            Arrays.asList("§7Spawn with §f24 cobblestone", "§7and a diamond pickaxe.",
                "", "§7+§f3 blocks §7on kill.")));
        PERKS.put("safety_first", new Perk("Safety First",
            Collections.singletonList("§7Spawn with a helmet.")));
        PERKS.put("barbarian", new Perk("Barbarian",
            Arrays.asList("§7Replaces your sword with a", "§7stronger axe.")));
        PERKS.put("trickle_down", new Perk("Trickle-down",
            Arrays.asList("§7Gold ingots reward", "§6+§610g§7 and heal §c2❤§7.")));
        PERKS.put("lucky_diamond", new Perk("Lucky Diamond",
            Arrays.asList("§730% chance to upgrade", "§7dropped armor pieces from", "§7kills to §bdiamond§7.",
                "", "§7Upgraded pieces warp to", "§7your inventory.")));
        PERKS.put("spammer", new Perk("Spammer",
            Arrays.asList("§7Double base gold reward on", "§7targets you've shot an", "§7arrow in.",
                "", "§6+2g§7 on assists.")));
        PERKS.put("bounty_hunter", new Perk("Bounty Hunter",
            Arrays.asList("§6+4g §7on all kills.", "§7Earn bounty assist shares.",
                "", "§c+1% damage§7/100g bounty", "§7on target.")));
        PERKS.put("streaker", new Perk("Streaker",
            Arrays.asList("§7Triple streak kill §bXP", "§7bonus.")));
        PERKS.put("assistant_streaker", new Perk("Assistant Streaker",
            Arrays.asList("§7Assists count their", "§aparticipation §7towards", "§7killstreaks.",
                "", "§7Don't lose §clives §7on", "§7mystic items from dying", "§7after activating a",
                "§cmegastreak§7.")));
        PERKS.put("coop_cat", new Perk("Co-op Cat",
            Arrays.asList("§7Earn §b+50% XP §7and", "§6+50%g §7on all assists.")));
        PERKS.put("conglomerate", new Perk("Conglomerate",
            Arrays.asList("§7Don't earn §bXP §7from", "§7kills. The §bXP §7you would", "§7earn is converted to §6gold",
                "§7at a §e20% §7ratio.")));
        PERKS.put("gladiator", new Perk("Gladiator",
            Arrays.asList("§7Receive §9-3% §7damage per", "§7nearby player.",
                "", "§712 blocks range.", "§7Minimum 3, max 10 players.")));
        PERKS.put("vampire", new Perk("Vampire",
            Arrays.asList("§7Don't earn golden apples.", "§7Heal §c0.5❤ §7on hit.", "§7Tripled on arrow crit.",
                "§cRegen I §7(8s) on kill.")));
        PERKS.put("recon", new Perk("Recon",
            Arrays.asList("§7Each fourth arrow shot at", "§7someone rewards §b+40 XP", "§7and deals §c+50% damage.")));
        PERKS.put("overheal", new Perk("Overheal",
            Collections.singletonList("§7Double healing item limits.")));
        PERKS.put("rambo", new Perk("Rambo",
            Arrays.asList("§7Don't earn golden apples.", "§7Max health: §c8❤", "§7Refill all health on kill.")));
        PERKS.put("olympus", new Perk("Olympus",
            Arrays.asList("§7Golden apples you earn turn", "§7into §bOlympus Potions§7.",
                "", "§bOlympus Potion", "§9Speed I (0:24)", "§9Regeneration III (0:10)", "§9Resistance II (0:04)",
                "§bGain +27 XP!", "§7Can only hold 1")));
        PERKS.put("dirty", new Perk("Dirty",
            Arrays.asList("§7Gain Resistance II (4s) on", "§7kill.")));
        PERKS.put("first_strike", new Perk("First Strike",
            Arrays.asList("§7First hit on a player deals", "§c+35% damage §7and grants", "§eSpeed I §7(5s).")));
        PERKS.put("soup", new Perk("Soup",
            Arrays.asList("§7Golden apples you earn turn", "§7into §aTasty Soup§7. You", "§7also earn soup on assists.",
                "", "§aTasty Soup", "§9Speed I (0:07)", "§a1.5❤ Heal §7+ §61❤ Absorption",
                "§cNext melee hit +15% damage")));
        PERKS.put("marathon", new Perk("Marathon",
            Arrays.asList("§7Cannot wear boots.", "§7While you have speed:", "§8◼ §7Deal §c+18% §7damage",
                "§8◼ §7Receive §9-18% §7damage")));
        PERKS.put("thick", new Perk("Thick",
            Collections.singletonList("§7You have §c+2 Max ❤§7.")));
        PERKS.put("kung_fu_knowledge", new Perk("Kung Fu Knowledge",
            Arrays.asList("§7No sword damage.", "§7Fists hit like a truck.", "§7Gain speed II (5s) every",
                "§7fourth strike on a player.")));
        KILLSTREAKS.put("overdrive", new Killstreak("Overdrive", -1));
        KILLSTREAKS.put("beastmode", new Killstreak("Beastmode", -1));
        KILLSTREAKS.put("hermit", new Killstreak("Hermit", -1));
        KILLSTREAKS.put("highlander", new Killstreak("Highlander", -1));
        KILLSTREAKS.put("grand_finale", new Killstreak("Grand Finale", -1));
        KILLSTREAKS.put("to_the_moon", new Killstreak("To the moon", -1));
        KILLSTREAKS.put("uberstreak", new Killstreak("Uberstreak", -1));
        KILLSTREAKS.put("second_gapple", new Killstreak("Second Gapple", 3));
        KILLSTREAKS.put("extra_xp", new Killstreak("Explicious", 3));
        KILLSTREAKS.put("res_and_regen", new Killstreak("R&R", 3));
        KILLSTREAKS.put("arquebusier", new Killstreak("Arquebusier", 3));
        KILLSTREAKS.put("khanate", new Killstreak("Khanate", 3));
        KILLSTREAKS.put("leech", new Killstreak("Leech", 3));
        KILLSTREAKS.put("speed_two", new Killstreak("Hero's Haste", 5));
        KILLSTREAKS.put("pungent", new Killstreak("Pungent", 5));
        KILLSTREAKS.put("fight_or_flight", new Killstreak("Fight or Flight", 5));
        KILLSTREAKS.put("tough_skin", new Killstreak("Tough Skin", 5));
        KILLSTREAKS.put("feast", new Killstreak("Feast", 7));
        KILLSTREAKS.put("counter_strike", new Killstreak("Counter-Strike", 7));
        KILLSTREAKS.put("gold_nanofactory", new Killstreak("Gold Nano-factory", 7));
        KILLSTREAKS.put("tactical_retreat", new Killstreak("Tactical Retreat", 7));
        KILLSTREAKS.put("glass_sword", new Killstreak("Glass Pickaxe", 7));
        KILLSTREAKS.put("assured_strike", new Killstreak("Assured Strike", 7));
        KILLSTREAKS.put("xp_stack", new Killstreak("XP Stack", 10));
        KILLSTREAKS.put("gold_stack", new Killstreak("Gold Stack", 10));
        KILLSTREAKS.put("super_streaker", new Killstreak("Super Streaker", 10));
        KILLSTREAKS.put("ice_cube", new Killstreak("Ice Cube", 10));
        KILLSTREAKS.put("shield_aura", new Killstreak("Aura of Protection", 10));
        KILLSTREAKS.put("monster", new Killstreak("Monster", 25));
        KILLSTREAKS.put("sponge_steve", new Killstreak("Spongesteve", 25));
        KILLSTREAKS.put("apostle", new Killstreak("Apostle to RNGesus", 25));
        KILLSTREAKS.put("withercraft", new Killstreak("Withercraft", 25));
        /*KILLSTREAK_DESCRIPTIONS.put("overdrive", Arrays.asList(
            "§7Triggers on: §c50 kills",
            "",
            "§7On trigger:",
            "§a■ §7Perma §eSpeed I§7.",
            "§a■ §7Earn §b+100% XP §7from kills.",
            "§a■ §7Earn §6+50% gold §7from kills.",
            "",
            "§7BUT:",
            "§c■ §7Receive §c+0.1❤ §7§overy",
            "§7true damage per 5 kills over 50.",
            "",
            "§7On death:",
            "§e■ §7Gain §b4,000 XP§7."));
        KILLSTREAK_DESCRIPTIONS.put("beastmode", Arrays.asList(
            "§7Triggers on: §c50 kills",
            "",
            "§7On trigger:",
            "§a■ §7Gain a §bDiamond Helmet§7.",
            "§a■ §7Deal §c+25% §7damage.",
            "§a■ §7Earn §b+50% XP §7from kills.",
            "§a■ §7Earn §6+75% gold §7from kills.",
            "",
            "§7BUT:",
            "§c■ §7Receive §c+0.1❤ §7damage per 5 kills above 50.",
            "",
            "§7On death:",
            "§e■ §7Keep the §bDiamond Helmet§7."
        ));
        KILLSTREAK_DESCRIPTIONS.put("hermit", Arrays.asList(
            "§7Triggers on: §c50 kills",
            "",
            "§7From 0 kills:",
            "§a■ §7Placed blocks stay §f2x §7longer.",
            "§c■ §7Permanent §9Slowness I§7.",
            "",
            "§7On trigger:",
            "§a■ §7Permanent §9Resistance I§7.",
            "§a■ §7True damage immunity.",
            "§a■ §7Gain §f32 Bedrock §7+ §f16 §7every 10 kills.",
            "§a■ §7Earn §6+5% gold §7and §b+5%",
            "§bXP §7from kills for each 10 kills",
            "§7over 50, up to 200.",
            "",
            "§7BUT:",
            "§c■ §7Receive §c+0.3% §7damage per",
            "§7kill over 50."
        ));
        KILLSTREAK_DESCRIPTIONS.put("highlander", Arrays.asList(
            "§7Triggers on: §c50 kills",
            "",
            "§7On trigger:",
            "§a■ §7Perma §eSpeed I§7.",
            "§a■ §7Earn §6+110% gold §7from kills.",
            "§a■ §7Deal §c+33% damage §7vs bountied players.",
            "",
            "§7BUT:",
            "§c■ §6+5000g §7max bounty.",
            "§c■ §7Receive §c+0.3% §7damage",
            "§7from §6Bounty Hunter §7wearers per",
            "§7kill over 50.",
            "",
            "§7On death:",
            "§e■ §7Earn your own bounty aswell."
        ));*/
      }

      private final JsonNode data;

      public Pit(final JsonNode data) {
        this.data = data;
      }

      public static Map<String, Upgrade> getUpgrades() {
        return UPGRADES;
      }

      public static Map<String, Perk> getPerks() {
        return PERKS;
      }

      public static Map<String, Killstreak> getKillstreaks() {
        return KILLSTREAKS;
      }

      public Profile getProfile() {
        return new Profile(JsonUtils.getOptionalObject(this.data, "profile"));
      }

      public JsonNode getStatistics() {
        return JsonUtils.getOptionalObject(this.data, "pit_stats_ptl");
      }

      public static final class Profile {

        private final JsonNode data;

        public Profile(final JsonNode data) {
          this.data = data;
        }

        public List<ItemStack> parseInventory(final String field) {
          final JsonNode data = this.data.get(field);

          if (data == null) {
            return Collections.emptyList();
          }

          final byte[] bytes = JsonUtils.getByteArray(data, "data");

          if (bytes == null) {
            return Collections.emptyList();
          }

          final NBTTagCompound tag;

          try {
            tag = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
          } catch (final IOException exception) {
            HypixelPitStats.getLogger().error("Failed to parse inventory data", exception);
            return Collections.emptyList();
          }

          final List<ItemStack> itemStacks = new ArrayList<>();
          final NBTTagList itemListTag = tag.getTagList("i", 10);

          for (int i = 0; i < itemListTag.tagCount(); i++) {
            final NBTTagCompound itemTag = itemListTag.getCompoundTagAt(i);

            itemStacks.add(ItemStack.loadItemStackFromNBT(itemTag));
          }

          return itemStacks;
        }

        public List<ItemStack> getArmor() {
          return this.parseInventory("inv_armor");
        }

        public List<ItemStack> getInventory() {
          return this.parseInventory("inv_contents");
        }

        public List<ItemStack> getEnderChest() {
          return this.parseInventory("inv_enderchest");
        }

        public List<ItemStack> getMysticWellItem() {
          return this.parseInventory("mystic_well_item");
        }

        public List<ItemStack> getMysticWellPants() {
          return this.parseInventory("mystic_well_pants");
        }

        public long getLastSave() {
          return JsonUtils.getOptionalLong(this.data, "last_save");
        }

        public int getCash() {
          return (int) Math.round(JsonUtils.getOptionalDouble(this.data, "cash"));
        }

        public int getXP() {
          return JsonUtils.getOptionalInt(this.data, "xp");
        }

        public int getRenown() {
          return JsonUtils.getOptionalInt(this.data, "renown");
        }

        public int getPrestige() {
          final JsonNode data = this.data.get("prestiges");

          if (data == null) {
            return 0;
          }

          return data.size();
        }

        public Set<String> getRenownUnlocks() {
          final Set<String> renownUnlocks = new HashSet<>();
          final JsonNode data = this.data.get("renown_unlocks");

          if (data == null || data.isEmpty()) {
            return renownUnlocks;
          }

          for (final JsonNode node : data) {
            renownUnlocks.add(node.get("key").asText());
          }

          return renownUnlocks;
        }

        public Map<String, Integer> getUnlocks() {
          JsonNode data = this.data.get("unlocks_" + this.getPrestige());

          if (data == null) {
            data = this.data.get("unlocks");

            if (data == null) {
              return Collections.emptyMap();
            }
          }

          final Map<String, Integer> unlocks = new HashMap<>();

          for (final JsonNode node : data) {
            unlocks.put(node.get("key").asText(), node.get("tier").asInt());
          }

          return unlocks;
        }

        public int getLevel() {
          return PitLeveling.getLevel(this.getPrestige(), this.getXP());
        }

        public String getFormattedLevel() {
          final int prestige = this.getPrestige();
          final int level = this.getLevel();

          final String levelColor = Pit.LEVEL_COLORS[(int) Math.floor(level / 10D)];

          if (prestige == 0) {
            return "§7[" + levelColor + level + "§r§7]";
          }

          final String prestigeColor = Pit.PRESTIGE_COLORS[prestige];

          return prestigeColor + "[§e" + NumberUtils.toRomanNumeral(prestige) + prestigeColor + '-' + levelColor + level
              + "§7" + prestigeColor
              + ']';
        }

        public int getBounty() {
          final JsonNode data = this.data.get("bounties");

          if (data == null || data.isEmpty()) {
            return 0;
          }

          int bounty = 0;

          for (final JsonNode node : data) {
            bounty += node.get("amount").asInt();
          }

          return bounty;
        }

        public String getSelectedPerk(final int slot) {
          return JsonUtils.getOptionalText(this.data, "selected_perk_" + slot);
        }

        public String getSelectedKillstreak(final int slot) {
          return JsonUtils.getOptionalText(this.data, "selected_killstreak_" + slot);
        }

        public static final class Upgrade {

          private final String name;
          private final List<List<String>> descriptions;

          public Upgrade(final String name, final List<List<String>> descriptions) {
            this.name = name;
            this.descriptions = descriptions;
          }

          public String getName() {
            return this.name;
          }

          public List<List<String>> getDescriptions() {
            return this.descriptions;
          }
        }

        public static final class Perk {

          private final String name;
          private final List<String> description;

          public Perk(final String name, final List<String> description) {
            this.name = name;
            this.description = description;
          }

          public List<String> getDescription(final Profile profile) {
            if (this.name.equals("assistant_streaker") &&
                !profile.getRenownUnlocks().contains("unlock_perk_assistant_streaker")) {
              return this.description.subList(0, 3);
            }

            return this.description;
          }

          public String getName() {
            return this.name;
          }

          public List<String> getDescription() {
            return this.description;
          }
        }

        public static final class Killstreak {

          private final String name;
          private final int interval;

          public Killstreak(final String name, final int interval) {
            this.name = name;
            this.interval = interval;
          }

          public String getName() {
            return this.name;
          }

          public int getInterval() {
            return this.interval;
          }
        }
      }
    }
  }
}
