package interpreter.types;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class MSBlock extends MSType {
    private final Block block;

    public MSBlock(String id) {
        super(MSTypeEnum.MSBlock);
        block = Registries.BLOCK.get(new Identifier(id.toLowerCase().replace("minecraft:", "")));
        if (block == Blocks.AIR && !id.equals("minecraft:air")) {
            throw new IllegalArgumentException("Block " + id + " does not exist");
        }
    }

    public MSBlock(Block block) {
        super(MSTypeEnum.MSBlock);
        this.block = block;
    }

    @Override
    public String toString() {
        return block.toString().replace("Block{", "").replace("}", "");
    }

    public Block getValue() {
        return block;
    }

    @Override
    public boolean equals(MSType value) {
        if (value instanceof MSBlock b) {
            return this.block == b.block;
        }
        return false;
    }
    
}
