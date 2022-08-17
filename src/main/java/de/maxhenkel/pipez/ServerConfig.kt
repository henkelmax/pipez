package de.maxhenkel.pipez

import de.maxhenkel.corelib.config.ConfigBase
import de.maxhenkel.pipez.types.CapabilityType
import de.maxhenkel.pipez.types.TransferType
import de.maxhenkel.pipez.types.Upgrade
import net.minecraftforge.common.ForgeConfigSpec

class ServerConfig(private val builder:ForgeConfigSpec.Builder) : ConfigBase(builder) {

    // forge config system doesn't allow to inject dynamically
    // infinity
    val infinitySpeed = buildIntValue(CapabilityType.ITEM, Upgrade.INFINITY, TransferType.SPEED)
    val infinityAmount = buildIntValue(CapabilityType.ITEM, Upgrade.INFINITY, TransferType.AMOUNT)

    // items
    val itemPipeSpeed = buildIntValue(CapabilityType.ITEM, Upgrade.NONE, TransferType.SPEED)
    val itemPipeAmount = buildIntValue(CapabilityType.ITEM, Upgrade.NONE, TransferType.AMOUNT)
    val itemPipeSpeedBasic = buildIntValue(CapabilityType.ITEM, Upgrade.BASIC, TransferType.SPEED)
    val itemPipeAmountBasic = buildIntValue(CapabilityType.ITEM, Upgrade.BASIC, TransferType.AMOUNT)
    val itemPipeSpeedImproved = buildIntValue(CapabilityType.ITEM, Upgrade.IMPROVED, TransferType.SPEED)
    val itemPipeAmountImproved = buildIntValue(CapabilityType.ITEM, Upgrade.IMPROVED, TransferType.AMOUNT)
    val itemPipeSpeedAdvanced = buildIntValue(CapabilityType.ITEM, Upgrade.ADVANCED, TransferType.SPEED)
    val itemPipeAmountAdvanced = buildIntValue(CapabilityType.ITEM, Upgrade.ADVANCED, TransferType.AMOUNT)
    val itemPipeSpeedUltimate = buildIntValue(CapabilityType.ITEM, Upgrade.ULTIMATE, TransferType.SPEED)
    val itemPipeAmountUltimate = buildIntValue(CapabilityType.ITEM, Upgrade.ULTIMATE, TransferType.AMOUNT)

    // Fluid
    val fluidPipeAmount = buildIntValue(CapabilityType.FLUID, Upgrade.NONE)
    val fluidPipeAmountBasic = buildIntValue(CapabilityType.FLUID, Upgrade.BASIC)
    val fluidPipeAmountImproved = buildIntValue(CapabilityType.FLUID, Upgrade.IMPROVED)
    val fluidPipeAmountAdvanced = buildIntValue(CapabilityType.FLUID, Upgrade.ADVANCED)
    val fluidPipeAmountUltimate = buildIntValue(CapabilityType.FLUID, Upgrade.ULTIMATE)

    // Energy
    val energyPipeAmount = buildIntValue(CapabilityType.ENERGY, Upgrade.NONE)
    val energyPipeAmountBasic = buildIntValue(CapabilityType.ENERGY, Upgrade.BASIC)
    val energyPipeAmountImproved = buildIntValue(CapabilityType.ENERGY, Upgrade.IMPROVED)
    val energyPipeAmountAdvanced = buildIntValue(CapabilityType.ENERGY, Upgrade.ADVANCED)
    val energyPipeAmountUltimate = buildIntValue(CapabilityType.ENERGY, Upgrade.ULTIMATE)

    // Gas
    val gasPipeAmount = buildIntValue(CapabilityType.GAS, Upgrade.NONE)
    val gasPipeAmountBasic = buildIntValue(CapabilityType.GAS, Upgrade.BASIC)
    val gasPipeAmountImproved = buildIntValue(CapabilityType.GAS, Upgrade.IMPROVED)
    val gasPipeAmountAdvanced = buildIntValue(CapabilityType.GAS, Upgrade.ADVANCED)
    val gasPipeAmountUltimate = buildIntValue(CapabilityType.GAS, Upgrade.ULTIMATE)

