package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.tileentity.EnergyPipeTileEntity;
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

public class EnergyPipeBlock extends PipeBlock {

    protected EnergyPipeBlock() {
        setRegistryName(new ResourceLocation(Main.MODID, "energy_pipe"));
    }

    @Override
    public boolean canConnectTo(IWorldReader world, BlockPos pos, Direction facing) {
        TileEntity te = world.getTileEntity(pos.offset(facing));
        return (te != null && te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).isPresent());
    }

    @Override
    public boolean isPipe(IWorldReader world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos.offset(facing));
        return state.getBlock().equals(this);
    }

    @Override
    TileEntity createTileEntity() {
        return new EnergyPipeTileEntity();
    }

    @Override
    public ActionResultType onPipeSideActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, Direction direction) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof EnergyPipeTileEntity && isExtracting(worldIn, pos, direction)) {
            if (worldIn.isRemote) {
                return ActionResultType.SUCCESS;
            }
            EnergyPipeTileEntity pipe = (EnergyPipeTileEntity) tileEntity;
            PipeContainerProvider.openGui(player, pipe, direction, (i, playerInventory, playerEntity) -> new ExtractContainer(i, playerInventory, pipe, direction));
            return ActionResultType.SUCCESS;
        }
        return super.onPipeSideActivated(state, worldIn, pos, player, handIn, hit, direction);
    }
}
