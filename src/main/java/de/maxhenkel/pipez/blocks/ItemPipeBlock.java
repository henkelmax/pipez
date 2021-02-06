package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.tileentity.ItemPipeTileEntity;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class ItemPipeBlock extends PipeBlock {

    protected ItemPipeBlock() {
        setRegistryName(new ResourceLocation(Main.MODID, "item_pipe"));
    }

    @Override
    public boolean canConnectTo(IWorldReader world, BlockPos pos, Direction facing) {
        TileEntity te = world.getTileEntity(pos.offset(facing));
        return (te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite()).isPresent());
    }

    @Override
    public boolean isPipe(IWorldReader world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos.offset(facing));
        return state.getBlock().equals(this);
    }

    @Override
    public ActionResultType onPipeSideActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, Direction direction) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof ItemPipeTileEntity && isExtracting(state, direction)) {
            if (worldIn.isRemote) {
                return ActionResultType.SUCCESS;
            }
            ItemPipeTileEntity pipe = (ItemPipeTileEntity) tileEntity;
            PipeContainerProvider.openGui(player, pipe, direction, (i, playerInventory, playerEntity) -> new ExtractContainer(i, playerInventory, pipe, direction));
            return ActionResultType.SUCCESS;
        }
        return super.onPipeSideActivated(state, worldIn, pos, player, handIn, hit, direction);
    }
}
