package ru.mdashlw.hypixel.pit.stats.asm;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Side;
import org.spongepowered.asm.mixin.Mixins;

public final class ModCore implements IFMLLoadingPlugin {

  public ModCore() {
    MixinBootstrap.init();
    Mixins.addConfiguration("mixins.pitstats.json");
    MixinEnvironment.getDefaultEnvironment().setSide(Side.CLIENT);
  }

  @Override
  public String[] getASMTransformerClass() {
    return new String[0];
  }

  @Override
  public String getModContainerClass() {
    return null;
  }

  @Override
  public String getSetupClass() {
    return null;
  }

  @Override
  public void injectData(final Map<String, Object> data) {
  }

  @Override
  public String getAccessTransformerClass() {
    return null;
  }
}
