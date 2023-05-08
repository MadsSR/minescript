package minescript;

import io.wispforest.owo.ui.parsing.UIParsing;
import minescript.block.ModBlocks;
import minescript.block.ModItemGroup;
import minescript.block.entity.ModBlockEntities;
import minescript.networking.MineScriptPackets;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineScript implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("minescript");
	public static final String MOD_ID = "minescript";

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		ModItemGroup.registerItemGroups();
		ModBlockEntities.registerBlockEntities();
		ModBlocks.registerModBlocks();
		MineScriptPackets.registerC2SPackets();
	}
}