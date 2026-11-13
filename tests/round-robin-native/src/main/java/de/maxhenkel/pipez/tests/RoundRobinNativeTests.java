package de.maxhenkel.pipez.tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.GsonBuilder;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.FluidFilter;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.FluidPipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;

/** Disposable local fixture. Never install on a public server. */
@Mod("pipez_round_robin_test")
public class RoundRobinNativeTests {
    static FluidResource WATER;

    static void check(boolean ok, String name, List<String> checks) {
        if (!ok)
            throw new AssertionError(name);
        checks.add(name);
    }

    static class Tile extends FluidPipeTileEntity {
        int cursor;
        List<Filter<?, ?>> filters = List.of();
        UpgradeTileEntity.FilterMode mode = UpgradeTileEntity.FilterMode.WHITELIST;

        Tile(ServerLevel level, int cursor) {
            super(BlockPos.ZERO, ModBlocks.FLUID_PIPE.get().defaultBlockState());
            setLevel(level);
            this.cursor = cursor;
        }

        @Override
        public int getRoundRobinIndex(Direction d, PipeType<?, ?> t) {
            return cursor;
        }

        @Override
        public void setRoundRobinIndex(Direction d, PipeType<?, ?> t, int value) {
            cursor = value;
        }

        @Override
        public <T> List<Filter<?, ?>> getFilters(Direction d, PipeType<T, ?> t) {
            return filters;
        }

        @Override
        public UpgradeTileEntity.FilterMode getFilterMode(Direction d, PipeType t) {
            return mode;
        }
    }

    static class Pipe extends FluidPipeType {
        int rate;

        @Override
        public int getRate(PipeLogicTileEntity t, Direction d) {
            return rate;
        }

        void run(Tile t, List<PipeTileEntity.Connection> cs, ResourceHandler<FluidResource> from) {
            insertEqually(t, Direction.NORTH, cs, from);
        }
    }

    static class Connection extends PipeTileEntity.Connection {
        final ResourceHandler<FluidResource> handler;

        Connection(ServerLevel level, int index, ResourceHandler<FluidResource> handler) {
            super(level, new BlockPos(index, 64, 0), Direction.NORTH, 1);
            this.handler = handler;
        }

        @Override
        public ResourceHandler<FluidResource> getFluidHandler() {
            return handler;
        }
    }

    public RoundRobinNativeTests() {
        if (!Boolean.getBoolean("pipez.roundrobin.fixture"))
            throw new IllegalStateException("Local fixture flag required");
        NeoForge.EVENT_BUS.addListener(this::commands);
    }

    void commands(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(Commands.literal("pipezroundrobinqa")
                        .requires(s -> s.getEntity() == null && s.getServer().getLocalIp().equals("127.0.0.1"))
                        .executes(ctx -> {
                            var out = new LinkedHashMap<String, Object>();
                            var checks = new ArrayList<String>();
                            try {
                                if (!ctx.getSource().getServer().getPlayerList().getPlayers().isEmpty())
                                    throw new IllegalStateException("No players permitted");
                                WATER = FluidResource.of(Fluids.WATER);
                                var vectors = new ArrayList<List<Long>>();
                                var level = ctx.getSource().getServer().overworld();
                                var pipe = new Pipe();
                                for (int count : new int[] { 0, 1, 2, 7, 31 })
                                    for (int start = 0; start < Math.max(count, 1); start++)
                                        for (int rate : new int[] { 0, 1, 5, 101 })
                                            for (int scenario = 0; scenario < 4; scenario++) {
                                                var tile = new Tile(level, start);
                                                pipe.rate = rate;
                                                var source = new FluidStacksResourceHandler(1, 10000);
                                                source.set(0, WATER, scenario == 3 ? 0 : 97);
                                                var dests = new ArrayList<FluidStacksResourceHandler>();
                                                var cs = new ArrayList<PipeTileEntity.Connection>();
                                                for (int i = 0; i < count; i++) {
                                                    var dest = new FluidStacksResourceHandler(1, (i % 3 == 0) ? 0 : 17);
                                                    dests.add(dest);
                                                    cs.add(new Connection(level, i,
                                                            scenario == 2 && i % 2 == 0 ? null : dest));
                                                }
                                                if (scenario == 1) {
                                                    var f = new FluidFilter();
                                                    f.setInvert(true);
                                                    tile.filters = List.of(f);
                                                }
                                                pipe.run(tile, cs, source);
                                                long total = source.getAmountAsLong(0);
                                                var vector = new ArrayList<Long>();
                                                vector.add(total);
                                                vector.add((long) tile.cursor);
                                                for (var dest : dests) {
                                                    long amount = dest.getAmountAsLong(0);
                                                    total += amount;
                                                    vector.add(amount);
                                                }
                                                check(total == (scenario == 3 ? 0 : 97), "fluid_conservation", checks);
                                                vectors.add(vector);
                                                check(Transaction.getLifecycle() == Transaction.Lifecycle.NONE,
                                                        "transaction_closed", checks);
                                            }
                                out.put("fluid_vectors", vectors);
                                var txSource = new FluidStacksResourceHandler(1, 10000);
                                txSource.set(0, WATER, 1000);
                                var txDest = new FluidStacksResourceHandler(1, 10000);
                                try (var root = Transaction.openRoot()) {
                                    check(ResourceHandlerUtil.move(txSource, txDest, r -> true, 200, root) == 200,
                                            "nested_move", checks);
                                    check(txDest.getAmountAsLong(0) == 200, "child_commit_visible", checks);
                                }
                                check(txSource.getAmountAsLong(0) == 1000 && txDest.getAmountAsLong(0) == 0,
                                        "parent_abort_restores_fluid", checks);
                                check(ResourceHandlerUtil.move(txSource, txDest, r -> true, 200, null) == 200,
                                        "root_move", checks);
                                try (var root = Transaction.openRoot()) {
                                    boolean rejected = false;
                                    try {
                                        ResourceHandlerUtil.move(txSource, txDest, r -> true, 1, null);
                                    } catch (net.minecraft.ReportedException ex) {
                                        rejected = ex.getCause() instanceof IllegalStateException && ex.getCause()
                                                .getMessage().contains(ResourceHandlerUtil.class.getName());
                                    }
                                    check(rejected, "invalid_root_still_has_resource_handler_caller", checks);
                                }
                                var tile = new Tile(level, 0);
                                pipe.rate = 1000;
                                var empty = new FluidStacksResourceHandler(1, 10000);
                                var cs = new ArrayList<PipeTileEntity.Connection>();
                                for (int i = 0; i < 2048; i++)
                                    cs.add(new Connection(level, i, new FluidStacksResourceHandler(1, 0)));
                                for (int i = 0; i < 3; i++)
                                    pipe.run(tile, cs, empty);
                                var fluidTimes = new ArrayList<Long>();
                                for (int i = 0; i < 7; i++) {
                                    long start = System.nanoTime();
                                    for (int j = 0; j < 8; j++)
                                        pipe.run(tile, cs, empty);
                                    fluidTimes.add(System.nanoTime() - start);
                                }
                                out.put("empty_fluid_2048_dest_8ticks_nanos", fluidTimes);
                                out.put("checks", checks);
                                out.put("phase", "passed");
                            } catch (Throwable ex) {
                                out.put("phase", "failed");
                                out.put("error", ex.toString());
                            }
                            try {
                                Files.writeString(Path.of("pipez-round-robin-test.json"),
                                        new GsonBuilder().setPrettyPrinting().create().toJson(out));
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal("TRANSFER_QA " + out.get("phase")),
                                    false);
                            return 1;
                        }));
    }
}
