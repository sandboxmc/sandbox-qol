package io.sandbox.qol;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import io.sandbox.lib.Config;
import io.sandbox.lib.SandboxLogger;
import io.sandbox.paths.effects.PathStatusEffect;
import io.sandbox.potions.Potions;
import io.sandbox.qol.configTypes.PathsConfig;
import io.sandbox.qol.configTypes.PetsConfig;
import io.sandbox.qol.configTypes.PotionsConfig;
import io.sandbox.qol.configTypes.RailsConfig;

public class Main implements ModInitializer {
  private static final SandboxLogger LOGGER = new SandboxLogger("SandboxQoL");
	private static final String BASE_CONFIG_FOLDER = "SandboxMC/QoL/";

	private static Config<RailsConfig> railsConfig = new Config<RailsConfig>(RailsConfig.class, BASE_CONFIG_FOLDER + "railsConfig.json");
	private static Config<PathsConfig> pathsConfig = new Config<PathsConfig>(PathsConfig.class, BASE_CONFIG_FOLDER + "pathsConfig.json");
	private static Config<PetsConfig> petsConfig = new Config<PetsConfig>(PetsConfig.class, BASE_CONFIG_FOLDER + "petsConfig.json");
	private static Config<PotionsConfig> potionsConfig = new Config<PotionsConfig>(PotionsConfig.class, BASE_CONFIG_FOLDER + "potionsConfig.json");

	public static final StatusEffect PATH_BOOST_EFFECT = new PathStatusEffect();

	@Override
	public void onInitialize() {
		LOGGER.info("The following improvements were enabled:");
		if (getRailsConfig().enabled) {
			LOGGER.info("Speed Rails");
		}
		if (getPathsConfig().enabled) {
			initializePaths();
			LOGGER.info("Path Boost");
		}
		if (getPetsConfig().enabled) {
			LOGGER.info("Safe Pets");
		}
		if (getPotionsConfig().enabled) {
			Potions.init();
			LOGGER.info("Stackable Potions");
		}
	}

	public static RailsConfig getRailsConfig() {
		return railsConfig.getConfig();
	}

	public static PathsConfig getPathsConfig() {
		return pathsConfig.getConfig();
	}

	public static PetsConfig getPetsConfig() {
		return petsConfig.getConfig();
	}

	public static PotionsConfig getPotionsConfig() {
		return potionsConfig.getConfig();
	}

	public static void initializePaths() {
		Registry.register(Registries.STATUS_EFFECT, new Identifier("sandbox", "path_boost"), PATH_BOOST_EFFECT.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "16976b60-675e-11ec-90d6-0242ac120003", 0.20000000298023224D, Operation.MULTIPLY_TOTAL));
	}
}
