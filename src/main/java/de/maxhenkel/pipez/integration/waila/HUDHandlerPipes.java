package de.maxhenkel.pipez.integration.waila;

import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.tileentity.ItemPipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class HUDHandlerPipes implements IComponentProvider, IServerDataProvider<TileEntity> {

    static final HUDHandlerPipes INSTANCE = new HUDHandlerPipes();

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        CompoundNBT compound = accessor.getServerData();
        if (compound.contains("Upgrade", Constants.NBT.TAG_STRING)) {
            tooltip.add(ITextComponent.Serializer.getComponentFromJson(compound.getString("Upgrade")));
        }
        if (compound.contains("Tooltip", Constants.NBT.TAG_STRING)) {
            tooltip.add(ITextComponent.Serializer.getComponentFromJson(compound.getString("Tooltip")));
        }
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

            UpgradeTileEntity<?> upgradeTileEntity = (UpgradeTileEntity<?>) te;

            ItemStack upgrade = upgradeTileEntity.getUpgradeInventory().getStackInSlot(selectedSide.getIndex());
            if (!upgrade.isEmpty()) {
                compound.putString("Upgrade", ITextComponent.Serializer.toJson(upgrade.getDisplayName()));
                if (upgradeTileEntity instanceof ItemPipeTileEntity) {
                    ItemPipeTileEntity itemPipe = (ItemPipeTileEntity) upgradeTileEntity;
                    compound.putString("Tooltip", ITextComponent.Serializer.toJson(new TranslationTextComponent("tooltip.pipez.item_rate", itemPipe.getRate(selectedSide), itemPipe.getSpeed(selectedSide))));
                } else {
                    compound.putString("Tooltip", ITextComponent.Serializer.toJson(new TranslationTextComponent("tooltip.pipez.rate", upgradeTileEntity.getRate(selectedSide))));
                }
            } else {
                compound.putString("Tooltip", ITextComponent.Serializer.toJson(new TranslationTextComponent("tooltip.pipez.no_upgrade")));
            }
        }
    }
}
