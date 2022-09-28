package io.sandbox.qol;

import net.fabricmc.api.ModInitializer;

import io.sandbox.lib.Config;
import io.sandbox.lib.SandboxLogger;
import io.sandbox.qol.config_types.RailsConfig;

public class Main implements ModInitializer {
  private static final SandboxLogger LOGGER = new SandboxLogger("SandboxQoL");
	private static final String BASE_CONFIG_FOLDER = "SandboxMC/QoL/";

	private static Config<RailsConfig> railsConfig;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing...");

		railsConfig = new Config<RailsConfig>(RailsConfig.class, BASE_CONFIG_FOLDER + "railsConfig.json");

		LOGGER.info("The following improvements were enabled:");
		if (getRailsConfig().enabled) {
			LOGGER.info("Speed Rails");
		}
	}

	public static RailsConfig getRailsConfig() {
		return railsConfig.getConfig();
	}
}
