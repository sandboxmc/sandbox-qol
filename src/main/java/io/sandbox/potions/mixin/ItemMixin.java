package io.sandbox.potions.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import io.sandbox.potions.IConfigCount;
import net.minecraft.item.Item;

@Mixin(Item.class)
public class ItemMixin implements IConfigCount {
  @Final
	@Mutable
	@Shadow
	private int maxCount;

	public void setMaxCount(int i) {
    this.maxCount = i;
	}
}
