package io.sandbox.rails.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends Entity {
  private static double maxSafeVel = 1.0D;
  private World myWorld;
  private BlockPos myPos;
  private BlockPos lastPos;
  private BlockState rail;
  private BlockState underRail;
  private BlockState left;
  private BlockState right;
  private int specials = 0;

  // // This code is just for tracking BPS and velocity. It's currently written to only track along the east/west axis.
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

  @Inject(method = "getMaxOffRailSpeed", at = @At("HEAD"), cancellable = true)
	private void getMaxOffRailSpeed(CallbackInfoReturnable<Double> cbir) {
    // Ensure we've got a constant world reference.
    if (myWorld == null) {
      myWorld = this.getWorld();
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
    rail = myWorld.getBlockState(myPos);

    // If we're not on a rail then use the normal logic.
    if (!(rail.getBlock() instanceof AbstractRailBlock)) {
      specials = 0;
      return;
    }

    // If we're not on a powered rail then use the normal logic.
    if (!rail.isOf(Blocks.POWERED_RAIL)) {
      // If we're on a powered rail that is on soulsand let's slow us down.
      underRail = myWorld.getBlockState(myPos.down());
      if (underRail.isOf(Blocks.SOUL_SAND) || underRail.isOf(Blocks.SOUL_SOIL)) {
        specials = 0;

        // Force the current velocity down to base.
        Vec3d vel = this.getVelocity();
        if (vel.x > maxSafeVel) {
          this.setVelocity(maxSafeVel, vel.y, vel.z);
        }
        if (vel.x < -maxSafeVel) {
          this.setVelocity(-maxSafeVel, vel.y, vel.z);
        }
        if (vel.z > maxSafeVel) {
          this.setVelocity(vel.x, vel.y, maxSafeVel);
        }
        if (vel.z < -maxSafeVel) {
          this.setVelocity(vel.x, vel.y, -maxSafeVel);
        }
      }
      return;
    }

    // If the powered rail we're on is NOT powered then reset specials to 0.
    if (!(Boolean)rail.get(PoweredRailBlock.POWERED)) {
      specials = 0;
      return;
    }

    // Set up our left/right blocks based on our direction.
    setLeftAndRightBlocks();

    // If we're on a powered rail but it is NOT flanked by lightning rods then use the normal logic.
    if (!left.isOf(Blocks.LIGHTNING_ROD) || !right.isOf(Blocks.LIGHTNING_ROD)) {
      specials = 0;
      return;
    }

    // Finally we can assume we're on a powered rail flanked by lightning rods.
    // We want to trigger specials to increase.
    specials++;
    updateReturnVal(cbir, true);
  }

  private void setLeftAndRightBlocks() {
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
        // I have no idea what this would really be, but let's set them to null and leave.
        left = null;
        right = null;
    }
  }

  private void updateReturnVal(CallbackInfoReturnable<Double> cbir, boolean boost) {
    if (specials > 0 && !this.isTouchingWater()) {
      switch (specials) {
        case 1:
          cbir.setReturnValue(0.5D);
          break;
        case 2:
          cbir.setReturnValue(0.6D);
          break;
        case 3:
          cbir.setReturnValue(0.7D);
          break;
        case 4:
          cbir.setReturnValue(0.8D);
          break;
        case 5:
          cbir.setReturnValue(0.9D);
          break;
        case 6:
          cbir.setReturnValue(0.9D);
          break;
        case 7:
          cbir.setReturnValue(1.0D);
          break;
        default:
          // Set to the maximum of 1.5 which forces velocity of 1.944 (max allowed by game) without deceleration.
          cbir.setReturnValue(1.5D);
          if (boost) {
            Vec3d vel = this.getVelocity();
            this.setVelocity(vel.add(vel.x, 0, vel.z)); // just double the current velocity
          }
      }
    }
  }
}
