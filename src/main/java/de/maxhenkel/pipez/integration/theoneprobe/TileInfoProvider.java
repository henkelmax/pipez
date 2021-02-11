package de.maxhenkel.pipez.integration.theoneprobe;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.tileentity.ItemPipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class TileInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return Main.MODID + ":probeinfoprovider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, PlayerEntity player, World world, BlockState state, IProbeHitData hitData) {
        TileEntity te = world.getTileEntity(hitData.getPos());

        if (state.getBlock() instanceof PipeBlock) {
            PipeBlock pipe = (PipeBlock) state.getBlock();
            Direction selectedSide = pipe.getSelection(state, world, hitData.getPos(), player).getKey();
            if (selectedSide == null) {
                return;
            }
            if (!(te instanceof UpgradeTileEntity)) {
                return;
            }

            UpgradeTileEntity<?> upgradeTileEntity = (UpgradeTileEntity<?>) te;

            ItemStack upgrade = upgradeTileEntity.getUpgradeInventory().getStackInSlot(selectedSide.getIndex());
            if (!upgrade.isEmpty()) {
                if (upgradeTileEntity instanceof ItemPipeTileEntity) {
                    ItemPipeTileEntity itemPipe = (ItemPipeTileEntity) upgradeTileEntity;
                    info.horizontal().item(upgrade).vertical().itemLabel(upgrade).text(new TranslationTextComponent("tooltip.pipez.item_rate", itemPipe.getRate(selectedSide), itemPipe.getSpeed(selectedSide)));
                } else {
                    info.horizontal().item(upgrade).vertical().itemLabel(upgrade).text(new TranslationTextComponent("tooltip.pipez.rate", upgradeTileEntity.getRate(selectedSide)));
                }
            } else {
                info.text(new TranslationTextComponent("tooltip.pipez.no_upgrade"));
            }
        }
    }
}
