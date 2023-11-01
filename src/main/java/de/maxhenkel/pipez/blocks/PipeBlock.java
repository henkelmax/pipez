package de.maxhenkel.pipez.blocks;

import de.maxhenkel.corelib.block.IItemBlock;
import de.maxhenkel.corelib.block.VoxelUtils;
import de.maxhenkel.corelib.blockentity.SimpleBlockEntityTicker;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.corelib.helpers.Triple;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.items.UpgradeItem;
import de.maxhenkel.pipez.items.WrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForgeMod;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public abstract class PipeBlock extends Block implements IItemBlock, SimpleWaterloggedBlock, EntityBlock {

    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty HAS_DATA = BooleanProperty.create("has_data");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected PipeBlock() {
        super(Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.5F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK));

        registerDefaultState(stateDefinition.any()
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(HAS_DATA, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public Item toItem() {
        return new BlockItem(this, new Item.Properties());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return new SimpleBlockEntityTicker<>();
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        Direction side = getSelection(state, worldIn, pos, player).getKey();
        if (side != null) {
            return onPipeSideActivated(state, worldIn, pos, player, handIn, hit, side);
        } else {
            return super.use(state, worldIn, pos, player, handIn, hit);
        }
    }

    public InteractionResult onWrenchClicked(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, Direction side) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (side != null) {
            if (worldIn.getBlockState(pos.relative(side)).getBlock() != this) {
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
            side = hit.getDirection();
            if (worldIn.getBlockState(pos.relative(side)).getBlock() != this) {
                setExtracting(worldIn, pos, side, false);
                if (isAbleToConnect(worldIn, pos, side)) {
                    setDisconnected(worldIn, pos, side, false);
                }
            } else {
                setDisconnected(worldIn, pos, side, false);
                setDisconnected(worldIn, pos.relative(side), side.getOpposite(), false);
            }
        }

        PipeTileEntity.markPipesDirty(worldIn, pos);
        return InteractionResult.SUCCESS;
    }

    public InteractionResult onPipeSideActivated(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, Direction direction) {
        return super.use(state, worldIn, pos, player, handIn, hit);
    }

    public InteractionResult onPipeSideForceActivated(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, @Nullable Direction side) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (WrenchItem.isWrench(heldItem)) {
            return onWrenchClicked(state, world, pos, player, hand, hit, side);
        } else if (heldItem.getItem() instanceof UpgradeItem && player.isShiftKeyDown() && side != null) {
            BlockEntity te = world.getBlockEntity(pos);
            if (!(te instanceof UpgradeTileEntity upgradeTe)) {
                return InteractionResult.PASS;
            }
            if (!upgradeTe.isExtracting(side)) {
                return InteractionResult.PASS;
            }
            ItemStack oldUpgrade;
            if (player.getAbilities().instabuild) {
                oldUpgrade = upgradeTe.setUpgradeItem(side, heldItem.copy().split(1));
            } else {
                oldUpgrade = upgradeTe.setUpgradeItem(side, heldItem.split(1));
            }
            if (heldItem.isEmpty()) {
                player.setItemInHand(hand, oldUpgrade);
            } else {
                if (!player.getInventory().add(oldUpgrade)) {
                    player.drop(oldUpgrade, true);
                }
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
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

    public boolean isExtracting(LevelAccessor world, BlockPos pos, Direction side) {
        PipeTileEntity pipe = getTileEntity(world, pos);
        if (pipe == null) {
            return false;
        }
        return pipe.isExtracting(side);
    }

    public boolean isDisconnected(LevelAccessor world, BlockPos pos, Direction side) {
        PipeTileEntity pipe = getTileEntity(world, pos);
        if (pipe == null) {
            return false;
        }
        return pipe.isDisconnected(side);
    }

    public void setHasData(Level world, BlockPos pos, boolean hasData) {
        BlockState blockState = world.getBlockState(pos);
        world.setBlockAndUpdate(pos, blockState.setValue(HAS_DATA, hasData));
        if (!hasData) {
//            world.removeBlockEntity(pos);
        }
    }

    public void setExtracting(Level world, BlockPos pos, Direction side, boolean extracting) {
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
        BlockState blockState = world.getBlockState(pos);
        BooleanProperty sideProperty = getProperty(side);
        boolean connected = blockState.getValue(sideProperty);
        world.setBlockAndUpdate(pos, blockState.setValue(sideProperty, !connected));
        world.setBlockAndUpdate(pos, blockState.setValue(sideProperty, connected));
    }

    public void setDisconnected(Level world, BlockPos pos, Direction side, boolean disconnected) {
        PipeTileEntity pipe = getTileEntity(world, pos);
        if (pipe == null) {
            if (disconnected) {
                setHasData(world, pos, true);
                pipe = getTileEntity(world, pos);
                if (pipe != null) {
                    pipe.setDisconnected(side, disconnected);
                    world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(getProperty(side), false));
                }
            }
        } else {
            pipe.setDisconnected(side, disconnected);
            if (!pipe.hasReasonToStay()) {
                setHasData(world, pos, false);
            }
            world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(getProperty(side), !disconnected));
        }
    }

    @Nullable
    public PipeTileEntity getTileEntity(LevelAccessor world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof PipeTileEntity) {
            return (PipeTileEntity) te;
        }
        return null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getState(context.getLevel(), context.getClickedPos(), null);
    }

    private BlockState getState(Level world, BlockPos pos, @Nullable BlockState oldState) {
        FluidState fluidState = world.getFluidState(pos);
        boolean hasData = false;
        if (oldState != null && oldState.getBlock() == this) {
            hasData = oldState.getValue(HAS_DATA);
        }
        return defaultBlockState()
                .setValue(UP, isConnected(world, pos, Direction.UP))
                .setValue(DOWN, isConnected(world, pos, Direction.DOWN))
                .setValue(NORTH, isConnected(world, pos, Direction.NORTH))
                .setValue(SOUTH, isConnected(world, pos, Direction.SOUTH))
                .setValue(EAST, isConnected(world, pos, Direction.EAST))
                .setValue(WEST, isConnected(world, pos, Direction.WEST))
                .setValue(HAS_DATA, hasData)
                .setValue(WATERLOGGED, fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8);
    }

    public boolean isConnected(LevelAccessor world, BlockPos pos, Direction facing) {
        PipeTileEntity pipe = getTileEntity(world, pos);
        PipeTileEntity other = getTileEntity(world, pos.relative(facing));

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

    public boolean isAbleToConnect(LevelAccessor world, BlockPos pos, Direction facing) {
        return isPipe(world, pos, facing) || canConnectTo(world, pos, facing);
    }

    public abstract boolean canConnectTo(LevelAccessor world, BlockPos pos, Direction facing);

    public abstract boolean isPipe(LevelAccessor world, BlockPos pos, Direction facing);

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos pos1, boolean b) {
        super.neighborChanged(state, world, pos, block, pos1, b);
        BlockState newState = getState(world, pos, state);
        if (!state.getProperties().stream().allMatch(property -> state.getValue(property).equals(newState.getValue(property)))) {
            world.setBlockAndUpdate(pos, newState);
            PipeTileEntity.markPipesDirty(world, pos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, HAS_DATA, WATERLOGGED);
    }

    public static final VoxelShape SHAPE_NORTH = Block.box(5D, 5D, 0D, 11D, 11D, 5D);
    public static final VoxelShape SHAPE_SOUTH = Block.box(5D, 5D, 11D, 11D, 11D, 16D);
    public static final VoxelShape SHAPE_EAST = Block.box(11D, 5D, 5D, 16D, 11D, 11D);
    public static final VoxelShape SHAPE_WEST = Block.box(0D, 5D, 5D, 5D, 11D, 11D);
    public static final VoxelShape SHAPE_UP = Block.box(5D, 11D, 5D, 11D, 16D, 11D);
    public static final VoxelShape SHAPE_DOWN = Block.box(5D, 0D, 5D, 11D, 5D, 11D);
    public static final VoxelShape SHAPE_CORE = Block.box(5D, 5D, 5D, 11D, 11D, 11D);
    public static final VoxelShape SHAPE_EXTRACT_NORTH = VoxelUtils.combine(SHAPE_NORTH, Block.box(4D, 4D, 0D, 12D, 12D, 1D));
    public static final VoxelShape SHAPE_EXTRACT_SOUTH = VoxelUtils.combine(SHAPE_SOUTH, Block.box(4D, 4D, 15D, 12D, 12D, 16D));
    public static final VoxelShape SHAPE_EXTRACT_EAST = VoxelUtils.combine(SHAPE_EAST, Block.box(15D, 4D, 4D, 16D, 12D, 12D));
    public static final VoxelShape SHAPE_EXTRACT_WEST = VoxelUtils.combine(SHAPE_WEST, Block.box(0D, 4D, 4D, 1D, 12D, 12D));
    public static final VoxelShape SHAPE_EXTRACT_UP = VoxelUtils.combine(SHAPE_UP, Block.box(4D, 15D, 4D, 12D, 16D, 12D));
    public static final VoxelShape SHAPE_EXTRACT_DOWN = VoxelUtils.combine(SHAPE_DOWN, Block.box(4D, 0D, 4D, 12D, 1D, 12D));

    public VoxelShape getShape(BlockGetter blockReader, BlockPos pos, BlockState state, boolean advanced) {
        PipeTileEntity pipe = null;
        if (advanced && blockReader instanceof LevelAccessor) {
            pipe = getTileEntity((LevelAccessor) blockReader, pos);
        }

        VoxelShape shape = SHAPE_CORE;
        if (state.getValue(UP)) {
            if (pipe != null && pipe.isExtracting(Direction.UP)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_UP);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_UP);
            }
        }
        if (state.getValue(DOWN)) {
            if (pipe != null && pipe.isExtracting(Direction.DOWN)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_DOWN);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_DOWN);
            }
        }
        if (state.getValue(SOUTH)) {
            if (pipe != null && pipe.isExtracting(Direction.SOUTH)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_SOUTH);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_SOUTH);
            }
        }
        if (state.getValue(NORTH)) {
            if (pipe != null && pipe.isExtracting(Direction.NORTH)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_NORTH);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_NORTH);
            }
        }
        if (state.getValue(EAST)) {
            if (pipe != null && pipe.isExtracting(Direction.EAST)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_EAST);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_EAST);
            }
        }
        if (state.getValue(WEST)) {
            if (pipe != null && pipe.isExtracting(Direction.WEST)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_WEST);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_WEST);
            }
        }
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext) {
            EntityCollisionContext ctx = (EntityCollisionContext) context;
            if (ctx.getEntity() instanceof Player player) {
                if (player.level().isClientSide) {
                    return getSelectionShape(state, worldIn, pos, player);
                }
            }
        }
        return getShape(worldIn, pos, state, true);
    }

    public VoxelShape getSelectionShape(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        Pair<Direction, VoxelShape> selection = getSelection(state, world, pos, player);

        if (selection.getKey() == null) {
            return getShape(world, pos, state, true);
        }

        if (world.getBlockState(pos.relative(selection.getKey())).getBlock() == this) {
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

    public Pair<Direction, VoxelShape> getSelection(BlockState state, BlockGetter blockReader, BlockPos pos, Player player) {
        Vec3 start = player.getEyePosition(1F);
        Vec3 end = start.add(player.getLookAngle().normalize().scale(getBlockReachDistance(player)));

        Direction direction = null;
        VoxelShape selection = null;
        double shortest = Double.MAX_VALUE;

        double d = checkShape(state, blockReader, pos, start, end, SHAPE_CORE, null);
        if (d < shortest) {
            shortest = d;
        }

        if (!(blockReader instanceof LevelAccessor)) {
            return new Pair<>(direction, selection);
        }

        PipeTileEntity pipe = getTileEntity((LevelAccessor) blockReader, pos);

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

    public float getBlockReachDistance(Player player) {
        float distance = (float) player.getAttribute(NeoForgeMod.BLOCK_REACH.get()).getValue();
        return player.isCreative() ? distance : distance - 0.5F;
    }

    private double checkShape(BlockState state, BlockGetter world, BlockPos pos, Vec3 start, Vec3 end, VoxelShape shape, BooleanProperty direction) {
        if (direction != null && !state.getValue(direction)) {
            return Double.MAX_VALUE;
        }
        BlockHitResult blockRayTraceResult = world.clipWithInteractionOverride(start, end, pos, shape, state);
        if (blockRayTraceResult == null) {
            return Double.MAX_VALUE;
        }
        return blockRayTraceResult.getLocation().distanceTo(start);
    }

    private double checkShape(BlockState state, BlockGetter world, BlockPos pos, Vec3 start, Vec3 end, VoxelShape shape, @Nullable PipeTileEntity pipe, Direction side) {
        if (pipe != null && !pipe.isExtracting(side)) {
            return Double.MAX_VALUE;
        }
        BlockHitResult blockRayTraceResult = world.clipWithInteractionOverride(start, end, pos, shape, state);
        if (blockRayTraceResult == null) {
            return Double.MAX_VALUE;
        }
        return blockRayTraceResult.getLocation().distanceTo(start);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            if (!newState.getValue(HAS_DATA)) {
                level.removeBlockEntity(pos);
            }
        } else {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof UpgradeTileEntity upgrade) {
                Containers.dropContents(level, pos, upgrade.getUpgradeInventory());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return getShape(worldIn, pos, state, false);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return getShape(reader, pos, state, false);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return getShape(worldIn, pos, state, false);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return getShape(reader, pos, state, false);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return getShape(worldIn, pos, state, false);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(HAS_DATA)) {
            return createTileEntity(pos, state);
        } else {
            return null;
        }
    }

    abstract BlockEntity createTileEntity(BlockPos pos, BlockState state);

}
