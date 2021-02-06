package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.PipeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPipeTileEntity extends UpgradeTileEntity {

    public ItemPipeTileEntity() {
        super(ModTileEntities.ITEM_PIPE);
    }

    @Override
    public boolean canInsert(TileEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent();
    }

    @Override
    public void tick() {
        super.tick();

        if (world.getGameTime() % getSpeed() != 0) {
            return;
        }

        BlockState blockState = getBlockState();
        Block block = blockState.getBlock();
        if (!(block instanceof PipeBlock)) {
            return;
        }
        PipeBlock pipe = (PipeBlock) block;

        for (Direction direction : Direction.values()) {
            if (!pipe.isExtracting(blockState, direction)) {
                continue;
            }
            IItemHandler itemHandler = getItemHandler(pos.offset(direction), direction.getOpposite());
            if (itemHandler == null) {
                continue;
            }

            List<Connection> connections = getConnections().stream().sorted(Comparator.comparingInt(Connection::getDistance)).collect(Collectors.toList());

            int itemsToTransfer = getAmount();

            connectionLoop:
            for (Connection connection : connections) {
                IItemHandler destination = getItemHandler(connection.getPos(), connection.getDirection());
                if (destination == null) {
                    continue;
                }

                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    if (itemsToTransfer <= 0) {
                        break connectionLoop;
                    }
                    ItemStack simulatedExtract = itemHandler.extractItem(i, itemsToTransfer, true);

                    ItemStack stack = ItemHandlerHelper.insertItem(destination, simulatedExtract, false);
                    int insertedAmount = simulatedExtract.getCount() - stack.getCount();
                    itemsToTransfer -= insertedAmount;
                    itemHandler.extractItem(i, insertedAmount, false);
                }
            }
        }
    }

    @Nullable
    private IItemHandler getItemHandler(BlockPos pos, Direction direction) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return null;
        }
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(null);
    }

    public int getSpeed() {
        return 20;
    }

    public int getAmount() {
        return 8;
    }


}
