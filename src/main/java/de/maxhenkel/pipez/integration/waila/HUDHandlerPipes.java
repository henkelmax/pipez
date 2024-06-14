package de.maxhenkel.pipez.integration.waila;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public class HUDHandlerPipes implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    static final HUDHandlerPipes INSTANCE = new HUDHandlerPipes();

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Main.MODID, "pipe");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag compound = blockAccessor.getServerData();
        if (compound.contains("Upgrade", Tag.TAG_STRING)) {
            iTooltip.add(Component.Serializer.fromJson(compound.getString("Upgrade"), blockAccessor.getLevel().registryAccess()));
        }
        iTooltip.addAll(getTooltips(blockAccessor, compound));
    }

    @Override
    public void appendServerData(CompoundTag compound, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockState().getBlock() instanceof PipeBlock pipe) {
            BlockEntity te = blockAccessor.getBlockEntity();
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
                compound.putString("Upgrade", Component.Serializer.toJson(Component.translatable("tooltip.pipez.no_upgrade"), blockAccessor.getLevel().registryAccess()));
            } else {
                compound.putString("Upgrade", Component.Serializer.toJson(upgrade.getHoverName(), blockAccessor.getLevel().registryAccess()));
            }

            List<Component> tooltips = new ArrayList<>();
            for (PipeType<?, ?> pipeType : pipeTile.getPipeTypes()) {
                if (pipeTile.isEnabled(selectedSide, pipeType)) {
                    tooltips.add(pipeType.getTransferText(pipeTile.getUpgrade(selectedSide)));
                }
            }
            putTooltips(blockAccessor, compound, tooltips);
        }
    }

    public void putTooltips(BlockAccessor blockAccessor, CompoundTag compound, List<Component> tooltips) {
        ListTag list = new ListTag();
        for (Component tooltip : tooltips) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(tooltip, blockAccessor.getLevel().registryAccess())));
        }
        compound.put("Tooltips", list);
    }

    public List<Component> getTooltips(BlockAccessor blockAccessor, CompoundTag compound) {
        List<Component> tooltips = new ArrayList<>();
        if (!compound.contains("Tooltips", Tag.TAG_LIST)) {
            return tooltips;
        }
        ListTag list = compound.getList("Tooltips", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            tooltips.add(Component.Serializer.fromJson(list.getString(i), blockAccessor.getLevel().registryAccess()));
        }
        return tooltips;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
