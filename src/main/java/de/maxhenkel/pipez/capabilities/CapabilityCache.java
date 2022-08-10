package de.maxhenkel.pipez.capabilities;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.events.ServerTickEvents;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.LongStream;

public class CapabilityCache {

    private static final long UPDATE_DURATION = 20; // 20 ticks

    public static CapabilityCache getInstance() {
        return ServerTickEvents.capabilityCache;
    }

    public long tickCount = UPDATE_DURATION * 2;

    protected Logger logger = LogManager.getLogger(Main.MODID);

    protected HashMap<Level, HashMap<BlockPos, EnumMap<Direction, LazyOptional<IItemHandler>>>> itemCache = new HashMap<>();
    protected HashMap<Level, HashMap<BlockPos, EnumMap<Direction, LazyOptional<IFluidHandler>>>> fluidCache = new HashMap<>();
    protected HashMap<Level, HashMap<BlockPos, EnumMap<Direction, LazyOptional<IEnergyStorage>>>> energyCache = new HashMap<>();
    protected HashMap<Level, HashMap<BlockPos, EnumMap<Direction, LazyOptional<IGasHandler>>>> gasCache = new HashMap<>();

    protected HashMap<Level, HashMap<BlockPos, EnumMap<Direction, long[]>>> updatedTimeCache = new HashMap<>();
    public CapabilityCache() {

    }

    public void addTick() {
        tickCount += 1;
    }

    protected <T> LazyOptional<T> getCachedValue(HashMap<Level, HashMap<BlockPos, EnumMap<Direction, LazyOptional<T>>>> cacheMap, Level level, BlockPos blockPos, Direction direction) {
        HashMap<BlockPos, EnumMap<Direction, LazyOptional<T>>> cacheLevelMap = cacheMap.get(level);
        if (cacheLevelMap == null) {
            cacheMap.put(level, new HashMap<>());
            return LazyOptional.empty();
        }
        EnumMap<Direction, LazyOptional<T>> cachePosMap = cacheLevelMap.get(blockPos);
        if (cachePosMap == null) {
            cacheLevelMap.put(blockPos, new EnumMap<>(Direction.class));
            return LazyOptional.empty();
        }
        return cachePosMap.get(direction);
    }

    protected long getUpdatedTime(Level level, BlockPos blockPos, Direction direction, CapType capType) {
        var posMap = updatedTimeCache.computeIfAbsent(level, k -> new HashMap<>());
        var directionMap = posMap.computeIfAbsent(blockPos, k -> new EnumMap<>(Direction.class));
        var timestamps = directionMap.computeIfAbsent(direction, k -> LongStream.of(0, 0, 0, 0).toArray());
        return switch (capType) {
            case ITEM -> timestamps[0];
            case FLUID -> timestamps[1];
            case ENERGY -> timestamps[2];
            case GAS -> timestamps[3];
        };
    }

    protected void putUpdatedTime(Level level, BlockPos blockPos, Direction direction, CapType capType,
                                  long timestamp) {
        var posMap = updatedTimeCache.computeIfAbsent(level, k -> new HashMap<>());
        var directionMap = posMap.computeIfAbsent(blockPos, k -> new EnumMap<>(Direction.class));
        var timestamps = directionMap.computeIfAbsent(direction, k -> LongStream.of(0, 0, 0, 0).toArray());
        var index = switch (capType) {
            case ITEM -> 0;
            case FLUID -> 1;
            case ENERGY -> 2;
            case GAS -> 3;
        };
        timestamps[index] = timestamp;
    }

    protected <T> void putCacheValue(HashMap<Level, HashMap<BlockPos, EnumMap<Direction, T>>> cacheMap, Level level, BlockPos blockPos, Direction direction, T value) {
        HashMap<BlockPos, EnumMap<Direction, T>> cachePosMap = cacheMap.get(level);
        if (cachePosMap == null) {
            cachePosMap = new HashMap<>();
            cacheMap.put(level, cachePosMap);
        }
        EnumMap<Direction, T> cacheDirectionMap = cachePosMap.get(blockPos);
        if (cacheDirectionMap == null) {
            cacheDirectionMap = new EnumMap<>(Direction.class);
            cachePosMap.put(blockPos, cacheDirectionMap);
        }
        cacheDirectionMap.put(direction, value);
    }


