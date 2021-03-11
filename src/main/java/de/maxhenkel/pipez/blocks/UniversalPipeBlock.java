package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.tileentity.UniversalPipeTileEntity;
import de.maxhenkel.pipez.capabilities.ModCapabilities;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class UniversalPipeBlock extends PipeBlock {

    protected UniversalPipeBlock() {
        setRegistryName(new ResourceLocation(Main.MODID, "universal_pipe"));
    }

    @Override
    public boolean canConnectTo(IWorldReader world, BlockPos pos, Direction facing) {
        TileEntity te = world.getBlockEntity(pos.relative(facing));
        return te != null && (
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite()).isPresent()
                        || te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()).isPresent()
                        || te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).isPresent()
                        || te.getCapability(ModCapabilities.GAS_HANDLER_CAPABILITY, facing.getOpposite()).isPresent()
        );
    }

    @Override
    public boolean isPipe(IWorldReader world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos.relative(facing));
        return state.getBlock().equals(this);
    }

    @Override
    TileEntity createTileEntity() {
        return new UniversalPipeTileEntity();
    }

    @Override
    public ActionResultType onPipeSideActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, Direction direction) {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof UniversalPipeTileEntity && isExtracting(worldIn, pos, direction)) {
            if (worldIn.isClientSide) {
                return ActionResultType.SUCCESS;
            }
            UniversalPipeTileEntity pipe = (UniversalPipeTileEntity) tileEntity;
            PipeContainerProvider.openGui(player, pipe, direction, -1, (i, playerInventory, playerEntity) -> new ExtractContainer(i, playerInventory, pipe, direction, -1));
            return ActionResultType.SUCCESS;
        }
        return super.onPipeSideActivated(state, worldIn, pos, player, handIn, hit, direction);
    }
}
