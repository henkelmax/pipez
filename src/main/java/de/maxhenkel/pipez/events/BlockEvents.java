package de.maxhenkel.pipez.events;

import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.items.FilterDestinationToolItem;
import de.maxhenkel.pipez.items.WrenchItem;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlockEvents {

    @SubscribeEvent
    public void onBlockClick(PlayerInteractEvent.RightClickBlock event) {
        onDestinationToolClick(event);
        onWrenchPipeClick(event);
    }

    private void onWrenchPipeClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldItem = event.getPlayer().getHeldItem(event.getHand());
        if (!WrenchItem.isWrench(heldItem)) {
            return;
        }
        BlockState state = event.getWorld().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof PipeBlock)) {
            return;
        }

        PipeBlock pipe = (PipeBlock) state.getBlock();
        ActionResultType result = pipe.onWrenchClicked(state, event.getWorld(), event.getPos(), event.getPlayer(), event.getHand(), event.getHitVec());

        if (result.isSuccessOrConsume()) {
            event.setUseItem(Event.Result.ALLOW);
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private void onDestinationToolClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldItem = event.getPlayer().getHeldItem(event.getHand());
        if (!(heldItem.getItem() instanceof FilterDestinationToolItem)) {
            return;
        }

        TileEntity te = event.getWorld().getTileEntity(event.getPos());

        if (te == null) {
            return;
        }

        BlockState blockState = event.getWorld().getBlockState(event.getPos());
        if (blockState.getBlock() instanceof PipeBlock) {
            return;
        }

        FilterDestinationToolItem.setDestination(heldItem, new DirectionalPosition(event.getPos().toImmutable(), event.getFace()));
        event.getPlayer().sendStatusMessage(new TranslationTextComponent("message.pipez.filter_destination_tool.destination.set"), true);
        event.setUseItem(Event.Result.ALLOW);
        event.setCancellationResult(ActionResultType.SUCCESS);
        event.setCanceled(true);
    }

}
