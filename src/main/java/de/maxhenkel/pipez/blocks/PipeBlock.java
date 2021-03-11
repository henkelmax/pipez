package de.maxhenkel.pipez.blocks;

import de.maxhenkel.corelib.block.IItemBlock;
import de.maxhenkel.corelib.block.VoxelUtils;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.corelib.helpers.Triple;
import de.maxhenkel.pipez.ModItemGroups;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.items.UpgradeItem;
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
import net.minecraft.item.ItemStack;
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
        super(Block.Properties.of(Material.METAL, MaterialColor.COLOR_GRAY).strength(0.5F).sound(SoundType.METAL));

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
        return new BlockItem(this, new Item.Properties().tab(ModItemGroups.TAB_PIPEZ)).setRegistryName(getRegistryName());
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        Direction side = getSelection(state, worldIn, pos, player).getKey();
        if (side != null) {
            return onPipeSideActivated(state, worldIn, pos, player, handIn, hit, side);
        } else {
            return super.use(state, worldIn, pos, player, handIn, hit);
        }
    }

    public ActionResultType onWrenchClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, Direction side) {
        if (!player.isShiftKeyDown()) {
            return ActionResultType.PASS;
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
        return ActionResultType.SUCCESS;
    }

    public ActionResultType onPipeSideActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, Direction direction) {
        return super.use(state, worldIn, pos, player, handIn, hit);
    }

    public ActionResultType onPipeSideForceActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit, @Nullable Direction side) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (WrenchItem.isWrench(heldItem)) {
            return onWrenchClicked(state, world, pos, player, hand, hit, side);
        } else if (heldItem.getItem() instanceof UpgradeItem && player.isShiftKeyDown() && side != null) {
            TileEntity te = world.getBlockEntity(pos);
            if (!(te instanceof UpgradeTileEntity)) {
                return ActionResultType.PASS;
            }
            UpgradeTileEntity upgradeTe = (UpgradeTileEntity) te;
            ItemStack oldUpgrade;
            if (player.abilities.instabuild) {
                oldUpgrade = upgradeTe.setUpgradeItem(side, heldItem.copy().split(1));
            } else {
                oldUpgrade = upgradeTe.setUpgradeItem(side, heldItem.split(1));
            }
            if (heldItem.isEmpty()) {
                player.setItemInHand(hand, oldUpgrade);
            } else {
                if (!player.inventory.add(oldUpgrade)) {
                    player.drop(oldUpgrade, true);
                }
            }
            return ActionResultType.sidedSuccess(world.isClientSide);
        }

        return ActionResultType.PASS;
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
        world.setBlockAndUpdate(pos, blockState.setValue(HAS_DATA, hasData));
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
            } else {
                BlockState blockState = world.getBlockState(pos);
                BooleanProperty sideProperty = getProperty(side);
                boolean connected = blockState.getValue(sideProperty);
                world.setBlockAndUpdate(pos, blockState.setValue(sideProperty, !connected));
                world.setBlockAndUpdate(pos, blockState.setValue(sideProperty, connected));
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
    public PipeTileEntity getTileEntity(IWorldReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof PipeTileEntity) {
            return (PipeTileEntity) te;
        }
        return null;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getState(context.getLevel(), context.getClickedPos(), null);
    }

    private BlockState getState(World world, BlockPos pos, @Nullable BlockState oldState) {
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

    public boolean isConnected(IWorldReader world, BlockPos pos, Direction facing) {
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

    public boolean isAbleToConnect(IWorldReader world, BlockPos pos, Direction facing) {
        return isPipe(world, pos, facing) || canConnectTo(world, pos, facing);
    }

    public abstract boolean canConnectTo(IWorldReader world, BlockPos pos, Direction facing);

    public abstract boolean isPipe(IWorldReader world, BlockPos pos, Direction facing);

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos pos1, boolean b) {
        super.neighborChanged(state, world, pos, block, pos1, b);
        BlockState newState = getState(world, pos, state);
        if (!state.getProperties().stream().allMatch(property -> state.getValue(property).equals(newState.getValue(property)))) {
            world.setBlockAndUpdate(pos, newState);
            PipeTileEntity.markPipesDirty(world, pos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, HAS_DATA, WATERLOGGED);
    }

    public static final VoxelShape SHAPE_NORTH = Block.box(5D, 5D, 5D, 11D, 11D, 0D);
    public static final VoxelShape SHAPE_SOUTH = Block.box(5D, 5D, 11D, 11D, 11D, 16D);
    public static final VoxelShape SHAPE_EAST = Block.box(11D, 5D, 5D, 16D, 11D, 11D);
    public static final VoxelShape SHAPE_WEST = Block.box(5D, 5D, 5D, 0D, 11D, 11D);
    public static final VoxelShape SHAPE_UP = Block.box(5D, 11D, 5D, 11D, 16D, 11D);
    public static final VoxelShape SHAPE_DOWN = Block.box(5D, 5D, 5D, 11D, 0D, 11D);
    public static final VoxelShape SHAPE_CORE = Block.box(5D, 5D, 5D, 11D, 11D, 11D);
    public static final VoxelShape SHAPE_EXTRACT_NORTH = VoxelUtils.combine(SHAPE_NORTH, Block.box(4D, 4D, 1D, 12D, 12D, 0D));
    public static final VoxelShape SHAPE_EXTRACT_SOUTH = VoxelUtils.combine(SHAPE_SOUTH, Block.box(4D, 4D, 15D, 12D, 12D, 16D));
    public static final VoxelShape SHAPE_EXTRACT_EAST = VoxelUtils.combine(SHAPE_EAST, Block.box(15D, 4D, 4D, 16D, 12D, 12D));
    public static final VoxelShape SHAPE_EXTRACT_WEST = VoxelUtils.combine(SHAPE_WEST, Block.box(1D, 4D, 4D, 0D, 12D, 12D));
    public static final VoxelShape SHAPE_EXTRACT_UP = VoxelUtils.combine(SHAPE_UP, Block.box(4D, 15D, 4D, 12D, 16D, 12D));
    public static final VoxelShape SHAPE_EXTRACT_DOWN = VoxelUtils.combine(SHAPE_DOWN, Block.box(4D, 1D, 4D, 12D, 0D, 12D));

    public VoxelShape getShape(IBlockReader blockReader, BlockPos pos, BlockState state, boolean advanced) {
        PipeTileEntity pipe = null;
        if (advanced && blockReader instanceof IWorldReader) {
            pipe = getTileEntity((IWorldReader) blockReader, pos);
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
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (context instanceof EntitySelectionContext) {
            EntitySelectionContext ctx = (EntitySelectionContext) context;
            if (ctx.getEntity() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) ctx.getEntity();
                if (player.level.isClientSide) {
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

    public Pair<Direction, VoxelShape> getSelection(BlockState state, IBlockReader blockReader, BlockPos pos, PlayerEntity player) {
        Vector3d start = player.getEyePosition(1F);
        Vector3d end = start.add(player.getLookAngle().normalize().scale(getBlockReachDistance(player)));

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
        if (direction != null && !state.getValue(direction)) {
            return Double.MAX_VALUE;
        }
        BlockRayTraceResult blockRayTraceResult = world.clipWithInteractionOverride(start, end, pos, shape, state);
        if (blockRayTraceResult == null) {
            return Double.MAX_VALUE;
        }
        return blockRayTraceResult.getLocation().distanceTo(start);
    }

    private double checkShape(BlockState state, IBlockReader world, BlockPos pos, Vector3d start, Vector3d end, VoxelShape shape, @Nullable PipeTileEntity pipe, Direction side) {
        if (pipe != null && !pipe.isExtracting(side)) {
            return Double.MAX_VALUE;
        }
        BlockRayTraceResult blockRayTraceResult = world.clipWithInteractionOverride(start, end, pos, shape, state);
        if (blockRayTraceResult == null) {
            return Double.MAX_VALUE;
        }
        return blockRayTraceResult.getLocation().distanceTo(start);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            if (!newState.getValue(HAS_DATA)) {
                worldIn.removeBlockEntity(pos);
            }
        } else {
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getShape(worldIn, pos, state, false);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return getShape(reader, pos, state, false);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getShape(worldIn, pos, state, false);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return getShape(reader, pos, state, false);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getShape(worldIn, pos, state, false);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader worldIn) {
        return null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if (state.getValue(HAS_DATA)) {
            return createTileEntity();
        } else {
            return null;
        }
    }

    abstract TileEntity createTileEntity();

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(HAS_DATA);
    }

}
