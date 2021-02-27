package de.maxhenkel.pipez.integration.waila;

import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class HUDHandlerPipes implements IComponentProvider, IServerDataProvider<TileEntity> {

    static final HUDHandlerPipes INSTANCE = new HUDHandlerPipes();

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        CompoundNBT compound = accessor.getServerData();
        if (compound.contains("Upgrade", Constants.NBT.TAG_STRING)) {
            tooltip.add(ITextComponent.Serializer.getComponentFromJson(compound.getString("Upgrade")));
        }
        tooltip.addAll(getTooltips(compound));
    }

    @Override
    public void appendServerData(CompoundNBT compound, ServerPlayerEntity player, World world, TileEntity te) {
        if (te.getBlockState().getBlock() instanceof PipeBlock) {
            PipeBlock pipe = (PipeBlock) te.getBlockState().getBlock();
            Direction selectedSide = pipe.getSelection(te.getBlockState(), world, te.getPos(), player).getKey();
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
                compound.putString("Upgrade", ITextComponent.Serializer.toJson(new TranslationTextComponent("tooltip.pipez.no_upgrade")));
            } else {
                compound.putString("Upgrade", ITextComponent.Serializer.toJson(upgrade.getDisplayName()));
            }

            List<ITextComponent> tooltips = new ArrayList<>();
            for (PipeType<?> pipeType : pipeTile.getPipeTypes()) {
                if (pipeTile.isEnabled(selectedSide, pipeType)) {
                    tooltips.add(pipeType.getTransferText(pipeTile.getUpgrade(selectedSide)));
                }
            }
            putTooltips(compound, tooltips);
        }
    }

    public void putTooltips(CompoundNBT compound, List<ITextComponent> tooltips) {
        ListNBT list = new ListNBT();
        for (ITextComponent tooltip : tooltips) {
            list.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(tooltip)));
        }
        compound.put("Tooltips", list);
    }

    public List<ITextComponent> getTooltips(CompoundNBT compound) {
        List<ITextComponent> tooltips = new ArrayList<>();
        if (!compound.contains("Tooltips", Constants.NBT.TAG_LIST)) {
            return tooltips;
        }
        ListNBT list = compound.getList("Tooltips", Constants.NBT.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            tooltips.add(ITextComponent.Serializer.getComponentFromJson(list.getString(i)));
        }
        return tooltips;
    }

}
