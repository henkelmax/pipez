package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.Upgrade;
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
    public Distribution getDefaultDistribution() {
        return Distribution.NEAREST;
    }

    @Override
    public RedstoneMode getDefaultRedstoneMode() {
        return RedstoneMode.IGNORED;
    }

    @Override
    public FilterMode getDefaultFilterMode() {
        return FilterMode.WHITELIST;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isRemote) {
            return;
        }

        for (Direction direction : Direction.values()) {
            if (world.getGameTime() % getSpeed(direction) != 0) {
                continue;
            }

            if (!isExtracting(direction)) {
                continue;
            }
            IItemHandler itemHandler = getItemHandler(pos.offset(direction), direction.getOpposite());
            if (itemHandler == null) {
                continue;
            }

            List<Connection> connections = getConnections().stream().sorted(Comparator.comparingInt(Connection::getDistance)).collect(Collectors.toList());

            int itemsToTransfer = getAmount(direction);

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

    public int getSpeed(Direction direction) {
        Upgrade upgrade = getUpgrade(direction);
        if (upgrade == null) {
            return 20;
        }
        switch (upgrade) {
            case BASIC:
                return 15;
            case IMPROVED:
                return 10;
            case ADVANCED:
                return 5;
            case ULTIMATE:
            default:
                return 1;
        }
    }

    public int getAmount(Direction direction) {
        Upgrade upgrade = getUpgrade(direction);
        if (upgrade == null) {
            return 4;
        }
        switch (upgrade) {
            case BASIC:
                return 8;
            case IMPROVED:
                return 16;
            case ADVANCED:
                return 32;
            case ULTIMATE:
            default:
                return 64;
        }
    }

}
