package io.sandbox.qol.configTypes;

public class RailsConfig {
  // Available settings
  public Boolean enabled;
  public String leftBlock;
  public String rightBlock;
  public String bottomBlock;
  public int requiredLength;

  // Defaults
  public RailsConfig() {
    enabled = true;
    leftBlock = "minecraft:lightning_rod";
    rightBlock = "minecraft:lightning_rod";
    bottomBlock = null;
    requiredLength = 8;
  }
}
