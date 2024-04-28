package de.maxhenkel.pipez.integration.theoneprobe;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileInfoProvider implements IProbeInfoProvider {

    public static final ResourceLocation ID = new ResourceLocation(Main.MODID, "probeinfoprovider");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player player, Level world, BlockState state, IProbeHitData hitData) {
        BlockEntity te = world.getBlockEntity(hitData.getPos());

        if (state.getBlock() instanceof PipeBlock) {
            PipeBlock pipe = (PipeBlock) state.getBlock();
            Direction selectedSide = pipe.getSelection(state, world, hitData.getPos(), player).getKey();
            if (selectedSide == null) {
                return;
            }
            if (!(te instanceof PipeLogicTileEntity)) {
                return;
            }

            PipeLogicTileEntity pipeTile = (PipeLogicTileEntity) te;

            if (!pipeTile.isExtracting(selectedSide)) {
                return;
            }

            ItemStack upgrade = pipeTile.getUpgradeItem(selectedSide);

            IProbeInfo i;
            if (upgrade.isEmpty()) {
                i = info.text(Component.translatable("tooltip.pipez.no_upgrade"));
            } else {
                i = info.horizontal()
                        .item(upgrade)
                        .vertical()
                        .itemLabel(upgrade);
            }
            for (PipeType<?, ?> type : pipeTile.getPipeTypes()) {
                if (pipeTile.isEnabled(selectedSide, type)) {
                    i = i.text(type.getTransferText(pipeTile.getUpgrade(selectedSide)));
                }
            }
        }
    }
}
