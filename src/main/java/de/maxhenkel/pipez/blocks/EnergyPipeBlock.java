package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.blocks.tileentity.EnergyPipeTileEntity;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;

public class EnergyPipeBlock extends PipeBlock {

    protected EnergyPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canConnectTo(Level world, BlockPos pos, Direction facing) {
        return world.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(facing), facing.getOpposite()) != null;
    }

    @Override
    public boolean isPipe(LevelAccessor world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos.relative(facing));
        return state.getBlock().equals(this);
    }

    @Override
    BlockEntity createTileEntity(BlockPos pos, BlockState state) {
        return new EnergyPipeTileEntity(pos, state);
    }

    @Override
    public InteractionResult onPipeSideActivated(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, Direction direction) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof EnergyPipeTileEntity && isExtracting(worldIn, pos, direction)) {
            if (worldIn.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            EnergyPipeTileEntity pipe = (EnergyPipeTileEntity) tileEntity;
            PipeContainerProvider.openGui(player, pipe, direction, -1, (i, playerInventory, playerEntity) -> new ExtractContainer(i, playerInventory, pipe, direction, -1));
            return InteractionResult.SUCCESS;
        }
        return super.onPipeSideActivated(stack, state, worldIn, pos, player, handIn, hit, direction);
    }
}
