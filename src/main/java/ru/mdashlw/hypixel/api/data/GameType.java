package ru.mdashlw.hypixel.api.data;

public enum GameType {
  UNKNOWN("§cUnknown"),
  MAIN_LOBBY("Main Lobby"),
  MAIN("Main Lobby"),
  TOURNAMENT_LOBBY("Tournament Hall"),
  TOURNAMENT("Tournament Hall"),
  LIMBO("Limbo"),
  IDLE("AFK"),
  QUEUE("Queue"),
  QUAKECRAFT("Quakecraft"),
  WALLS("The Walls"),
  PAINTBALL("Paintball Warfare"),
  SURVIVAL_GAMES("Blitz SG"),
  TNTGAMES("The TNT Games"),
  VAMPIREZ("VampireZ"),
  WALLS3("Mega Walls"),
  ARCADE("Arcade Games"),
  ARENA("Arena Brawl"),
  UHC("UHC Champions"),
  MCGO("Cops and Crims"),
  BATTLEGROUND("Warlords"),
  SUPER_SMASH("Smash Heroes"),
  GINGERBREAD("Turbo Kart Racers"),
  HOUSING("Housing"),
  SKYWARS("SkyWars"),
  TRUE_COMBAT("Crazy Walls"),
  SPEED_UHC("Speed UHC"),
  SKYCLASH("SkyClash"),
  LEGACY("Classic Games"),
  PROTOTYPE("Prototype"),
  BEDWARS("Bed Wars"),
  MURDER_MYSTERY("Murder Mystery"),
  BUILD_BATTLE("Build Battle"),
  DUELS("Duels"),
  SKYBLOCK("SkyBlock"),
  PIT("The Hypixel Pit"),
  REPLAY("Replay");

  private final String displayName;

  GameType(final String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return this.displayName;
  }
}
