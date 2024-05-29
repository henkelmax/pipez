package de.maxhenkel.pipez.events;

import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.items.FilterDestinationToolItem;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class BlockEvents {

    @SubscribeEvent
    public void onBlockClick(PlayerInteractEvent.RightClickBlock event) {
        onDestinationToolClick(event);
        onPipeClick(event);
    }

    private void onPipeClick(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof PipeBlock)) {
            return;
        }

        PipeBlock pipe = (PipeBlock) state.getBlock();

        Direction side = pipe.getSelection(state, event.getLevel(), event.getPos(), event.getEntity()).getKey();
        InteractionResult result = pipe.onPipeSideForceActivated(state, event.getLevel(), event.getPos(), event.getEntity(), event.getHand(), event.getHitVec(), side);
        if (result.consumesAction()) {
            event.setUseItem(TriState.TRUE);
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private void onDestinationToolClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldItem = event.getEntity().getItemInHand(event.getHand());
        if (!(heldItem.getItem() instanceof FilterDestinationToolItem)) {
            return;
        }

        BlockEntity te = event.getLevel().getBlockEntity(event.getPos());

        if (te == null) {
            return;
        }

        BlockState blockState = event.getLevel().getBlockState(event.getPos());
        if (blockState.getBlock() instanceof PipeBlock) {
            return;
        }

        FilterDestinationToolItem.setDestination(heldItem, new DirectionalPosition(event.getPos().immutable(), event.getFace()));
        event.getEntity().displayClientMessage(Component.translatable("message.pipez.filter_destination_tool.destination.set"), true);
        event.setUseItem(TriState.TRUE);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

}
