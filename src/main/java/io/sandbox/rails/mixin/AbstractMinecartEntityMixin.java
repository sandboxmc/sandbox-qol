package io.sandbox.rails.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.sandbox.lib.BlockHelper;
import io.sandbox.lib.SandboxLogger;
import io.sandbox.qol.Main;
import io.sandbox.qol.config_types.RailsConfig;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends Entity {
  private static final RailsConfig CONFIG = Main.getRailsConfig();
  private static final SandboxLogger LOGGER = new SandboxLogger("SandboxSpeedRails");
  private boolean configLoaded = false;
  private World myWorld;
  private BlockPos myPos;
  private BlockPos lastPos;
  private int specials = 0;
  private double[] specialVelocities;
  private Block leftBlockType;
  private Block rightBlockType;
  private Block bottomBlockType;

  // // This code is just for tracking BPS and velocity. It's currently written to only track along the east/west axis.
  // // This is strictly for testing purposes and should never be used in a real world environment.
  // private int ticks = 0;
  // private BlockPos startPos;
  // @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  // private void tick(CallbackInfo cbi) {
  //   if (myWorld == null || myWorld.isClient()) { return; }
  //   ticks++;
  //   if (ticks == 1) {
  //     startPos = this.getBlockPos();
  //   } else if (ticks == 20) {
  //     ticks = 0;
  //     if (myPos != null) {
  //       Vec3d vel =  this.getVelocity();
  //       if (Math.abs(vel.x) > Math.abs(vel.z)) {
  //         System.out.println(
  //           "SP: " + specials + 
  //           " | POS: " + myPos.toShortString() +
  //           " | BPS: " + (myPos.getX() - startPos.getX()) + 
  //           " | VEL: " + vel
  //         );
  //       } else {
  //         System.out.println(
  //           "SP: " + specials + 
  //           " | POS: " + myPos.toShortString() +
  //           " | BPS: " + (myPos.getZ() - startPos.getZ()) + 
  //           " | VEL: " + vel
  //         );
  //       }
  //       startPos = myPos;
  //     }
  //   }
  // }

  public AbstractMinecartEntityMixin(EntityType<?> entityType, World world) {
    super(entityType, world);
    throw new IllegalStateException("AbstractMinecartEntityMixin's dummy constructor called!");
  }

  @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
	private void getMaxSpeed(CallbackInfoReturnable<Double> cbir) {
    if (!CONFIG.enabled) { return; }
    
    if (!configLoaded) {
      LOGGER.info("Running first time setup...");
      myWorld = this.getWorld();
      leftBlockType = BlockHelper.getBlockFromId(CONFIG.leftBlock);
      rightBlockType = BlockHelper.getBlockFromId(CONFIG.rightBlock);
      bottomBlockType = BlockHelper.getBlockFromId(CONFIG.bottomBlock);
      specialVelocities = new double[CONFIG.requiredLength - 1]; // last velocity does something special
      double mod = (0.5D / specialVelocities.length);
      for (int i = 0; i < specialVelocities.length; i++) {
        specialVelocities[i] = 0.5D + (mod * i);
      }
      configLoaded = true;
      LOGGER.info("Finished!");
    }

    // This is a server side only mod.
    if (myWorld.isClient()) { return; }

    updateReturnVal(cbir, false);

    // Update our current block position.
    myPos = this.getBlockPos();

    // If the current position is the same as the LAST position then we already did this and we can leave.
    if (myPos.equals(lastPos)) { return; }

    // Update last position to current.
    lastPos = myPos;

    // Get the block state for the rail we're assumed to be on.
    BlockState rail = myWorld.getBlockState(myPos);

    // If we're touching water or we're not on a rail then reset specials to 0 and use the default logic.
    if (this.isTouchingWater() || !(rail.getBlock() instanceof AbstractRailBlock)) {
      specials = 0;
      return;
    }

    // If we are then on any rail other than a powered rail let's continue with whatever specials we had and the default logic.
    if (!rail.isOf(Blocks.POWERED_RAIL)) { return; }

    // If the powered rails were not powered or are not surrounded by the appriopriate blocks then reset specials to 0 and use the default logic.
    // This means that a regular powered rail or an unpowered powered rail will bring the velocity back to normal.
    // This is useful for corners and such.
    if (!(Boolean)rail.get(PoweredRailBlock.POWERED) || hasInvalidBlocks()) {
      specials = 0;
      return;
    }

    // Finally we can assume we're on a powered rail with the proper positional blocks.
    // We want to trigger specials to increase.
    specials++;
    updateReturnVal(cbir, true);
  }

  private boolean hasInvalidBlocks() {
    // This is always the same.
    if (bottomBlockType != null && !myWorld.getBlockState(myPos.down()).isOf(bottomBlockType)) {
      return true;
    }

    // Left and Right depend on cardinal direction.
    BlockState left;
    BlockState right;
    switch (this.getMovementDirection().getName()) {
      case "north":
        left = myWorld.getBlockState(myPos.west());
        right = myWorld.getBlockState(myPos.east());
        break;
      case "south":
        left = myWorld.getBlockState(myPos.east());
        right = myWorld.getBlockState(myPos.west());
        break;
      case "east":
        left = myWorld.getBlockState(myPos.north());
        right = myWorld.getBlockState(myPos.south());
        break;
      case "west":
        left = myWorld.getBlockState(myPos.south());
        right = myWorld.getBlockState(myPos.north());
        break;
      default:
        // Not sure how this would exactly happen but let's just verify if we had a setting or not and return based on that.
        return leftBlockType != null || rightBlockType != null;
    }

    if (leftBlockType != null && !left.isOf(leftBlockType)) {
      return true;
    }

    if (rightBlockType != null && !right.isOf(rightBlockType)) {
      return true;
    }

    return false;
  }

  private void updateReturnVal(CallbackInfoReturnable<Double> cbir, boolean boost) {
    if (specials >= CONFIG.requiredLength) {
      // Set to the maximum of 1.5 which forces velocity of 1.944 (max allowed by game) without deceleration.
      cbir.setReturnValue(1.5D);
      if (boost) {
        Vec3d vel = this.getVelocity();
        this.setVelocity(vel.add(vel.x, 0, vel.z)); // just double the current velocity
      }
    } else if (specials > 0) {
      cbir.setReturnValue(specialVelocities[specials - 1]);
    }
  }
}