    protected <T> LazyOptional<T> getCachedCapability(HashMap<Level, HashMap<BlockPos, EnumMap<Direction, LazyOptional<T>>>> cacheMap, net.minecraftforge.common.capabilities.Capability<T> cap,
                                     @Nullable Level level, BlockPos blockPos, Direction direction, CapType capType) {
        // Check level is null
        if (level == null) {
            return LazyOptional.empty();
        }

        // Get Value
        LazyOptional<T> capability = getCachedValue(cacheMap, level, blockPos, direction);
        // logger.log(org.apache.logging.log4j.Level.DEBUG, "Cache Hit: " + (capability != null ? "true" : "false"));
        if (capability == null || !capability.isPresent()) {
            // Check updated time
            /*
            long updatedTime = getUpdatedTime(level, blockPos, direction, capType);
            if (Math.abs(tickCount - updatedTime) < UPDATE_DURATION) {
                return LazyOptional.empty();
            }
             */
            // Check BlockEntity exists
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity == null) {
                return LazyOptional.empty();
            }
            capability = blockEntity.getCapability(cap, direction);
            putCacheValue(cacheMap, level, blockPos, direction, capability);
            // putUpdatedTime(level, blockPos, direction, capType, tickCount);
            capability.addListener(self -> {
                // logger.log(org.apache.logging.log4j.Level.DEBUG, "Remove Cache:" + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ() + ", " + direction.getSerializedName());
                putCacheValue(cacheMap, level, blockPos, direction, null);
                // putUpdatedTime(level, blockPos, direction, capType, tickCount);
            });
            // logger.log(org.apache.logging.log4j.Level.DEBUG, "Cache Miss: " + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ() + ", " + direction.getSerializedName());
        }
        return capability;
    }

    @Nullable
    protected <T> T getCachedCapabilityResult(HashMap<Level, HashMap<BlockPos, EnumMap<Direction, LazyOptional<T>>>> cacheMap, net.minecraftforge.common.capabilities.Capability<T> cap,
                                           @Nullable Level level, BlockPos blockPos, Direction direction, CapType capType) {
        LazyOptional<T> cache = getCachedCapability(cacheMap, cap, level, blockPos, direction, capType);
        if (!cache.isPresent()) {
            return null;
        } else {
            Optional<T> cacheResolved = cache.resolve();
            if (cacheResolved.isEmpty()) {
                return null;
            } else {
                return cacheResolved.get();
            }
        }
    }


    public LazyOptional<IItemHandler> getItemCapability(@Nullable Level level, BlockPos blockPos, Direction direction) {
        return getCachedCapability(itemCache, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                level, blockPos, direction, CapType.ITEM);
    }

    @Nullable
    public IItemHandler getItemCapabilityResult(@Nullable Level level, BlockPos blockPos, Direction direction) {
        return getCachedCapabilityResult(itemCache, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                level, blockPos, direction, CapType.ITEM);
    }

    public LazyOptional<IFluidHandler> getFluidCapability(@Nullable Level level, BlockPos blockPos, Direction direction) {
        return getCachedCapability(fluidCache, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                level, blockPos, direction, CapType.FLUID);
    }

    @Nullable
    public IFluidHandler getFluidCapabilityResult(@Nullable Level level, BlockPos blockPos, Direction direction) {
        return getCachedCapabilityResult(fluidCache, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                level, blockPos, direction, CapType.FLUID);
    }

    public LazyOptional<IEnergyStorage> getEnergyCapability(@Nullable Level level, BlockPos blockPos, Direction direction) {
        return getCachedCapability(energyCache, CapabilityEnergy.ENERGY,
                level, blockPos, direction, CapType.ENERGY);
    }

    @Nullable
    public IEnergyStorage getEnergyCapabilityResult(@Nullable Level level, BlockPos blockPos, Direction direction) {
        return getCachedCapabilityResult(energyCache, CapabilityEnergy.ENERGY,
                level, blockPos, direction, CapType.ENERGY);
    }

    public LazyOptional<IGasHandler> getGasCapability(@Nullable Level level, BlockPos blockPos, Direction direction) {
        return getCachedCapability(gasCache, ModCapabilities.GAS_HANDLER_CAPABILITY,
                level, blockPos, direction, CapType.GAS);
    }

    @Nullable
    public IGasHandler getGasCapabilityResult(@Nullable Level level, BlockPos blockPos, Direction direction) {
        return getCachedCapabilityResult(gasCache, ModCapabilities.GAS_HANDLER_CAPABILITY,
                level, blockPos, direction, CapType.GAS);
    }

    enum CapType {
        ITEM,
        FLUID,
        ENERGY,
        GAS,
    }
}
