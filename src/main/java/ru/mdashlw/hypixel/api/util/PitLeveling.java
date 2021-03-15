package ru.mdashlw.hypixel.api.util;

public final class PitLeveling {

  private static final float[] PRESTIGE_MULTIPLIERS = {1F, 1.1F, 1.2F, 1.3F, 1.4F, 1.5F, 1.75F, 2F, 2.5F, 3F, 4F, 5F,
      6F, 7F, 8F, 9F, 10F, 12F, 14F, 16F, 18F, 20F, 24F, 28F, 32F, 36F, 40F, 45F, 50F, 75F, 100F, 101F, 101F, 101F,
      101F, 101F};
  private static final int[] PRESTIGE_XPS = {65950, 138510, 217680, 303430, 395760, 494700, 610140, 742040, 906930,
      1104780, 1368580, 1698330, 2094030, 2555680, 3083280, 3676830, 4336330, 5127730, 6051030, 7106230, 8293330,
      9612330, 11195130, 13041730, 15152130, 17526330, 20164330, 23132080, 26429580, 31375830, 37970830, 44631780,
      51292730, 57953680, 64614630, 71275580};
  private static final int[] LEVEL_XPS = {15, 30, 50, 75, 125, 300, 600, 800, 900, 1000, 1200, 1500, 0};

  private PitLeveling() {
  }

  // https://github.com/PitPanda/PitPandaProduction/blob/master/structures/Pit.js#L1443
  public static int getLevel(final int prestige, final int totalXP) {
    int xp = (prestige > 0) ? totalXP - PRESTIGE_XPS[prestige - 1] : totalXP;
    final float multiplier = PRESTIGE_MULTIPLIERS[prestige];
    double level = 0;

    while (xp > 0 && level < 120) {
      final double levelXp = LEVEL_XPS[(int) Math.floor(level / 10D)] * multiplier;

      if (xp >= levelXp * 10) {
        xp -= levelXp * 10;
        level += 10;
      } else {
        final double gain = Math.floor(xp / levelXp);

        level += gain;
        xp = 0;
      }
    }

    return (int) level;
  }
}
