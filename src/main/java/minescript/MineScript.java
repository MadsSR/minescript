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
    public static final Logger LOGGER = LoggerFactory.getLogger("minescript");
    public static final String MOD_ID = "minescript";

    @Override
    public void onInitialize() {
        ModItemGroup.registerItemGroups();
        ModBlockEntities.registerBlockEntities();
        ModBlocks.registerModBlocks();
        MineScriptPackets.registerC2SPackets();
    }
}