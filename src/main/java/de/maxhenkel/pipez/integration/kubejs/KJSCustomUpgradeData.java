package de.maxhenkel.pipez.integration.kubejs;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * @description: add kubejs support
 * @author: HowXu
 * @date: 2025/8/17 23:59
 */
public class KJSCustomUpgradeData {
    
    private final int ItemSpeed;
    private final int ItemAmount;
    private final int FluidAmount;
    private final int EnergyAmount;
    private final int GasAmount;

    public KJSCustomUpgradeData(int itemSpeed, int itemAmount, int fluidAmount, int energyAmount, int gasAmount) {
        ItemSpeed = itemSpeed;
        ItemAmount = itemAmount;
        FluidAmount = fluidAmount;
        EnergyAmount = energyAmount;
        GasAmount = gasAmount;
    }

    public int getEnergyAmount() {
        return EnergyAmount;
    }

    public int getItemAmount() {
        return ItemAmount;
    }

    public int getItemSpeed() {
        return ItemSpeed;
    }

    public int getFluidAmount() {
        return FluidAmount;
    }

    public int getGasAmount() {
        return GasAmount;
    }
}
