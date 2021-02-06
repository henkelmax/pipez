package de.maxhenkel.pipez.blocks;

import de.maxhenkel.pipez.ModItemGroups;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.ItemPipeTileEntity;
import de.maxhenkel.corelib.block.IItemBlock;
import de.maxhenkel.corelib.block.VoxelUtils;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.corelib.helpers.Triple;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
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
    public static final BooleanProperty EXTRACT_DOWN = BooleanProperty.create("extract_down");
    public static final BooleanProperty EXTRACT_UP = BooleanProperty.create("extract_up");
    public static final BooleanProperty EXTRACT_NORTH = BooleanProperty.create("extract_north");
    public static final BooleanProperty EXTRACT_SOUTH = BooleanProperty.create("extract_south");
    public static final BooleanProperty EXTRACT_WEST = BooleanProperty.create("extract_west");
    public static final BooleanProperty EXTRACT_EAST = BooleanProperty.create("extract_east");
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
                .with(EXTRACT_UP, false)
                .with(EXTRACT_DOWN, false)
                .with(EXTRACT_NORTH, false)
                .with(EXTRACT_SOUTH, false)
                .with(EXTRACT_EAST, false)
                .with(EXTRACT_WEST, false)
                .with(WATERLOGGED, false)
        );
    }

    @Override
    public Item toItem() {
        return new BlockItem(this, new Item.Properties().group(ModItemGroups.TAB_PIPEZ)).setRegistryName(getRegistryName());
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        Direction direction = getSelection(state, worldIn, pos, player).getKey();
        if (direction != null) {
            if (worldIn.getBlockState(pos.offset(direction)).getBlock() != this) {
                if (player.isSneaking()) { //TODO wrench
                    BooleanProperty extractProperty = getExtractProperty(direction);
                    boolean extract = state.get(extractProperty);
                    worldIn.setBlockState(pos, state.with(extractProperty, !extract));
                    PipeTileEntity.markPipesDirty(worldIn, pos);
                    return ActionResultType.SUCCESS;
                } else {
                    return onPipeSideActivated(state, worldIn, pos, player, handIn, hit, direction);
                }
            }
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    public ActionResultType onPipeSideActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, Direction direction) {
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    public BooleanProperty getExtractProperty(Direction direction) {
        switch (direction) {
            case NORTH:
                return EXTRACT_NORTH;
            case SOUTH:
                return EXTRACT_SOUTH;
            case WEST:
                return EXTRACT_WEST;
            case EAST:
                return EXTRACT_EAST;
            case UP:
                return EXTRACT_UP;
            case DOWN:
            default:
                return EXTRACT_DOWN;
        }
    }

    public boolean isExtracting(BlockState state, Direction direction) {
        return state.get(getExtractProperty(direction));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getState(context.getWorld(), context.getPos());
    }

    private BlockState getState(World world, BlockPos pos) {
        FluidState fluidState = world.getFluidState(pos);
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() != this) {
            return getDefaultState()
                    .with(UP, isConnected(world, pos, Direction.UP))
                    .with(DOWN, isConnected(world, pos, Direction.DOWN))
                    .with(NORTH, isConnected(world, pos, Direction.NORTH))
                    .with(SOUTH, isConnected(world, pos, Direction.SOUTH))
                    .with(EAST, isConnected(world, pos, Direction.EAST))
                    .with(WEST, isConnected(world, pos, Direction.WEST))
                    .with(WATERLOGGED, fluidState.isTagged(FluidTags.WATER) && fluidState.getLevel() == 8);
        } else {
            return blockState
                    .with(UP, isConnected(world, pos, Direction.UP))
                    .with(EXTRACT_UP, !isPipe(world, pos, Direction.UP) && blockState.get(EXTRACT_UP))
                    .with(DOWN, isConnected(world, pos, Direction.DOWN))
                    .with(EXTRACT_DOWN, !isPipe(world, pos, Direction.DOWN) && blockState.get(EXTRACT_DOWN))
                    .with(NORTH, isConnected(world, pos, Direction.NORTH))
                    .with(EXTRACT_NORTH, !isPipe(world, pos, Direction.NORTH) && blockState.get(EXTRACT_NORTH))
                    .with(SOUTH, isConnected(world, pos, Direction.SOUTH))
                    .with(EXTRACT_SOUTH, !isPipe(world, pos, Direction.SOUTH) && blockState.get(EXTRACT_SOUTH))
                    .with(EAST, isConnected(world, pos, Direction.EAST))
                    .with(EXTRACT_EAST, !isPipe(world, pos, Direction.EAST) && blockState.get(EXTRACT_EAST))
                    .with(WEST, isConnected(world, pos, Direction.WEST))
                    .with(EXTRACT_WEST, !isPipe(world, pos, Direction.WEST) && blockState.get(EXTRACT_WEST))
                    .with(WATERLOGGED, fluidState.isTagged(FluidTags.WATER) && fluidState.getLevel() == 8);
        }

    }

    public boolean isConnected(IWorldReader world, BlockPos pos, Direction facing) {
        return isPipe(world, pos, facing) || canConnectTo(world, pos, facing);
    }

    public boolean isExtracting(BlockState state) {
        return SHAPES.stream().anyMatch(triple -> state.get(triple.getValue2()) && state.get(getExtractProperty(triple.getValue3())));
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
        BlockState newState = getState(world, pos);
        if (!state.getProperties().stream().allMatch(property -> state.get(property).equals(newState.get(property)))) {
            world.setBlockState(pos, newState);
            PipeTileEntity.markPipesDirty(world, pos);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, EXTRACT_UP, EXTRACT_DOWN, EXTRACT_NORTH, EXTRACT_SOUTH, EXTRACT_EAST, EXTRACT_WEST, WATERLOGGED);
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

    public VoxelShape getShape(BlockState state) {
        VoxelShape shape = SHAPE_CORE;
        if (state.get(UP)) {
            if (state.get(EXTRACT_UP)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_UP);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_UP);
            }
        }
        if (state.get(DOWN)) {
            if (state.get(EXTRACT_DOWN)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_DOWN);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_DOWN);
            }
        }
        if (state.get(SOUTH)) {
            if (state.get(EXTRACT_SOUTH)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_SOUTH);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_SOUTH);
            }
        }
        if (state.get(NORTH)) {
            if (state.get(EXTRACT_NORTH)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_NORTH);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_NORTH);
            }
        }
        if (state.get(EAST)) {
            if (state.get(EXTRACT_EAST)) {
                shape = VoxelUtils.combine(shape, SHAPE_EXTRACT_EAST);
            } else {
                shape = VoxelUtils.combine(shape, SHAPE_EAST);
            }
        }
        if (state.get(WEST)) {
            if (state.get(EXTRACT_WEST)) {
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
        return getShape(state);
    }

    public VoxelShape getSelectionShape(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        Pair<Direction, VoxelShape> selection = getSelection(state, world, pos, player);

        if (selection.getKey() == null) {
            return getShape(state);
        }

        if (world.getBlockState(pos.offset(selection.getKey())).getBlock() == this) {
            return getShape(state);
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

    private static final List<Triple<VoxelShape, BooleanProperty, Direction>> EXTRACT_SHAPES = Arrays.asList(
            new Triple<>(SHAPE_EXTRACT_NORTH, EXTRACT_NORTH, Direction.NORTH),
            new Triple<>(SHAPE_EXTRACT_SOUTH, EXTRACT_SOUTH, Direction.SOUTH),
            new Triple<>(SHAPE_EXTRACT_WEST, EXTRACT_WEST, Direction.WEST),
            new Triple<>(SHAPE_EXTRACT_EAST, EXTRACT_EAST, Direction.EAST),
            new Triple<>(SHAPE_EXTRACT_UP, EXTRACT_UP, Direction.UP),
            new Triple<>(SHAPE_EXTRACT_DOWN, EXTRACT_DOWN, Direction.DOWN)
    );

    private Pair<Direction, VoxelShape> getSelection(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        Vector3d start = player.getEyePosition(1F);
        Vector3d end = start.add(player.getLookVec().normalize().scale(getBlockReachDistance(player)));

        Direction direction = null;
        VoxelShape selection = null;
        double shortest = Double.MAX_VALUE;

        double d = checkShape(state, world, pos, start, end, SHAPE_CORE, null);
        if (d < shortest) {
            shortest = d;
        }
        for (int i = 0; i < SHAPES.size(); i++) {
            Triple<VoxelShape, BooleanProperty, Direction> extract = EXTRACT_SHAPES.get(i);
            Triple<VoxelShape, BooleanProperty, Direction> shape = SHAPES.get(i);
            if (state.get(extract.getValue2())) {
                d = checkShape(state, world, pos, start, end, extract.getValue1(), extract.getValue2());
                if (d < shortest) {
                    shortest = d;
                    direction = extract.getValue3();
                    selection = extract.getValue1();
                }
            } else {
                d = checkShape(state, world, pos, start, end, shape.getValue1(), shape.getValue2());
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

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.isIn(newState.getBlock())) {
            if (isExtracting(state) != isExtracting(newState)) {
                worldIn.removeTileEntity(pos);
                // TODO drop items
            }
        }
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getShape(state);
    }

    @Override
    public VoxelShape getRayTraceShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return getShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return getShape(state);
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getShape(state);
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
        if (isExtracting(state)) {
            return new ItemPipeTileEntity();
        } else {
            return null;
        }
    }

}
