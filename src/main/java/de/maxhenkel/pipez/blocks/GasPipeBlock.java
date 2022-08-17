package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.tileentity.GasPipeTileEntity;
import de.maxhenkel.pipez.capabilities.CapabilityCacheOld;
import de.maxhenkel.pipez.capabilities.ModCapabilities;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.ModList;

public class GasPipeBlock extends PipeBlock {

    protected GasPipeBlock() {
        setRegistryName(new ResourceLocation(Main.MODID, "gas_pipe"));
    }

    @Override
    public boolean canConnectTo(LevelAccessor world, BlockPos pos, Direction facing) {
        Level level = (Level) world;
        return (level != null && CapabilityCacheOld.getInstance().getGasCapability(
                level, pos.relative(facing), facing.getOpposite()).isPresent());
    }

    @Override
    public boolean isPipe(LevelAccessor world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos.relative(facing));
        return state.getBlock().equals(this);
    }

    @Override
    BlockEntity createTileEntity(BlockPos pos, BlockState state) {
        if (!ModList.get().isLoaded("mekanism")) {
            return null;
        }
        return new GasPipeTileEntity(pos, state);
    }

    @Override
    public InteractionResult onPipeSideActivated(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, Direction direction) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof GasPipeTileEntity && isExtracting(worldIn, pos, direction)) {
            if (worldIn.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            GasPipeTileEntity pipe = (GasPipeTileEntity) tileEntity;
            PipeContainerProvider.openGui(player, pipe, direction, -1, (i, playerInventory, playerEntity) -> new ExtractContainer(i, playerInventory, pipe, direction, -1));
            return InteractionResult.SUCCESS;
        }
        return super.onPipeSideActivated(state, worldIn, pos, player, handIn, hit, direction);
    }
}
