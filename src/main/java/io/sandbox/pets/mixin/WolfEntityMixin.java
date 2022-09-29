package io.sandbox.pets.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.sandbox.qol.Main;
import io.sandbox.qol.configTypes.PetsConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.world.World;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends TameableEntity implements Angerable {
  private static final PetsConfig CONFIG = Main.getPetsConfig();

  public WolfEntityMixin(EntityType<? extends WolfEntity> entityType, World world) {
    super(entityType, world);
    throw new IllegalStateException("WolfEntityMixin's dummy constructor called!");
  }

  @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cbir) {
    if (!CONFIG.enabled) { return; }

    // If the wolf isn't tamed we can just leave.
    if (!this.isTamed()) { return; }

    // If it was the wolf's owner then prevent the damage.
    Entity dmgSource = source.getAttacker();
    if (dmgSource == null) { return; }

    if (this.isTeammate(dmgSource)) {
      cbir.setReturnValue(false);
      cbir.cancel();
    }
  }
}
