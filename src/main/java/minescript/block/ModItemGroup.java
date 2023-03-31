package java.minescript.block;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static ItemGroup MINESCRIPT_GROUP;

    public static void registerItemGroups() {
        MINESCRIPT_GROUP = FabricItemGroup.builder(new Identifier("java/minescript", "minescript_group"))
                .displayName(Text.literal("MineScript group"))
                .icon(() -> new ItemStack(Items.DIAMOND_HOE))
                .entries((enabledFeatures, entries) -> {
                    entries.add(Items.DIAMOND);
                    entries.add(ModBlocks.TURTLE_BLOCK);
                })
                .build();
    }
}
