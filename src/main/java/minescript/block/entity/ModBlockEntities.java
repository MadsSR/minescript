package minescript.block.entity;

import minescript.MineScript;
import minescript.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static BlockEntityType<TurtleBlockEntity> TURTLE_BLOCK_ENTITY;

    public static void registerBlockEntities() {
        TURTLE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MineScript.MOD_ID, "turtle_block"),
                FabricBlockEntityTypeBuilder.create(TurtleBlockEntity::new,
                        ModBlocks.TURTLE_BLOCK).build(null));
    }
}
