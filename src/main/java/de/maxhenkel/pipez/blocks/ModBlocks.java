package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.Main;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK, Main.MODID);

    public static final DeferredHolder<Block, ItemPipeBlock> ITEM_PIPE = BLOCK_REGISTER.register("item_pipe", ItemPipeBlock::new);
    public static final DeferredHolder<Block, FluidPipeBlock> FLUID_PIPE = BLOCK_REGISTER.register("fluid_pipe", FluidPipeBlock::new);
    public static final DeferredHolder<Block, EnergyPipeBlock> ENERGY_PIPE = BLOCK_REGISTER.register("energy_pipe", EnergyPipeBlock::new);
    public static final DeferredHolder<Block, UniversalPipeBlock> UNIVERSAL_PIPE = BLOCK_REGISTER.register("universal_pipe", UniversalPipeBlock::new);
    public static final DeferredHolder<Block, GasPipeBlock> GAS_PIPE = BLOCK_REGISTER.register("gas_pipe", GasPipeBlock::new);

    public static void init(IEventBus eventBus) {
        BLOCK_REGISTER.register(eventBus);
    }

}
