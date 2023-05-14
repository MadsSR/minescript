package minescript;

import minescript.block.ModBlocks;
import minescript.block.ModItemGroup;
import minescript.block.entity.ModBlockEntities;
import minescript.network.MineScriptPackets;
import minescript.screen.TextEditorScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineScript implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("minescript");
    public static final String MOD_ID = "minescript";
    public static ScreenHandlerType<TextEditorScreenHandler> TEXT_EDITOR_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(TextEditorScreenHandler::new);

    static {
        TEXT_EDITOR_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "text_editor_screen_handler"), TEXT_EDITOR_SCREEN_HANDLER);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Registering MineScript");
        ModItemGroup.registerItemGroups();
        ModBlockEntities.registerBlockEntities();
        ModBlocks.registerModBlocks();
        MineScriptPackets.registerC2SPackets();
    }
}