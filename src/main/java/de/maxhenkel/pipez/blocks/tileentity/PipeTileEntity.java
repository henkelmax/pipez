package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.PipeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class PipeTileEntity extends TileEntity implements ITickableTileEntity {

    @Nullable
    protected List<Connection> connectionCache;
    protected boolean[] extractingSides;
    protected boolean[] disconnectedSides;

    public PipeTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        extractingSides = new boolean[Direction.values().length];
        disconnectedSides = new boolean[Direction.values().length];
    }

    public List<Connection> getConnections() {
        if (world == null) {
            return Collections.emptyList();
        }
        if (connectionCache == null) {
            updateCache();
            if (connectionCache == null) {
                return Collections.emptyList();
            }
        }
        return connectionCache;
    }

    public static void markPipesDirty(World world, BlockPos pos) {
        List<BlockPos> travelPositions = new ArrayList<>();
        List<BlockPos> queue = new ArrayList<>();
        Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof PipeBlock)) {
            return;
        }
        PipeBlock pipeBlock = (PipeBlock) block;

        PipeTileEntity pipeTe = pipeBlock.getTileEntity(world, pos);
        if (pipeTe != null) {
            for (Direction side : Direction.values()) {
                if (pipeTe.isExtracting(side)) {
                    if (!pipeBlock.canConnectTo(world, pos, side)) {
                        pipeTe.setExtracting(side, false);
                    }
                }
            }
        }

        travelPositions.add(pos);
        addToDirtyList(world, pos, pipeBlock, travelPositions, queue);
        while (queue.size() > 0) {
            BlockPos blockPos = queue.get(0);
            block = world.getBlockState(blockPos).getBlock();
            if (block instanceof PipeBlock) {
                addToDirtyList(world, blockPos, (PipeBlock) block, travelPositions, queue);
            }
            queue.remove(0);
        }
        for (BlockPos p : travelPositions) {
            TileEntity te = world.getTileEntity(p);
            if (!(te instanceof PipeTileEntity)) {
                continue;
            }
            PipeTileEntity pipe = (PipeTileEntity) te;
            pipe.connectionCache = null;
        }
    }

    private static void addToDirtyList(World world, BlockPos pos, PipeBlock pipeBlock, List<BlockPos> travelPositions, List<BlockPos> queue) {
        for (Direction direction : Direction.values()) {
            if (pipeBlock.isConnected(world, pos, direction)) {
                BlockPos p = pos.offset(direction);
                if (!travelPositions.contains(p) && !queue.contains(p)) {
                    travelPositions.add(p);
                    queue.add(p);
                }
            }
        }
    }

    private void updateCache() {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof PipeBlock)) {
            connectionCache = null;
            return;
        }
        if (!isExtracting()) {
            connectionCache = null;
            return;
        }

        Map<DirectionalPosition, Integer> connections = new HashMap<>();

        Map<BlockPos, Integer> queue = new HashMap<>();
        List<BlockPos> travelPositions = new ArrayList<>();

        addToQueue(world, pos, queue, travelPositions, connections, 1);

        while (queue.size() > 0) {
            Map.Entry<BlockPos, Integer> blockPosIntegerEntry = queue.entrySet().stream().findAny().get();
            addToQueue(world, blockPosIntegerEntry.getKey(), queue, travelPositions, connections, blockPosIntegerEntry.getValue());
            travelPositions.add(blockPosIntegerEntry.getKey());
            queue.remove(blockPosIntegerEntry.getKey());
        }

        connectionCache = connections.entrySet().stream().map(entry -> new Connection(entry.getKey().getPos(), entry.getKey().getDirection(), entry.getValue())).collect(Collectors.toList());
    }

    public void addToQueue(World world, BlockPos position, Map<BlockPos, Integer> queue, List<BlockPos> travelPositions, Map<DirectionalPosition, Integer> insertPositions, int distance) {
        Block block = world.getBlockState(position).getBlock();
        if (!(block instanceof PipeBlock)) {
            return;
        }
        PipeBlock pipeBlock = (PipeBlock) block;
        for (Direction direction : Direction.values()) {
            if (pipeBlock.isConnected(world, position, direction)) {
                BlockPos p = position.offset(direction);
                DirectionalPosition dp = new DirectionalPosition(p, direction.getOpposite());
                if (canInsert(position, direction)) {
                    if (!insertPositions.containsKey(dp)) {
                        insertPositions.put(dp, distance);
                    } else {
                        if (insertPositions.get(dp) > distance) {
                            insertPositions.put(dp, distance);
                        }
                    }
                } else {
                    if (!travelPositions.contains(p) && !queue.containsKey(p)) {
                        queue.put(p, distance + 1);
                    }
                }
            }
        }
    }

    public boolean canInsert(BlockPos pos, Direction direction) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PipeTileEntity) {
            PipeTileEntity pipe = (PipeTileEntity) te;
            if (pipe.isExtracting(direction)) {
                return false;
            }
        }

        TileEntity tileEntity = world.getTileEntity(pos.offset(direction));
        if (tileEntity == null) {
            return false;
        }
        return canInsert(tileEntity, direction.getOpposite());
    }

    public abstract boolean canInsert(TileEntity tileEntity, Direction direction);

    @Override
    public void tick() {

    }

    public boolean isExtracting(Direction side) {
        return extractingSides[side.getIndex()];
    }

    public boolean isExtracting() {
        for (boolean extract : extractingSides) {
            if (extract) {
                return true;
            }
        }
        return false;
    }

    public boolean hasReasonToStay() {
        if (isExtracting()) {
            return true;
        }
        for (boolean disconnected : disconnectedSides) {
            if (disconnected) {
                return true;
            }
        }
        return false;
    }

    public void setExtracting(Direction side, boolean extracting) {
        extractingSides[side.getIndex()] = extracting;
        markDirty();
    }

    public boolean isDisconnected(Direction side) {
        return disconnectedSides[side.getIndex()];
    }

    public void setDisconnected(Direction side, boolean disconnected) {
        disconnectedSides[side.getIndex()] = disconnected;
        markDirty();
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        extractingSides = new boolean[Direction.values().length];
        ListNBT extractingList = compound.getList("ExtractingSides", Constants.NBT.TAG_BYTE);
        if (extractingList.size() >= extractingSides.length) {
            for (int i = 0; i < extractingSides.length; i++) {
                ByteNBT b = (ByteNBT) extractingList.get(i);
                extractingSides[i] = b.getByte() != 0;
            }
        }

        disconnectedSides = new boolean[Direction.values().length];
        ListNBT disconnectedList = compound.getList("DisconnectedSides", Constants.NBT.TAG_BYTE);
        if (disconnectedList.size() >= disconnectedSides.length) {
            for (int i = 0; i < disconnectedSides.length; i++) {
                ByteNBT b = (ByteNBT) disconnectedList.get(i);
                disconnectedSides[i] = b.getByte() != 0;
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT extractingList = new ListNBT();
        for (boolean extractingSide : extractingSides) {
            extractingList.add(ByteNBT.valueOf(extractingSide));
        }
        compound.put("ExtractingSides", extractingList);

        ListNBT disconnectedList = new ListNBT();
        for (boolean disconnected : disconnectedSides) {
            disconnectedList.add(ByteNBT.valueOf(disconnected));
        }
        compound.put("DisconnectedSides", disconnectedList);
        return super.write(compound);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        handleUpdateTag(getBlockState(), pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    public static class Connection {
        private final BlockPos pos;
        private final Direction direction;
        private final int distance;

        public Connection(BlockPos pos, Direction direction, int distance) {
            this.pos = pos;
            this.direction = direction;
            this.distance = distance;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Direction getDirection() {
            return direction;
        }

        public int getDistance() {
            return distance;
        }

        @Override
        public String toString() {
            return "Connection{" +
                    "pos=" + pos +
                    ", direction=" + direction +
                    ", distance=" + distance +
                    '}';
        }
    }

    public static class DirectionalPosition {
        private final BlockPos pos;
        private final Direction direction;

        public DirectionalPosition(BlockPos pos, Direction direction) {
            this.pos = pos;
            this.direction = direction;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Direction getDirection() {
            return direction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DirectionalPosition that = (DirectionalPosition) o;
            if (!pos.equals(that.pos)) {
                return false;
            }
            return direction == that.direction;
        }
    }

}
