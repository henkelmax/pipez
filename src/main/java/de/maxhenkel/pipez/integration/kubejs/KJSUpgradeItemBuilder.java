package de.maxhenkel.pipez.integration.kubejs;

import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.items.UpgradeItem;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * @description: add kubejs support
 * @author: HowXu
 * @date: 2025/8/17 23:27
 */

@ReturnsSelf
public class KJSUpgradeItemBuilder extends ItemBuilder {

    // I have to say there is no need to create an id for it
    private KJSCustomUpgradeData data;

    // just default amount
    private int ItemSpeed = 10;
    private int ItemAmount = 16;
    private int FluidAmount = 1000;
    private int EnergyAmount = 32000;
    private int GasAmount = 3200;

    private boolean canChangeRedstoneMode;
    private boolean canChangeDistributionMode;
    private boolean canChangeFilter;

    public KJSUpgradeItemBuilder(ResourceLocation id) {
        super(id);
    }

    @Info("Create a upgrade with given itemSpeed, Range from 1 to 20, wrong data will be ignore")
    public KJSUpgradeItemBuilder itemSpeed(int itemSpeed) {
        if (itemSpeed >= 1 && itemSpeed <= 20) {
            this.ItemSpeed = itemSpeed;
        }
        return this;
    }

    @Info("Create a upgrade with given itemAmount, Range from 0 to MAX_INT, data < 0 will be ignore")
    public KJSUpgradeItemBuilder itemAmount(int itemAmount) {
        if (itemAmount > 0) {
            this.ItemAmount = itemAmount;
        }
        return this;
    }

    @Info("Create a upgrade with given fluidAmount, Range from 0 to MAX_INT, data < 0 will be ignore")
    public KJSUpgradeItemBuilder fluidAmount(int fluidAmount) {
        if (fluidAmount > 0) {
            this.FluidAmount = fluidAmount;
        }
        return this;
    }

    @Info("Create a upgrade with given energyAmount, Range from 0 to MAX_INT, data < 0 will be ignore")
    public KJSUpgradeItemBuilder energyAmount(int energyAmount) {
        if (energyAmount > 0) {
            this.EnergyAmount = energyAmount;
        }
        return this;
    }

    @Info("Create a upgrade with given gasAmount, Range from 0 to MAX_INT, data < 0 will be ignore")
    public KJSUpgradeItemBuilder gasAmount(int gasAmount) {
        if (gasAmount > 0) {
            this.GasAmount = gasAmount;
        }
        return this;
    }

    @Info("Does the upgrade can Change RedstoneMode")
    public KJSUpgradeItemBuilder canChangeRedstoneMode(boolean canChangeRedstoneMode) {
        this.canChangeRedstoneMode = canChangeRedstoneMode;
        return this;
    }

    @Info("Does the upgrade can Change DistributionMode")
    public KJSUpgradeItemBuilder canChangeDistributionMode(boolean canChangeDistributionMode) {
        this.canChangeDistributionMode = canChangeDistributionMode;
        return this;
    }

    @Info("Does the upgrade can Change Filter")
    public KJSUpgradeItemBuilder canChangeFilter(boolean canChangeFilter) {
        this.canChangeFilter = canChangeFilter;
        return this;
    }


    @Override
    public Item createObject() {
        var data = new KJSCustomUpgradeData(this.ItemSpeed, this.ItemAmount, this.FluidAmount, this.EnergyAmount, this.GasAmount);
        // The name is useless, kubejs will adopt it
        var upgrade = new Upgrade("", canChangeRedstoneMode, canChangeDistributionMode, canChangeFilter, data);
        return new UpgradeItem(upgrade);
    }

}
