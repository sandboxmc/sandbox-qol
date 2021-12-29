package io.sandbox.rails.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends Entity {
  private World myWorld;
  private BlockPos myPos;
  private BlockState rail;
  private BlockState underRail;
  private BlockState left;
  private BlockState right;
  private int specials = 0;

  public AbstractMinecartEntityMixin(EntityType<?> entityType, World world) {
    super(entityType, world);
    throw new IllegalStateException("AbstractMinecartEntityMixin's dummy constructor called!");
  }

  @Inject(method = "getMaxOffRailSpeed", at = @At("HEAD"), cancellable = true)
	private void getMaxOffRailSpeed(CallbackInfoReturnable<Double> cbir) {
    if (myWorld == null || myWorld.isClient()) { return; }

    if (specials == 16) {
      System.out.println("ALLOWING FAST");
      cbir.setReturnValue((this.isTouchingWater() ? 4.0D : 30.0D) / 20.0D);
    }
  }

  @Inject(method = "moveOnRail", at = @At("HEAD"), cancellable = true)
  private void moveOnRail(BlockPos pos, BlockState state, CallbackInfo cbi) {
    // Ensure we've got a constant world reference.
    if (myWorld == null) { myWorld = this.getWorld(); }

    // This is a server side only mod.
    if (myWorld.isClient()) { return; }

    System.out.println("Velocity: " + this.getVelocity().x);

    // Get the base block position and what should be the rail block we're on.
    myPos = pos;
    rail = state;

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
        addSpecialVelocity(true);
      }
      return;
    } else if (specials >= 16) {
      specials = 15; // Just make sure we don't end up incrementing past 16.
    }

    // If the powered rail we're on is NOT powered then use the normal logic.
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

    // Finally we can assume we're on a powered rail flanked by lightning rods so let's use the full speed increase during this time.
    addSpecialVelocity(false);
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

  private void addSpecialVelocity(boolean slow) {
    // double g = slow ? 1.0D : 4.0D;
    switch (this.getMovementDirection().getName()) {
      case "north":
        ++specials;
        if (specials == 16) {
          System.out.println("RAILGUNNNNNN!@!!!!!!");
          // this.setVelocity(0.0D, 0.0D, -g);
        }
        break;
      case "south":
        ++specials;
        if (specials == 16) {
          System.out.println("RAILGUNNNNNN!@!!!!!!");
          // this.setVelocity(0.0D, 0.0D, g);
        }
        break;
      case "west":
        ++specials;
        if (specials == 16) {
          System.out.println("RAILGUNNNNNN!@!!!!!!");
          // this.setVelocity(-g, 0.0D, 0.0D);
        }
        break;
      case "east":
        ++specials;
        if (specials == 16) {
          System.out.println("RAILGUNNNNNN!@!!!!!!");
          // this.setVelocity(g, 0.0D, 0.0D);
        }
        break;
      default:
        // Do nothing
    }
  }
}
