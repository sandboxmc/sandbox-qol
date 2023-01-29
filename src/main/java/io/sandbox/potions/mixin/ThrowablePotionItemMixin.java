package io.sandbox.potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.sandbox.qol.Main;
import io.sandbox.qol.configTypes.PotionsConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(ThrowablePotionItem.class)
public abstract class ThrowablePotionItemMixin extends PotionItem {
	private static final PotionsConfig CONFIG = Main.getPotionsConfig();

	private ThrowablePotionItemMixin(Settings settings) {
		super(settings);
	}

	@Inject(method="use", at=@At("RETURN"))
	private void onUse(
		World world,
		PlayerEntity user,
		Hand hand,
		CallbackInfoReturnable<TypedActionResult<ItemStack>> info
	) {
		if (CONFIG.enabled) {
			// Add a cooldown so that splash damage/health doesn't become absurdly overpowered when stackable
			user.getItemCooldownManager().set(this, CONFIG.splashPotionCooldownTicks); // 5 seconds
		}
	}
}
