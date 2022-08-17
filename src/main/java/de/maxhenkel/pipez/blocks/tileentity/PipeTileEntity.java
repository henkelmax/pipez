package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.corelib.blockentity.ITickableBlockEntity;
import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.blocks.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class PipeTileEntity extends BlockEntity implements ITickableBlockEntity {

    @Nullable
    protected List<Connection> connectionCache;
    protected boolean[] extractingSides;
    protected boolean[] disconnectedSides;

    /**
     * Invalidating the cache five ticks after load, because Mekanism is broken!
     */
    private int invalidateCountdown;

    public PipeTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
        extractingSides = new boolean[Direction.values().length];
        disconnectedSides = new boolean[Direction.values().length];
    }

    public List<Connection> getConnections() {
        if (level == null) {
            return new ArrayList<>();
        }
        if (connectionCache == null) {
            updateCache();
            if (connectionCache == null) {
                return new ArrayList<>();
            }
        }
        return connectionCache;
    }

    public static void markPipesDirty(Level world, BlockPos pos) {
        List<BlockPos> travelPositions = new ArrayList<>();
        LinkedList<BlockPos> queue = new LinkedList<>();
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
                        pipeTe.setExtracting(side, true); // .
                        if (!pipeTe.hasReasonToStay()) {
                            pipeBlock.setHasData(world, pos, false);
                        }
                        pipeTe.syncData();
                    }
                }
            }
        }

        travelPositions.add(pos);
        addToDirtyList(world, pos, pipeBlock, travelPositions, queue);
        while (queue.size() > 0) {
            BlockPos blockPos = queue.removeFirst();
            block = world.getBlockState(blockPos).getBlock();
            if (block instanceof PipeBlock) {
                addToDirtyList(world, blockPos, (PipeBlock) block, travelPositions, queue);
            }
        }
        for (BlockPos p : travelPositions) {
            BlockEntity te = world.getBlockEntity(p);
            if (!(te instanceof PipeTileEntity)) {
                continue;
            }
            PipeTileEntity pipe = (PipeTileEntity) te;
            pipe.connectionCache = null;
        }
    }

    private static void addToDirtyList(Level world, BlockPos pos, PipeBlock pipeBlock, List<BlockPos> travelPositions, LinkedList<BlockPos> queue) {
        for (Direction direction : Direction.values()) {
            if (pipeBlock.isConnected(world, pos, direction)) {
                BlockPos p = pos.relative(direction);
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

        addToQueue(level, worldPosition, queue, travelPositions, connections, 1);

        while (queue.size() > 0) {
            Map.Entry<BlockPos, Integer> blockPosIntegerEntry = queue.entrySet().stream().findAny().get();
            addToQueue(level, blockPosIntegerEntry.getKey(), queue, travelPositions, connections, blockPosIntegerEntry.getValue());
            travelPositions.add(blockPosIntegerEntry.getKey());
            queue.remove(blockPosIntegerEntry.getKey());
        }

        connectionCache = connections.entrySet().stream().map(
                entry -> new Connection(entry.getKey().getPos(), entry.getKey().getDirection(), entry.getValue())
        ).sorted(
                Comparator.comparingInt(PipeTileEntity.Connection::getDistance)
        ).collect(Collectors.toList());
    }

    public void addToQueue(Level world, BlockPos position, Map<BlockPos, Integer> queue, List<BlockPos> travelPositions, Map<DirectionalPosition, Integer> insertPositions, int distance) {
        Block block = world.getBlockState(position).getBlock();
        if (!(block instanceof PipeBlock)) {
            return;
        }
        PipeBlock pipeBlock = (PipeBlock) block;
        for (Direction direction : Direction.values()) {
            if (pipeBlock.isConnected(world, position, direction)) {
                BlockPos p = position.relative(direction);
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
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof PipeTileEntity) {
            PipeTileEntity pipe = (PipeTileEntity) te;
            if (pipe.isExtracting(direction)) {
                return false;
            }
        }

        BlockEntity tileEntity = level.getBlockEntity(pos.relative(direction));
        if (tileEntity == null) {
            return false;
        }
        if (tileEntity instanceof PipeTileEntity) {
            return false;
        }
        return canInsert(tileEntity, direction.getOpposite());
    }

    public abstract boolean canInsert(BlockEntity tileEntity, Direction direction);

    @Override
    public void tick() {
        if (invalidateCountdown > 0) {
            invalidateCountdown--;
            if (invalidateCountdown <= 0) {
                connectionCache = null;
            }
        }
    }

    public boolean isExtracting(Direction side) {
        return extractingSides[side.get3DDataValue()];
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
        extractingSides[side.get3DDataValue()] = extracting;
        setChanged();
    }

    public boolean isDisconnected(Direction side) {
        return disconnectedSides[side.get3DDataValue()];
    }

    public void setDisconnected(Direction side, boolean disconnected) {
        disconnectedSides[side.get3DDataValue()] = disconnected;
        setChanged();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        extractingSides = new boolean[Direction.values().length];
        ListTag extractingList = compound.getList("ExtractingSides", Tag.TAG_BYTE);
        if (extractingList.size() >= extractingSides.length) {
            for (int i = 0; i < extractingSides.length; i++) {
                ByteTag b = (ByteTag) extractingList.get(i);
                extractingSides[i] = b.getAsByte() != 0;
            }
        }

        disconnectedSides = new boolean[Direction.values().length];
        ListTag disconnectedList = compound.getList("DisconnectedSides", Tag.TAG_BYTE);
        if (disconnectedList.size() >= disconnectedSides.length) {
            for (int i = 0; i < disconnectedSides.length; i++) {
                ByteTag b = (ByteTag) disconnectedList.get(i);
                disconnectedSides[i] = b.getAsByte() != 0;
            }
        }
        invalidateCountdown = 5;
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        ListTag extractingList = new ListTag();
        for (boolean extractingSide : extractingSides) {
            extractingList.add(ByteTag.valueOf(extractingSide));
        }
        compound.put("ExtractingSides", extractingList);

        ListTag disconnectedList = new ListTag();
        for (boolean disconnected : disconnectedSides) {
            disconnectedList.add(ByteTag.valueOf(disconnected));
        }
        compound.put("DisconnectedSides", disconnectedList);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncData(ServerPlayer player) {
        player.connection.send(getUpdatePacket());
    }

    public void syncData() {
        if (level == null || level.isClientSide) {
            return;
        }
        LevelChunk chunk = level.getChunkAt(getBlockPos());
        ((ServerChunkCache) level.getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(e -> e.connection.send(getUpdatePacket()));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        saveAdditional(updateTag);
        return updateTag;
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

}
