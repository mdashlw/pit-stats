package ru.mdashlw.hypixel.api.util;

public final class PitLeveling {

    private PitLeveling() {
    }

    // https://github.com/PitPanda/PitPandaProduction/blob/e29cdd30f24c33397dbd47ff0e991b5b350c875d/structures/Pit.js#L1452
    public static int getLevel(final int prestige, long xp) {
        final float multiplier = Prestige.getPrestiges()[prestige].getMultiplier();
        double level = 0;

        while (xp > 0 && level < 120) {
            final double levelXp = Level.getLevels()[(int) Math.floor(level / 10)].getXp() * multiplier;

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

    public static final class Level {

        private static final Level[] LEVELS = {new Level(15, "§7"), new Level(30, "§9"), new Level(50, "§3"),
                                               new Level(75, "§2"), new Level(125, "§a"), new Level(300, "§e"),
                                               new Level(600, "§6§l"), new Level(800, "§c§l"), new Level(900, "§4§l"),
                                               new Level(1000, "§5§l"), new Level(1200, "§d§l"),
                                               new Level(1500, "§f§l"), new Level(0, "§b§l")};

        private final int xp;
        private final String colorCode;

        private Level(final int xp, final String colorCode) {
            this.xp = xp;
            this.colorCode = colorCode;
        }

        public static Level[] getLevels() {
            return LEVELS;
        }

        public int getXp() {
            return this.xp;
        }

        public String getColorCode() {
            return this.colorCode;
        }
    }

    public static final class Prestige {

        private static final Prestige[] PRESTIGES = {new Prestige(1.0F, 65950L, 65950L, "§7"),
                                                     new Prestige(1.1F, 72560L, 138510L, "§9"),
                                                     new Prestige(1.2F, 79170L, 217680L, "§9"),
                                                     new Prestige(1.3F, 85750L, 303430L, "§9"),
                                                     new Prestige(1.4F, 92330L, 395760L, "§9"),
                                                     new Prestige(1.5F, 98940L, 494700L, "§e"),
                                                     new Prestige(1.75F, 115440L, 610140L, "§e"),
                                                     new Prestige(2.0F, 131900L, 742040L, "§e"),
                                                     new Prestige(2.5F, 164890L, 906930L, "§e"),
                                                     new Prestige(3.0F, 197850L, 1104780L, "§e"),
                                                     new Prestige(4.0F, 263800L, 1368580L, "§6"),
                                                     new Prestige(5.0F, 329750L, 1698330L, "§6"),
                                                     new Prestige(6.0F, 395700L, 2094030L, "§6"),
                                                     new Prestige(7.0F, 461650L, 2555680L, "§6"),
                                                     new Prestige(8.0F, 527600L, 3083280L, "§6"),
                                                     new Prestige(9.0F, 593550L, 3676830L, "§c"),
                                                     new Prestige(10.0F, 659500L, 4336330L, "§c"),
                                                     new Prestige(12.0F, 791400L, 5127730L, "§c"),
                                                     new Prestige(14.0F, 923300L, 6051030L, "§c"),
                                                     new Prestige(16.0F, 1055200L, 7106230L, "§c"),
                                                     new Prestige(18.0F, 1187100L, 8293330L, "§5"),
                                                     new Prestige(20.0F, 1319000L, 9612330L, "§5"),
                                                     new Prestige(24.0F, 1582800L, 11195130L, "§5"),
                                                     new Prestige(28.0F, 1846600L, 13041730L, "§5"),
                                                     new Prestige(32.0F, 2110400L, 15152130L, "§5"),
                                                     new Prestige(36.0F, 2374200L, 17526330L, "§d"),
                                                     new Prestige(40.0F, 2638000L, 20164330L, "§d"),
                                                     new Prestige(45.0F, 2967750L, 23132080L, "§d"),
                                                     new Prestige(50.0F, 3297500L, 26429580L, "§d"),
                                                     new Prestige(75.0F, 4946250L, 31375830L, "§d"),
                                                     new Prestige(100.0F, 6595000L, 37970830L, "§f"),
                                                     new Prestige(101.0F, 6660950L, 44631780L, "§f"),
                                                     new Prestige(101.0F, 6660950L, 51292730L, "§f"),
                                                     new Prestige(101.0F, 6660950L, 57953680L, "§f"),
                                                     new Prestige(101.0F, 6660950L, 64614630L, "§f"),
                                                     new Prestige(101.0F, 6660950L, 71275580L, "§b"),
                                                     new Prestige(200.0F, 13190000L, 84465580L, "§b"),
                                                     new Prestige(300.0F, 19785000L, 104250580L, "§b"),
                                                     new Prestige(400.0F, 26380000L, 130630580L, "§b"),
                                                     new Prestige(500.0F, 32975000L, 163605580L, "§b"),
                                                     new Prestige(750.0F, 49462500L, 213068080L, "§1"),
                                                     new Prestige(1000.0F, 65950000L, 279018080L, "§1"),
                                                     new Prestige(1250.0F, 82437500L, 361455580L, "§1"),
                                                     new Prestige(1500.0F, 98925000L, 460380580L, "§1"),
                                                     new Prestige(1750.0F, 115412500L, 575793080L, "§1"),
                                                     new Prestige(2000.0F, 131900000L, 707693080L, "§1"),
                                                     new Prestige(3000.0F, 197850000L, 905543080L, "§1"),
                                                     new Prestige(5000.0F, 329750000L, 1235293080L, "§1"),
                                                     new Prestige(10000.0F, 659500000L, 1894793080L, "§1"),
                                                     new Prestige(50000.0F, 3297500000L, 5192293080L, "§1"),
                                                     new Prestige(100000.0F, 6595000000L, 11787293080L, "§1")};

        private final float multiplier;
        private final long totalXp;
        private final long sumXp;
        private final String colorCode;

        private Prestige(final float multiplier, final long totalXp, final long sumXp, final String colorCode) {
            this.multiplier = multiplier;
            this.totalXp = totalXp;
            this.sumXp = sumXp;
            this.colorCode = colorCode;
        }

        public static Prestige[] getPrestiges() {
            return PRESTIGES;
        }

        public float getMultiplier() {
            return this.multiplier;
        }

        public long getTotalXp() {
            return this.totalXp;
        }

        public long getSumXp() {
            return this.sumXp;
        }

        public String getColorCode() {
            return this.colorCode;
        }
    }
}
