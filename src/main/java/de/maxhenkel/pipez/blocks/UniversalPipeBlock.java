package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.tileentity.UniversalPipeTileEntity;
import de.maxhenkel.pipez.capabilities.CapabilityCache;
import de.maxhenkel.pipez.capabilities.ModCapabilities;
import de.maxhenkel.pipez.events.ServerTickEvents;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class UniversalPipeBlock extends PipeBlock {

    protected UniversalPipeBlock() {
        setRegistryName(new ResourceLocation(Main.MODID, "universal_pipe"));
    }

    @Override
    public boolean canConnectTo(LevelAccessor world, BlockPos pos, Direction facing) {
        Level level = (Level) world;
        if (level == null) {
            return false;
        }
        CapabilityCache capCache = CapabilityCache.getInstance();
        BlockPos targetPos = pos.relative(facing);
        Direction targetDirection = facing.getOpposite();

        var capability = false;
        capability |= capCache.getItemCapability(level, targetPos, targetDirection).isPresent();
        capability |= capCache.getFluidCapability(level, targetPos, targetDirection).isPresent();
        capability |= capCache.getEnergyCapability(level, targetPos, targetDirection).isPresent();
        capability |= capCache.getGasCapability(level, targetPos, targetDirection).isPresent();
        return capability;
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
    public InteractionResult onPipeSideActivated(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, Direction direction) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof UniversalPipeTileEntity && isExtracting(worldIn, pos, direction)) {
            if (worldIn.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            UniversalPipeTileEntity pipe = (UniversalPipeTileEntity) tileEntity;
            PipeContainerProvider.openGui(player, pipe, direction, -1, (i, playerInventory, playerEntity) -> new ExtractContainer(i, playerInventory, pipe, direction, -1));
            return InteractionResult.SUCCESS;
        }
        return super.onPipeSideActivated(state, worldIn, pos, player, handIn, hit, direction);
    }
}
