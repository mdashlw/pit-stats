package ru.mdashlw.hypixel.pit.stats.util;

import com.mojang.authlib.GameProfile;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;

public final class ItemStackUtils {

  private ItemStackUtils() {
  }

  public static ItemStack withDisplay(final ItemStack itemStack, final String name, final List<String> lore) {
    NBTTagCompound tag = itemStack.getTagCompound();

    if (tag == null) {
      tag = new NBTTagCompound();
      itemStack.setTagCompound(tag);
    }

    if (!tag.hasKey("display", 10)) {
      tag.setTag("display", new NBTTagCompound());
    }

    final NBTTagCompound displayTag = tag.getCompoundTag("display");

    displayTag.setString("Name", name);
    displayTag.setTag("Lore", NBTUtils.asStringTagList(lore));
    return itemStack;
  }

  public static ItemStack withNoAttributeModifiers(final ItemStack itemStack) {
    NBTTagCompound tag = itemStack.getTagCompound();

    if (tag == null) {
      tag = new NBTTagCompound();
      itemStack.setTagCompound(tag);
    }

    tag.setTag("AttributeModifiers", new NBTTagList());
    return itemStack;
  }

  public static ItemStack withLazySkullOwner(final ItemStack itemStack, final String owner) {
    NBTTagCompound tag = itemStack.getTagCompound();

    if (tag == null) {
      tag = new NBTTagCompound();
      itemStack.setTagCompound(tag);
    }

    final WeakReference<NBTTagCompound> tagRef = new WeakReference<>(tag);

    ForkJoinPool.commonPool().execute(() -> {
      GameProfile gameProfile = new GameProfile(null, owner);

      gameProfile = TileEntitySkull.updateGameprofile(gameProfile);

      final NBTTagCompound t = tagRef.get();

      if (t != null) {
        t.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameProfile));
      }
    });

    return itemStack;
  }
}