    fun getPipeValue(capType:CapabilityType, upgrade: Upgrade,
                      transferType: TransferType = TransferType.AMOUNT):Int {
        // get pipe value..... to ineffective way
        return when(capType) {
            CapabilityType.ITEM -> when (transferType) {
                TransferType.AMOUNT -> when (upgrade) {
                    Upgrade.NONE -> itemPipeAmount
                    Upgrade.BASIC -> itemPipeAmountBasic
                    Upgrade.IMPROVED -> itemPipeAmountImproved
                    Upgrade.ADVANCED -> itemPipeAmountAdvanced
                    Upgrade.ULTIMATE -> itemPipeAmountUltimate
                    Upgrade.INFINITY -> infinityAmount
                }
                TransferType.SPEED -> when (upgrade) {
                    Upgrade.NONE -> itemPipeSpeed
                    Upgrade.BASIC -> itemPipeSpeedBasic
                    Upgrade.IMPROVED -> itemPipeSpeedImproved
                    Upgrade.ADVANCED -> itemPipeSpeedAdvanced
                    Upgrade.ULTIMATE -> itemPipeSpeedUltimate
                    Upgrade.INFINITY -> infinitySpeed
                }
            }
            CapabilityType.FLUID -> when (upgrade) {
                Upgrade.NONE -> fluidPipeAmount
                Upgrade.BASIC -> fluidPipeAmountBasic
                Upgrade.IMPROVED -> fluidPipeAmountImproved
                Upgrade.ADVANCED -> fluidPipeAmountAdvanced
                Upgrade.ULTIMATE -> fluidPipeAmountUltimate
                Upgrade.INFINITY -> infinityAmount
            }
            CapabilityType.ENERGY -> when (upgrade) {
                Upgrade.NONE -> energyPipeAmount
                Upgrade.BASIC -> energyPipeAmountBasic
                Upgrade.IMPROVED -> energyPipeAmountImproved
                Upgrade.ADVANCED -> energyPipeAmountAdvanced
                Upgrade.ULTIMATE -> energyPipeAmountUltimate
                Upgrade.INFINITY -> infinityAmount
            }
            CapabilityType.GAS -> when (upgrade) {
                Upgrade.NONE -> gasPipeAmount
                Upgrade.BASIC -> gasPipeAmountBasic
                Upgrade.IMPROVED -> gasPipeAmountImproved
                Upgrade.ADVANCED -> gasPipeAmountAdvanced
                Upgrade.ULTIMATE -> gasPipeAmountUltimate
                Upgrade.INFINITY -> infinityAmount
            }
        }.get()
    }

    private fun buildIntValue(capType:CapabilityType, upgrade: Upgrade,
                              transferType: TransferType = TransferType.AMOUNT):ForgeConfigSpec.IntValue {
        val defaultValue:Int = when (capType) {
            CapabilityType.ITEM -> when (transferType) {
                // Speed
                TransferType.SPEED -> when (upgrade) {
                    Upgrade.NONE -> 20
                    Upgrade.BASIC -> 15
                    Upgrade.IMPROVED -> 10
                    Upgrade.ADVANCED -> 5
                    else -> 1
                }
                // Amount
                TransferType.AMOUNT -> when (upgrade) {
                    Upgrade.NONE -> 4
                    Upgrade.BASIC -> 8
                    Upgrade.IMPROVED -> 16
                    Upgrade.ADVANCED -> 32
                    Upgrade.ULTIMATE -> 64
                    Upgrade.INFINITY -> Int.MAX_VALUE // failsafe
                }
            }
            CapabilityType.FLUID -> when (upgrade) {
                Upgrade.NONE -> 50
                Upgrade.BASIC -> 100
                Upgrade.IMPROVED -> 500
                Upgrade.ADVANCED -> 2000
                Upgrade.ULTIMATE -> if (Main.isMekanismLoaded()) 32000 else 10000 // Mekanism match
                Upgrade.INFINITY -> Int.MAX_VALUE // failsafe
            }
            CapabilityType.ENERGY -> when (upgrade) {
                Upgrade.NONE -> 256
                Upgrade.BASIC -> 1024
                Upgrade.IMPROVED -> 8192
                Upgrade.ADVANCED -> 32768
                Upgrade.ULTIMATE -> if (Main.isMekanismLoaded()) 3270000 else 131072 // Mekanism match
                Upgrade.INFINITY -> Int.MAX_VALUE
            }
            CapabilityType.GAS -> when (upgrade) {
                Upgrade.NONE -> 200
                Upgrade.BASIC -> 400
                Upgrade.IMPROVED -> 2000
                Upgrade.ADVANCED -> 8000
                Upgrade.ULTIMATE -> if (Main.isMekanismLoaded()) 256000 else 40000 // Mekanism match
                Upgrade.INFINITY -> Int.MAX_VALUE
            }
        }

        return builder.comment(
            when (capType) {
                CapabilityType.ITEM -> when (transferType) {
                    TransferType.SPEED -> "The speed at which items are transferred"
                    TransferType.AMOUNT -> "The amount of items transferred"
                }
                CapabilityType.FLUID -> "The amount of mB transferred each tick"
                CapabilityType.ENERGY -> "The amount of FE transferred each tick"
                CapabilityType.GAS -> "The amount of mB transferred each tick (Only available if Mekanism is installed)"
            }
        ).defineInRange("${
            when (capType) {
                CapabilityType.ITEM -> "item_pipe"
                CapabilityType.FLUID -> "fluid_pipe"
                CapabilityType.ENERGY -> "energy_pipe"
                CapabilityType.GAS -> "gas_pipe"
            }
        }.${
            when (transferType) {
                TransferType.AMOUNT -> "amount"
                TransferType.SPEED -> "speed"
            }
        }.${
            when (upgrade) {
                Upgrade.NONE -> "no_upgrade"
                Upgrade.BASIC -> "basic"
                Upgrade.IMPROVED -> "improved"
                Upgrade.ADVANCED -> "advanced"
                Upgrade.ULTIMATE -> "ultimate"
                Upgrade.INFINITY -> "infinity" // dummy
            }
        }", defaultValue, 1, Int.MAX_VALUE)
    }
}

private data class ValueType(
    val capType: CapabilityType,
    val upgradeType: Upgrade,
    val transferType: TransferType,
)