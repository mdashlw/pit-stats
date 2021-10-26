package ru.mdashlw.hypixel.pit.stats.util;

import java.util.List;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public final class NbtUtils {

  private NbtUtils() {
  }

  public static NBTTagList asStringTagList(final List<String> elements) {
    final NBTTagList tag = new NBTTagList();

    for (final String element : elements) {
      if (element != null) {
        tag.appendTag(new NBTTagString(element));
      }
    }

    return tag;
  }
}
