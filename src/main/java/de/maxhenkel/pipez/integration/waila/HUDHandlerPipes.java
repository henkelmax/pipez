package de.maxhenkel.pipez.integration.waila;

import de.maxhenkel.pipez.ClientRegistryUtils;
import de.maxhenkel.pipez.Main;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public class HUDHandlerPipes implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    static final HUDHandlerPipes INSTANCE = new HUDHandlerPipes();

    private static final ResourceLocation UID = new ResourceLocation(Main.MODID, "pipe");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        //TODO Implement once Jade is updated
        /*CompoundTag compound = blockAccessor.getServerData();
        if (compound.contains("Upgrade", Tag.TAG_STRING)) {
            iTooltip.add(Component.Serializer.fromJson(compound.getString("Upgrade"), ClientRegistryUtils.getProvider()));
        }
        iTooltip.addAll(getTooltips(compound));*/
    }

    @Override
    public void appendServerData(CompoundTag compound, BlockAccessor blockAccessor) {
        //TODO Implement once Jade is updated
        /*if (blockAccessor.getBlockState().getBlock() instanceof PipeBlock) {
            BlockEntity te = blockAccessor.getBlockEntity();
            PipeBlock pipe = (PipeBlock) blockAccessor.getBlockState().getBlock();
            Direction selectedSide = pipe.getSelection(te.getBlockState(), blockAccessor.getLevel(), te.getBlockPos(), blockAccessor.getPlayer()).getKey();
            if (selectedSide == null) {
                return;
            }
            if (!(te instanceof UpgradeTileEntity)) {
                return;
            }

            PipeLogicTileEntity pipeTile = (PipeLogicTileEntity) te;

            if (!pipeTile.isExtracting(selectedSide)) {
                return;
            }

            ItemStack upgrade = pipeTile.getUpgradeItem(selectedSide);

            if (upgrade.isEmpty()) {
                compound.putString("Upgrade", Component.Serializer.toJson(Component.translatable("tooltip.pipez.no_upgrade"), ClientRegistryUtils.getProvider()));
            } else {
                compound.putString("Upgrade", Component.Serializer.toJson(upgrade.getHoverName(), ClientRegistryUtils.getProvider()));
            }

            List<Component> tooltips = new ArrayList<>();
            for (PipeType<?, ?> pipeType : pipeTile.getPipeTypes()) {
                if (pipeTile.isEnabled(selectedSide, pipeType)) {
                    tooltips.add(pipeType.getTransferText(pipeTile.getUpgrade(selectedSide)));
                }
            }
            putTooltips(compound, tooltips);
        }*/
    }

    public void putTooltips(CompoundTag compound, List<Component> tooltips) {
        ListTag list = new ListTag();
        for (Component tooltip : tooltips) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(tooltip, ClientRegistryUtils.getProvider())));
        }
        compound.put("Tooltips", list);
    }

    public List<Component> getTooltips(CompoundTag compound) {
        List<Component> tooltips = new ArrayList<>();
        if (!compound.contains("Tooltips", Tag.TAG_LIST)) {
            return tooltips;
        }
        ListTag list = compound.getList("Tooltips", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            tooltips.add(Component.Serializer.fromJson(list.getString(i), ClientRegistryUtils.getProvider()));
        }
        return tooltips;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
