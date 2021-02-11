package de.maxhenkel.pipez;

import de.maxhenkel.corelib.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig extends ConfigBase {

    public final ForgeConfigSpec.IntValue itemPipeSpeed;
    public final ForgeConfigSpec.IntValue itemPipeAmount;
    public final ForgeConfigSpec.IntValue itemPipeSpeedBasic;
    public final ForgeConfigSpec.IntValue itemPipeAmountBasic;
    public final ForgeConfigSpec.IntValue itemPipeSpeedImproved;
    public final ForgeConfigSpec.IntValue itemPipeAmountImproved;
    public final ForgeConfigSpec.IntValue itemPipeSpeedAdvanced;
    public final ForgeConfigSpec.IntValue itemPipeAmountAdvanced;
    public final ForgeConfigSpec.IntValue itemPipeSpeedUltimate;
    public final ForgeConfigSpec.IntValue itemPipeAmountUltimate;

    public final ForgeConfigSpec.IntValue fluidPipeAmount;
    public final ForgeConfigSpec.IntValue fluidPipeAmountBasic;
    public final ForgeConfigSpec.IntValue fluidPipeAmountImproved;
    public final ForgeConfigSpec.IntValue fluidPipeAmountAdvanced;
    public final ForgeConfigSpec.IntValue fluidPipeAmountUltimate;

    public final ForgeConfigSpec.IntValue energyPipeAmount;
    public final ForgeConfigSpec.IntValue energyPipeAmountBasic;
    public final ForgeConfigSpec.IntValue energyPipeAmountImproved;
    public final ForgeConfigSpec.IntValue energyPipeAmountAdvanced;
    public final ForgeConfigSpec.IntValue energyPipeAmountUltimate;

    public final ForgeConfigSpec.IntValue gasPipeAmount;
    public final ForgeConfigSpec.IntValue gasPipeAmountBasic;
    public final ForgeConfigSpec.IntValue gasPipeAmountImproved;
    public final ForgeConfigSpec.IntValue gasPipeAmountAdvanced;
    public final ForgeConfigSpec.IntValue gasPipeAmountUltimate;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);
        itemPipeSpeed = builder
                .comment("The speed at which items are transferred", "A value of 1 means every tick")
                .defineInRange("item_pipe.speed.no_upgrade", 20, 1, Integer.MAX_VALUE);

        itemPipeAmount = builder
                .comment("The amount of items transferred")
                .defineInRange("item_pipe.amount.no_upgrade", 4, 1, Integer.MAX_VALUE);

        itemPipeSpeedBasic = builder
                .comment("The speed at which items are transferred", "A value of 1 means every tick")
                .defineInRange("item_pipe.speed.basic", 15, 1, Integer.MAX_VALUE);

        itemPipeAmountBasic = builder
                .comment("The amount of items transferred")
                .defineInRange("item_pipe.amount.basic", 8, 1, Integer.MAX_VALUE);

        itemPipeSpeedImproved = builder
                .comment("The speed at which items are transferred", "A value of 1 means every tick")
                .defineInRange("item_pipe.speed.improved", 10, 1, Integer.MAX_VALUE);

        itemPipeAmountImproved = builder
                .comment("The amount of items transferred")
                .defineInRange("item_pipe.amount.improved", 16, 1, Integer.MAX_VALUE);

        itemPipeSpeedAdvanced = builder
                .comment("The speed at which items are transferred", "A value of 1 means every tick")
                .defineInRange("item_pipe.speed.advanced", 5, 1, Integer.MAX_VALUE);

        itemPipeAmountAdvanced = builder
                .comment("The amount of items transferred")
                .defineInRange("item_pipe.amount.advanced", 32, 1, Integer.MAX_VALUE);

        itemPipeSpeedUltimate = builder
                .comment("The speed at which items are transferred", "A value of 1 means every tick")
                .defineInRange("item_pipe.speed.ultimate", 1, 1, Integer.MAX_VALUE);

        itemPipeAmountUltimate = builder
                .comment("The amount of items transferred")
                .defineInRange("item_pipe.amount.ultimate", 64, 1, Integer.MAX_VALUE);

        fluidPipeAmount = builder
                .comment("The amount of mB transferred each tick")
                .defineInRange("fluid_pipe.amount.no_upgrade", 50, 1, Integer.MAX_VALUE);

        fluidPipeAmountBasic = builder
                .comment("The amount of mB transferred each tick")
                .defineInRange("fluid_pipe.amount.basic", 100, 1, Integer.MAX_VALUE);

        fluidPipeAmountImproved = builder
                .comment("The amount of mB transferred each tick")
                .defineInRange("fluid_pipe.amount.improved", 500, 1, Integer.MAX_VALUE);

        fluidPipeAmountAdvanced = builder
                .comment("The amount of mB transferred each tick")
                .defineInRange("fluid_pipe.amount.advanced", 2000, 1, Integer.MAX_VALUE);

        fluidPipeAmountUltimate = builder
                .comment("The amount of mB transferred each tick")
                .defineInRange("fluid_pipe.amount.ultimate", 10000, 1, Integer.MAX_VALUE);

        energyPipeAmount = builder
                .comment("The amount of FE transferred each tick")
                .defineInRange("energy_pipe.amount.no_upgrade", 256, 1, Integer.MAX_VALUE);

        energyPipeAmountBasic = builder
                .comment("The amount of FE transferred each tick")
                .defineInRange("energy_pipe.amount.basic", 1024, 1, Integer.MAX_VALUE);

        energyPipeAmountImproved = builder
                .comment("The amount of FE transferred each tick")
                .defineInRange("energy_pipe.amount.improved", 8192, 1, Integer.MAX_VALUE);

        energyPipeAmountAdvanced = builder
                .comment("The amount of FE transferred each tick")
                .defineInRange("energy_pipe.amount.advanced", 32768, 1, Integer.MAX_VALUE);

        energyPipeAmountUltimate = builder
                .comment("The amount of FE transferred each tick")
                .defineInRange("energy_pipe.amount.ultimate", 131072, 1, Integer.MAX_VALUE);

        gasPipeAmount = builder
                .comment("The amount of mB transferred each tick", "Only available if Mekanism is installed")
                .defineInRange("gas_pipe.amount.no_upgrade", 200, 1, Integer.MAX_VALUE);

        gasPipeAmountBasic = builder
                .comment("The amount of mB transferred each tick", "Only available if Mekanism is installed")
                .defineInRange("gas_pipe.amount.basic", 400, 1, Integer.MAX_VALUE);

        gasPipeAmountImproved = builder
                .comment("The amount of mB transferred each tick", "Only available if Mekanism is installed")
                .defineInRange("gas_pipe.amount.improved", 2000, 1, Integer.MAX_VALUE);

        gasPipeAmountAdvanced = builder
                .comment("The amount of mB transferred each tick", "Only available if Mekanism is installed")
                .defineInRange("gas_pipe.amount.advanced", 8000, 1, Integer.MAX_VALUE);

        gasPipeAmountUltimate = builder
                .comment("The amount of mB transferred each tick", "Only available if Mekanism is installed")
                .defineInRange("gas_pipe.amount.ultimate", 40000, 1, Integer.MAX_VALUE);
    }

}
