package de.maxhenkel.pipez.integration.waila;

import de.maxhenkel.corelib.codec.CodecUtils;
import de.maxhenkel.pipez.Main;
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
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HUDHandlerPipes implements IBlockComponentProvider {

    static final HUDHandlerPipes INSTANCE = new HUDHandlerPipes();

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Main.MODID, "pipe");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag compound = blockAccessor.getServerData();

        compound.getString("Upgrade").flatMap(s -> CodecUtils.fromJson(ComponentSerialization.CODEC, s)).ifPresent(iTooltip::add);

        iTooltip.addAll(getTooltips(blockAccessor, compound));
    }

    public List<Component> getTooltips(BlockAccessor blockAccessor, CompoundTag compound) {
        List<Component> tooltips = new ArrayList<>();
        Optional<ListTag> optionalList = compound.getList("Tooltips");
        if (optionalList.isEmpty()) {
            return tooltips;
        }
        ListTag list = optionalList.get();
        for (int i = 0; i < list.size(); i++) {
            list.getString(i).flatMap(s -> CodecUtils.fromJson(ComponentSerialization.CODEC, s)).ifPresent(tooltips::add);
        }
        return tooltips;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
