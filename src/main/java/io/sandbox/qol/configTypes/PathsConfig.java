package io.sandbox.qol.configTypes;

public class PathsConfig {
  public boolean enabled;
  public String block;
  public int durationInTicks;

  public PathsConfig() {
    enabled = true;
    block = "minecraft:dirt_path";
    durationInTicks = 60;
  }
}
