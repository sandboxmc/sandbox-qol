package io.sandbox.qol;

import net.fabricmc.api.ModInitializer;

import io.sandbox.lib.Config;

public class Main implements ModInitializer {
	@Override
	public void onInitialize() {
		System.out.println("Initializing Sandbox Speed Rails");
		Config<Object> a = new Config<Object>(Object.class, "my-config.json");
	}
}
