package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.Main;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);

    public static final RegistryObject<ItemPipeBlock> ITEM_PIPE = BLOCK_REGISTER.register("item_pipe", ItemPipeBlock::new);
    public static final RegistryObject<FluidPipeBlock> FLUID_PIPE = BLOCK_REGISTER.register("fluid_pipe", FluidPipeBlock::new);
    public static final RegistryObject<EnergyPipeBlock> ENERGY_PIPE = BLOCK_REGISTER.register("energy_pipe", EnergyPipeBlock::new);
    public static final RegistryObject<UniversalPipeBlock> UNIVERSAL_PIPE = BLOCK_REGISTER.register("universal_pipe", UniversalPipeBlock::new);
    public static final RegistryObject<GasPipeBlock> GAS_PIPE = BLOCK_REGISTER.register("gas_pipe", GasPipeBlock::new);

    public static void init() {
        BLOCK_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
