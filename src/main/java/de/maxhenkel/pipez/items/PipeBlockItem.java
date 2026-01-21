package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.tileentity.types.GasPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.utils.MekanismUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class PipeBlockItem extends BlockItem {

    private final PipeType<?, ?>[] pipeTypes;

    public PipeBlockItem(Block block, Properties properties, PipeType<?, ?>... pipeTypes) {
        super(block, properties);
        this.pipeTypes = pipeTypes;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);

        for (PipeType<?, ?> pipeType : pipeTypes) {
            if (pipeType == GasPipeType.INSTANCE && !MekanismUtils.isMekanismInstalled()) {
                continue;
            }
            tooltip.add(pipeType.getTransferText(null).copy().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!Main.SERVER_CONFIG.pipeNetworkLimitEnabled.get()) {
            return super.place(context);
        }

        Level level = context.getLevel();
        if (level.isClientSide) {
            return super.place(context);
        }

        Block block = getBlock();
        if (!(block instanceof PipeBlock pipeBlock)) {
            return super.place(context);
        }

        BlockPos placePos = context.getClickedPos();
        if (!level.getBlockState(placePos).canBeReplaced(context)) {
            placePos = placePos.relative(context.getClickedFace());
        }

        int maxSize = Main.SERVER_CONFIG.pipeNetworkMaxSize.get();
        int networkSize = countConnectedNetworkSize(level, placePos, pipeBlock);

        if (context.getPlayer() instanceof ServerPlayer player) {
            MutableComponent message;
            if (networkSize + 1 > maxSize) {
                message = Component.translatable("message.pipez.network_size_limit", networkSize + 1, maxSize)
                        .withStyle(ChatFormatting.RED);
                player.displayClientMessage(message, true);
                return InteractionResult.FAIL;
            } else {
                ChatFormatting color;
                float ratio = (float) (networkSize + 1) / maxSize;
                if (ratio < 0.5f) {
                    color = ChatFormatting.GREEN;
                } else if (ratio < 0.75f) {
                    color = ChatFormatting.YELLOW;
                } else if (ratio < 1.0f) {
                    color = ChatFormatting.GOLD;
                } else {
                    color = ChatFormatting.RED;
                }
                message = Component.translatable("message.pipez.network_size", networkSize + 1, maxSize)
                        .withStyle(color);
                player.displayClientMessage(message, true);
            }
        }

        return super.place(context);
    }

    private int countConnectedNetworkSize(Level level, BlockPos placePos, PipeBlock pipeBlock) {
        Set<BlockPos> visitedNetworks = new HashSet<>();
        int totalSize = 0;

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = placePos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.getBlock().equals(pipeBlock) && !visitedNetworks.contains(neighborPos)) {
                int networkSize = countNetworkPipes(level, neighborPos, pipeBlock, visitedNetworks);
                totalSize += networkSize;
            }
        }

        return totalSize;
    }

    private int countNetworkPipes(Level level, BlockPos startPos, PipeBlock pipeBlock, Set<BlockPos> visitedNetworks) {
        Set<BlockPos> visited = new HashSet<>();
        LinkedList<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            BlockState currentState = level.getBlockState(current);

            if (!(currentState.getBlock() instanceof PipeBlock)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (!visited.contains(neighbor)) {
                    BlockState neighborState = level.getBlockState(neighbor);
                    if (neighborState.getBlock().equals(pipeBlock)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        visitedNetworks.addAll(visited);
        return visited.size();
    }

}
