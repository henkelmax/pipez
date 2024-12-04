package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.Main;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    private static final DeferredRegister.Blocks BLOCK_REGISTER = DeferredRegister.createBlocks(Main.MODID);

    public static final DeferredHolder<Block, ItemPipeBlock> ITEM_PIPE = BLOCK_REGISTER.registerBlock("item_pipe", ItemPipeBlock::new, BlockBehaviour.Properties.of());
    public static final DeferredHolder<Block, FluidPipeBlock> FLUID_PIPE = BLOCK_REGISTER.registerBlock("fluid_pipe", FluidPipeBlock::new, BlockBehaviour.Properties.of());
    public static final DeferredHolder<Block, EnergyPipeBlock> ENERGY_PIPE = BLOCK_REGISTER.registerBlock("energy_pipe", EnergyPipeBlock::new, BlockBehaviour.Properties.of());
    public static final DeferredHolder<Block, UniversalPipeBlock> UNIVERSAL_PIPE = BLOCK_REGISTER.registerBlock("universal_pipe", UniversalPipeBlock::new, BlockBehaviour.Properties.of());
    public static final DeferredHolder<Block, GasPipeBlock> GAS_PIPE = BLOCK_REGISTER.registerBlock("gas_pipe", GasPipeBlock::new, BlockBehaviour.Properties.of());

    public static void init(IEventBus eventBus) {
        BLOCK_REGISTER.register(eventBus);
    }

}
