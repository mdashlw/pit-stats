package ru.mdashlw.hypixel.pit.stats.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import ru.mdashlw.hypixel.api.util.NumberUtils;

public final class DurationUtils {

  private DurationUtils() {
  }

  public static String format(final Duration duration) {
    final List<String> output = new ArrayList<>();
    final long days = duration.toDays();
    final long hours = duration.toHours() % 24;
    final long minutes = duration.toMinutes() % 60;
    final long seconds = duration.getSeconds() % 60;

    if (days != 0) {
      output.add(NumberUtils.plural(days, "day"));
    }

    if (hours != 0) {
      output.add(NumberUtils.plural(hours, "hour"));
    }

    if (minutes != 0) {
      output.add(NumberUtils.plural(minutes, "minute"));
    }

    if (seconds != 0 && output.size() < 3) {
      output.add(NumberUtils.plural(seconds, "second"));
    }

    if (output.isEmpty()) {
      output.add("now");
    }

    return String.join(" ", output);
  }
}
