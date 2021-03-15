package ru.mdashlw.hypixel.pit.stats.asm.mixins;

import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ru.mdashlw.hypixel.pit.stats.HypixelPitStats;

@Mixin(ItemStack.class)
public final class MixinItemStack {

  private static final List<String> COLORS = Arrays.asList("§cRed", "§eYellow", "§9Blue", "§6Orange", "§aGreen");

  @Shadow
  private Item item;
  @Shadow
  private NBTTagCompound stackTagCompound;

  @Inject(
      method = "getTooltip",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/item/ItemStack;getAttributeModifiers()Lcom/google/common/collect/Multimap;"
      ),
      locals = LocalCapture.CAPTURE_FAILSOFT
  )
  public void getTooltip(final EntityPlayer player, final boolean advanced,
      final CallbackInfoReturnable<List<String>> cir,
      final List<String> tooltip, final String displayName, final int hideFlags) {
    if (!HypixelPitStats.getInstance().getSettings().isDisplayRequiredPants()) {
      return;
    }

    final Item item = this.item;

    if (item != null && (item == Items.golden_sword || item == Items.bow)) {
      final NBTTagCompound nbt = this.stackTagCompound;

      if (nbt != null && nbt.hasKey("ExtraAttributes", 10)) {
        final NBTTagCompound extraAttributes = (NBTTagCompound) nbt.getTag("ExtraAttributes");
        final int nonce = extraAttributes.getInteger("Nonce");

        if (nonce != 0) {
          final int tier = extraAttributes.getInteger("UpgradeTier");

          if (tier != 3) {
            final String color = MixinItemStack.COLORS.get(nonce % 5);

            if (!tooltip.isEmpty()) {
              if (tooltip.get(tooltip.size() - 1).endsWith("Attack Damage")) {
                tooltip.add(tooltip.size() - 2, "");
                tooltip.add(tooltip.size() - 2, "§7Requires " + color + " Pants §7to §cTier III");
              } else {
                tooltip.add("");
                tooltip.add("§7Requires " + color + " Pants §7to §cTier III");
              }
            }
          }
        }
      }
    }
  }
}
