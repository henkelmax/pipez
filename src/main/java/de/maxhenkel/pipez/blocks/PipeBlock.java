package de.maxhenkel.pipez.blocks;

import de.maxhenkel.corelib.block.IItemBlock;
import de.maxhenkel.corelib.block.VoxelUtils;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.corelib.helpers.Triple;
import de.maxhenkel.pipez.ModItemGroups;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.items.WrenchItem;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public abstract class PipeBlock extends Block implements IItemBlock, IWaterLoggable, ITileEntityProvider {

    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty HAS_DATA = BooleanProperty.create("has_data");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected PipeBlock() {
        super(Block.Properties.create(Material.IRON, MaterialColor.GRAY).hardnessAndResistance(0.5F).sound(SoundType.METAL));

        setDefaultState(stateContainer.getBaseState()
                .with(UP, false)
                .with(DOWN, false)
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(EAST, false)
                .with(WEST, false)
                .with(HAS_DATA, false)
                .with(WATERLOGGED, false)
        );
    }

    @Override
    public Item toItem() {
        return new BlockItem(this, new Item.Properties().group(ModItemGroups.TAB_PIPEZ)).setRegistryName(getRegistryName());
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        Direction side = getSelection(state, worldIn, pos, player).getKey();
        if (side != null) {
            return onPipeSideActivated(state, worldIn, pos, player, handIn, hit, side);
        } else {
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
        }
    }

    public ActionResultType onWrenchClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!player.isSneaking()) {
            return ActionResultType.PASS;
        }

        Direction side = getSelection(state, worldIn, pos, player).getKey();
        if (side != null) {
            if (worldIn.getBlockState(pos.offset(side)).getBlock() != this) {
                boolean extracting = isExtracting(worldIn, pos, side);
                if (extracting) {
                    setExtracting(worldIn, pos, side, false);
                    setDisconnected(worldIn, pos, side, true);
                } else {
                    setExtracting(worldIn, pos, side, true);
                    setDisconnected(worldIn, pos, side, false);
                }
            } else {
                setDisconnected(worldIn, pos, side, true);
            }
        } else {
            // Core
            side = hit.getFace();
            if (worldIn.getBlockState(pos.offset(side)).getBlock() != this) {
                setExtracting(worldIn, pos, side, false);
                if (isAbleToConnect(worldIn, pos, side)) {
                    setDisconnected(worldIn, pos, side, false);
                }
            } else {
                setDisconnected(worldIn, pos, side, false);
                setDisconnected(worldIn, pos.offset(side), side.getOpposite(), false);
            }
        }

        PipeTileEntity.markPipesDirty(worldIn, pos);
        return ActionResultType.SUCCESS;
    }

    public ActionResultType onPipeSideActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, Direction direction) {
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    public BooleanProperty getProperty(Direction side) {
        switch (side) {
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case EAST:
                return EAST;
            case WEST:
                return WEST;
            case UP:
                return UP;
            case DOWN:
            default:
                return DOWN;
        }
    }

    public boolean isExtracting(IWorldReader world, BlockPos pos, Direction side) {
        PipeTileEntity pipe = getTileEntity(world, pos);
        if (pipe == null) {
            return false;
        }
        return pipe.isExtracting(side);
    }

    public boolean isDisconnected(IWorldReader world, BlockPos pos, Direction side) {
        PipeTileEntity pipe = getTileEntity(world, pos);
        if (pipe == null) {
            return false;
        }
        return pipe.isDisconnected(side);
    }

    public void setHasData(World world, BlockPos pos, boolean hasData) {
        BlockState blockState = world.getBlockState(pos);
        //if (blockState.get(HAS_DATA) != hasData) {
        world.setBlockState(pos, blockState.with(HAS_DATA, hasData));
        //}
    }

    public void setExtracting(World world, BlockPos pos, Direction side, boolean extracting) {
        PipeTileEntity pipe = getTileEntity(world, pos);
        if (pipe == null) {
            if (extracting) {
                setHasData(world, pos, true);
                pipe = getTileEntity(world, pos);
                if (pipe != null) {
                    pipe.setExtracting(side, extracting);
                }
            }
        } else {
            pipe.setExtracting(side, extracting);
            if (!pipe.hasReasonToStay()) {
                setHasData(world, pos, false);
            }
        }
    }

    public void setDisconnected(World world, BlockPos pos, Direction side, boolean disconnected) {
        PipeTileEntity pipe = getTileEntity(world, pos);
        if (pipe == null) {
            if (disconnected) {
                setHasData(world, pos, true);
                pipe = getTileEntity(world, pos);
                if (pipe != null) {
                    pipe.setDisconnected(side, disconnected);
                    world.setBlockState(pos, world.getBlockState(pos).with(getProperty(side), false));
                }
            }
        } else {
            pipe.setDisconnected(side, disconnected);
            if (!pipe.hasReasonToStay()) {
                setHasData(world, pos, false);
            }
            world.setBlockState(pos, world.getBlockState(pos).with(getProperty(side), !disconnected));
        }
    }

    @Nullable
    public PipeTileEntity getTileEntity(IWorldReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PipeTileEntity) {
            return (PipeTileEntity) te;
        }
        return null;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getState(context.getWorld(), context.getPos(), null);
    }

    private BlockState getState(World world, BlockPos pos, @Nullable BlockState oldState) {
        FluidState fluidState = world.getFluidState(pos);
        boolean hasData = false;
        if (oldState != null && oldState.getBlock() == this) {
            hasData = oldState.get(HAS_DATA);
        }
        return getDefaultState()
                .with(UP, isConnected(world, pos, Direction.UP))
                .with(DOWN, isConnected(world, pos, Direction.DOWN))
                .with(NORTH, isConnected(world, pos, Direction.NORTH))
                .with(SOUTH, isConnected(world, pos, Direction.SOUTH))
                .with(EAST, isConnected(world, pos, Direction.EAST))
                .with(WEST, isConnected(world, pos, Direction.WEST))
                .with(HAS_DATA, hasData)
                .with(WATERLOGGED, fluidState.isTagged(FluidTags.WATER) && fluidState.getLevel() == 8);
    }

    public boolean isConnected(IWorldReader world, BlockPos pos, Direction facing) {
        PipeTileEntity pipe = getTileEntity(world, pos);
        PipeTileEntity other = getTileEntity(world, pos.offset(facing));

        if (!isAbleToConnect(world, pos, facing)) {
            return false;
        }
        boolean canSelfConnect = pipe == null || !pipe.isDisconnected(facing);
        if (!canSelfConnect) {
            return false;
        }
        boolean canSideConnect = other == null || !other.isDisconnected(facing.getOpposite());
        return canSideConnect;
    }

    public boolean isAbleToConnect(IWorldReader world, BlockPos pos, Direction facing) {
        return isPipe(world, pos, facing) || canConnectTo(world, pos, facing);
    }

    public abstract boolean canConnectTo(IWorldReader world, BlockPos pos, Direction facing);

    public abstract boolean isPipe(IWorldReader world, BlockPos pos, Direction facing);

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos pos1, boolean b) {
        super.neighborChanged(state, world, pos, block, pos1, b);
        BlockState newState = getState(world, pos, state);
        if (!state.getProperties().stream().allMatch(property -> state.get(property).equals(newState.get(property)))) {
            world.setBlockState(pos, newState);
            PipeTileEntity.markPipesDirty(world, pos);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, HAS_DATA, WATERLOGGED);
    }

    public static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(5D, 5D, 5D, 11D, 11D, 0D);
    public static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(5D, 5D, 11D, 11D, 11D, 16D);
    public static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(11D, 5D, 5D, 16D, 11D, 11D);
    public static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(5D, 5D, 5D, 0D, 11D, 11D);
    public static final VoxelShape SHAPE_UP = Block.makeCuboidShape(5D, 11D, 5D, 11D, 16D, 11D);
    public static final VoxelShape SHAPE_DOWN = Block.makeCuboidShape(5D, 5D, 5D, 11D, 0D, 11D);
    public static final VoxelShape SHAPE_CORE = Block.makeCuboidShape(5D, 5D, 5D, 11D, 11D, 11D);
    public static final VoxelShape SHAPE_EXTRACT_NORTH = VoxelUtils.combine(SHAPE_NORTH, Block.makeCuboidShape(4D, 4D, 1D, 12D, 12D, 0D));
    public static final VoxelShape SHAPE_EXTRACT_SOUTH = VoxelUtils.combine(SHAPE_SOUTH, Block.makeCuboidShape(4D, 4D, 15D, 12D, 12D, 16D));
    public static final VoxelShape SHAPE_EXTRACT_EAST = VoxelUtils.combine(SHAPE_EAST, Block.makeCuboidShape(15D, 4D, 4D, 16D, 12D, 12D));
    public static final VoxelShape SHAPE_EXTRACT_WEST = VoxelUtils.combine(SHAPE_WEST, Block.makeCuboidShape(1D, 4D, 4D, 0D, 12D, 12D));
    public static final VoxelShape SHAPE_EXTRACT_UP = VoxelUtils.combine(SHAPE_UP, Block.makeCuboidShape(4D, 15D, 4D, 12D, 16D, 12D));
    public static final VoxelShape SHAPE_EXTRACT_DOWN = VoxelUtils.combine(SHAPE_DOWN, Block.makeCuboidShape(4D, 1D, 4D, 12D, 0D, 12D));

    public VoxelShape getShape(IBlockReader blockReader, BlockPos pos, BlockState state, boolean advanced) {
        PipeTileEntity pipe = null;
        if (advanced && blockReader instanceof IWorldReader) {
            pipe = getTileEntity((IWorldReader) blockReader, pos);
        }

        VoxelShape shape = SHAPE_CORE;
        if (state.get(UP)) {
            if (pipe != null && pipe.isExtracting(Direction.UP)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_UP);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_UP);
            }
        }
        if (state.get(DOWN)) {
            if (pipe != null && pipe.isExtracting(Direction.DOWN)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_DOWN);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_DOWN);
            }
        }
        if (state.get(SOUTH)) {
            if (pipe != null && pipe.isExtracting(Direction.SOUTH)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_SOUTH);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_SOUTH);
            }
        }
        if (state.get(NORTH)) {
            if (pipe != null && pipe.isExtracting(Direction.NORTH)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_NORTH);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_NORTH);
            }
        }
        if (state.get(EAST)) {
            if (pipe != null && pipe.isExtracting(Direction.EAST)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_EAST);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_EAST);
            }
        }
        if (state.get(WEST)) {
            if (pipe != null && pipe.isExtracting(Direction.WEST)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_WEST);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_WEST);
            }
        }
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (context instanceof EntitySelectionContext) {
            EntitySelectionContext ctx = (EntitySelectionContext) context;
            if (ctx.getEntity() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) ctx.getEntity();
                if (player.world.isRemote) {
                    return getSelectionShape(state, worldIn, pos, player);
                }
            }
        }
        return getShape(worldIn, pos, state, true);
    }

    public VoxelShape getSelectionShape(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        Pair<Direction, VoxelShape> selection = getSelection(state, world, pos, player);

        if (selection.getKey() == null) {
            return getShape(world, pos, state, true);
        }

        if (world.getBlockState(pos.offset(selection.getKey())).getBlock() == this) {
            if (!WrenchItem.isHoldingWrench(player)) {
                return getShape(world, pos, state, true);
            }
        }

        return selection.getValue();
    }

    private static final List<Triple<VoxelShape, BooleanProperty, Direction>> SHAPES = Arrays.asList(
            new Triple<>(SHAPE_NORTH, NORTH, Direction.NORTH),
            new Triple<>(SHAPE_SOUTH, SOUTH, Direction.SOUTH),
            new Triple<>(SHAPE_WEST, WEST, Direction.WEST),
            new Triple<>(SHAPE_EAST, EAST, Direction.EAST),
            new Triple<>(SHAPE_UP, UP, Direction.UP),
            new Triple<>(SHAPE_DOWN, DOWN, Direction.DOWN)
    );

    private static final List<Pair<VoxelShape, Direction>> EXTRACT_SHAPES = Arrays.asList(
            new Pair<>(SHAPE_EXTRACT_NORTH, Direction.NORTH),
            new Pair<>(SHAPE_EXTRACT_SOUTH, Direction.SOUTH),
            new Pair<>(SHAPE_EXTRACT_WEST, Direction.WEST),
            new Pair<>(SHAPE_EXTRACT_EAST, Direction.EAST),
            new Pair<>(SHAPE_EXTRACT_UP, Direction.UP),
            new Pair<>(SHAPE_EXTRACT_DOWN, Direction.DOWN)
    );

    private Pair<Direction, VoxelShape> getSelection(BlockState state, IBlockReader blockReader, BlockPos pos, PlayerEntity player) {
        Vector3d start = player.getEyePosition(1F);
        Vector3d end = start.add(player.getLookVec().normalize().scale(getBlockReachDistance(player)));

        Direction direction = null;
        VoxelShape selection = null;
        double shortest = Double.MAX_VALUE;

        double d = checkShape(state, blockReader, pos, start, end, SHAPE_CORE, null);
        if (d < shortest) {
            shortest = d;
        }

        if (!(blockReader instanceof IWorldReader)) {
            return new Pair<>(direction, selection);
        }

        PipeTileEntity pipe = getTileEntity((IWorldReader) blockReader, pos);

        for (int i = 0; i < Direction.values().length; i++) {
            Pair<VoxelShape, Direction> extract = EXTRACT_SHAPES.get(i);
            Triple<VoxelShape, BooleanProperty, Direction> shape = SHAPES.get(i);
            if (pipe != null && pipe.isExtracting(extract.getValue())) {
                d = checkShape(state, blockReader, pos, start, end, extract.getKey(), pipe, extract.getValue());
                if (d < shortest) {
                    shortest = d;
                    direction = extract.getValue();
                    selection = extract.getKey();
                }
            } else {
                d = checkShape(state, blockReader, pos, start, end, shape.getValue1(), shape.getValue2());
                if (d < shortest) {
                    shortest = d;
                    direction = shape.getValue3();
                    selection = shape.getValue1();
                }
            }
        }
        return new Pair<>(direction, selection);
    }

    public float getBlockReachDistance(PlayerEntity player) {
        float distance = (float) player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue();
        return player.isCreative() ? distance : distance - 0.5F;
    }

    private double checkShape(BlockState state, IBlockReader world, BlockPos pos, Vector3d start, Vector3d end, VoxelShape shape, BooleanProperty direction) {
        if (direction != null && !state.get(direction)) {
            return Double.MAX_VALUE;
        }
        BlockRayTraceResult blockRayTraceResult = world.rayTraceBlocks(start, end, pos, shape, state);
        if (blockRayTraceResult == null) {
            return Double.MAX_VALUE;
        }
        return blockRayTraceResult.getHitVec().distanceTo(start);
    }

    private double checkShape(BlockState state, IBlockReader world, BlockPos pos, Vector3d start, Vector3d end, VoxelShape shape, @Nullable PipeTileEntity pipe, Direction side) {
        if (pipe != null && !pipe.isExtracting(side)) {
            return Double.MAX_VALUE;
        }
        BlockRayTraceResult blockRayTraceResult = world.rayTraceBlocks(start, end, pos, shape, state);
        if (blockRayTraceResult == null) {
            return Double.MAX_VALUE;
        }
        return blockRayTraceResult.getHitVec().distanceTo(start);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.isIn(newState.getBlock())) {
            if (!newState.get(HAS_DATA)) {
                worldIn.removeTileEntity(pos);
            }
        } else {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getShape(worldIn, pos, state, false);
    }

    @Override
    public VoxelShape getRayTraceShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return getShape(reader, pos, state, false);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getShape(worldIn, pos, state, false);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return getShape(reader, pos, state, false);
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getShape(worldIn, pos, state, false);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if (state.get(HAS_DATA)) {
            return createTileEntity();
        } else {
            return null;
        }
    }

    abstract TileEntity createTileEntity();

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.get(HAS_DATA);
    }

}
