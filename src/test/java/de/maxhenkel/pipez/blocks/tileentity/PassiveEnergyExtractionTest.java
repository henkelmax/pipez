package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(EphemeralTestServerProvider.class)
class PassiveEnergyExtractionTest {

    @Test
    void extractionStartsWithoutAnIncomingCapabilityQuery(MinecraftServer server) {
        var source = new SimpleEnergyHandler(10000, 10000, 10000, 4000);
        var destination = new SimpleEnergyHandler(10000);
        var level = mock(ServerLevel.class);
        when(level.getGameTime()).thenReturn(10L);
        // Real connection subclasses avoid reflecting over the optional chemical API.
        var extractingConnection = new PipeTileEntity.Connection(level, BlockPos.ZERO, Direction.SOUTH, 0) {
            @Override
            public EnergyHandler getEnergyHandler() {
                return source;
            }
        };
        var destinationConnection = new PipeTileEntity.Connection(level, BlockPos.ZERO.east(), Direction.WEST, 1) {
            @Override
            public EnergyHandler getEnergyHandler() {
                return destination;
            }
        };
        var pipe = new EnergyPipeTileEntity(BlockPos.ZERO, ModBlocks.ENERGY_PIPE.get().defaultBlockState()) {
            @Override
            public Connection getExtractingConnection(Direction side) {
                return extractingConnection;
            }

            @Override
            public List<Connection> getSortedConnections(Direction side, PipeType type) {
                return List.of(destinationConnection);
            }

            @Override
            public boolean shouldWork(Direction side, PipeType<?, ?> type) {
                return true;
            }

            @Override
            public Distribution getDistribution(Direction side, PipeType type) {
                return Distribution.NEAREST;
            }
        };
        pipe.setLevel(level);
        pipe.extractingSides[Direction.NORTH.get3DDataValue()] = true;
        // Do not query onRegisterCapability(): a passive source never asks the pipe to receive.
        assertNull(pipe.energyStorages[Direction.NORTH.get3DDataValue()]);
        pipe.tick();
        assertTrue(destination.getAmountAsLong() > 0, "The first extraction tick must move energy from a passive source");
        assertEquals(Math.min(4000, EnergyPipeType.INSTANCE.getRate(null)), destination.getAmountAsLong());
        assertEquals(4000, source.getAmountAsLong() + destination.getAmountAsLong());
        var storage = pipe.energyStorages[Direction.NORTH.get3DDataValue()];
        for (var side : Direction.values()) {
            if (side != Direction.NORTH) {
                assertNull(pipe.energyStorages[side.get3DDataValue()]);
            }
        }

        for (int i = 0; i < 100; i++) {
            when(level.getGameTime()).thenReturn(11L + i);
            pipe.tick();
            assertSame(storage, pipe.energyStorages[Direction.NORTH.get3DDataValue()]);
            assertEquals(4000, source.getAmountAsLong() + destination.getAmountAsLong());
        }
        assertEquals(4000, destination.getAmountAsLong());

        // A full destination must not cause energy loss.
        try (var transaction = Transaction.openRoot()) {
            destination.insert(10000, transaction);
            source.insert(4000, transaction);
            transaction.commit();
        }
        pipe.tick();
        assertEquals(4000, source.getAmountAsLong());
        assertEquals(10000, destination.getAmountAsLong());
    }
}
