package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.blocks.tileentity.UniversalPipeTileEntity;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import de.maxhenkel.pipez.utils.GasUtils;
import de.maxhenkel.pipez.utils.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;

public class UniversalPipeBlock extends PipeBlock {

    protected UniversalPipeBlock() {
    }

    @Override
    public boolean canConnectTo(Level world, BlockPos pos, Direction facing) {
        return world.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(facing), facing.getOpposite()) != null
                || world.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(facing), facing.getOpposite()) != null
                || world.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(facing), facing.getOpposite()) != null
                || (MekanismUtils.isMekanismInstalled() && GasUtils.hasChemicalCapability(world, pos.relative(facing), facing.getOpposite()));
    }

    @Override
    public boolean isPipe(LevelAccessor world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos.relative(facing));
        return state.getBlock().equals(this);
    }

    @Override
    BlockEntity createTileEntity(BlockPos pos, BlockState state) {
        return new UniversalPipeTileEntity(pos, state);
    }

    @Override
    public ItemInteractionResult onPipeSideActivated(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, Direction direction) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof UniversalPipeTileEntity && isExtracting(worldIn, pos, direction)) {
            if (worldIn.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            UniversalPipeTileEntity pipe = (UniversalPipeTileEntity) tileEntity;
            PipeContainerProvider.openGui(player, pipe, direction, -1, (i, playerInventory, playerEntity) -> new ExtractContainer(i, playerInventory, pipe, direction, -1));
            return ItemInteractionResult.SUCCESS;
        }
        return super.onPipeSideActivated(stack, state, worldIn, pos, player, handIn, hit, direction);
    }
}
