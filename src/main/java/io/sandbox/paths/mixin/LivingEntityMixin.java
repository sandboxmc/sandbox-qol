package io.sandbox.paths.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.sandbox.lib.BlockHelper;
import io.sandbox.lib.SandboxLogger;
import io.sandbox.qol.Main;
import io.sandbox.qol.configTypes.PathsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class LivingEntityMixin extends LivingEntity {
  private static final PathsConfig CONFIG = Main.getPathsConfig();
  private static final SandboxLogger LOGGER = new SandboxLogger("SandboxPathBoost");
  private boolean configLoaded = false;
  private Block blockType;

  protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
    super(entityType, world);
    throw new IllegalStateException("LivingEntityMixin's dummy constructor called!");
  }

  @Inject(at = @At("HEAD"), method = "tickMovement", cancellable = true)
  public void tickMovement(CallbackInfo cb) {
    if (!CONFIG.enabled) { return; }
    
    if (!configLoaded) {
      LOGGER.info("Running first time setup...");
      blockType = BlockHelper.getBlockFromId(CONFIG.block);
      configLoaded = true;
      LOGGER.info("Finished!");
    }

    Vec3d pos = this.getPos();
    BlockPos blockPos = new BlockPos(Math.floor(pos.x), Math.floor(pos.y), Math.floor(pos.z));
    BlockState blockState = this.hasVehicle() ?
      this.world.getBlockState(blockPos.down()) :
      this.world.getBlockState(blockPos);

    if (!blockState.isAir() && blockState.isOf(blockType)) {
      int boost = 0;
      if (this.hasVehicle()) {
        boost = 1;
      }

      this.addStatusEffect(new StatusEffectInstance(Main.PATH_BOOST_EFFECT, CONFIG.durationInTicks, boost));
    }
  }
}
