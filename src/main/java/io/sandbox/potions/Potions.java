package io.sandbox.potions;

import io.sandbox.qol.Main;
import io.sandbox.qol.configTypes.PotionsConfig;
import net.minecraft.item.Items;

public class Potions {
  private static final PotionsConfig CONFIG = Main.getPotionsConfig();
  public static void init() {
    // Enabled has already been checked so no need to check here
    ((IConfigCount)(Items.POTION)).setMaxCount(CONFIG.potionStackSize);
    ((IConfigCount)(Items.SPLASH_POTION)).setMaxCount(CONFIG.splashPotionStackSize);
    ((IConfigCount)(Items.LINGERING_POTION)).setMaxCount(CONFIG.lingeringPotionStackSize);
  }
}
