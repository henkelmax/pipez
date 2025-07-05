package de.maxhenkel.pipez.integration.waila;

import de.maxhenkel.corelib.codec.CodecUtils;
import de.maxhenkel.pipez.PipezMod;
import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.ArrayList;
import java.util.List;

public class DataProviderPipes implements IServerDataProvider<BlockAccessor> {

    static final DataProviderPipes INSTANCE = new DataProviderPipes();

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "pipe_data");

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
                CodecUtils.toJsonString(ComponentSerialization.CODEC, Component.translatable("tooltip.pipez.no_upgrade")).ifPresent(s -> compound.putString("Upgrade", s));
            } else {
                CodecUtils.toJsonString(ComponentSerialization.CODEC, upgrade.getHoverName()).ifPresent(s -> compound.putString("Upgrade", s));
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
            CodecUtils.toJsonString(ComponentSerialization.CODEC, tooltip).ifPresent(s -> list.add(StringTag.valueOf(s)));
        }
        compound.put("Tooltips", list);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
