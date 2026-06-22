package de.maxhenkel.pipez;

import de.maxhenkel.corelib.config.ConfigBase;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;

public class ServerConfig extends ConfigBase {

    public final ModConfigSpec.IntValue itemPipeSpeed;
    public final ModConfigSpec.IntValue itemPipeAmount;
    public final ModConfigSpec.IntValue itemPipeSpeedBasic;
    public final ModConfigSpec.IntValue itemPipeAmountBasic;
    public final ModConfigSpec.IntValue itemPipeSpeedImproved;
    public final ModConfigSpec.IntValue itemPipeAmountImproved;
    public final ModConfigSpec.IntValue itemPipeSpeedAdvanced;
    public final ModConfigSpec.IntValue itemPipeAmountAdvanced;
    public final ModConfigSpec.IntValue itemPipeSpeedUltimate;
    public final ModConfigSpec.IntValue itemPipeAmountUltimate;

    public final ModConfigSpec.IntValue fluidPipeAmount;
    public final ModConfigSpec.IntValue fluidPipeAmountBasic;
    public final ModConfigSpec.IntValue fluidPipeAmountImproved;
    public final ModConfigSpec.IntValue fluidPipeAmountAdvanced;
    public final ModConfigSpec.IntValue fluidPipeAmountUltimate;

    public final ModConfigSpec.IntValue energyPipeAmount;
    public final ModConfigSpec.IntValue energyPipeAmountBasic;
    public final ModConfigSpec.IntValue energyPipeAmountImproved;
    public final ModConfigSpec.IntValue energyPipeAmountAdvanced;
    public final ModConfigSpec.IntValue energyPipeAmountUltimate;

    public final ModConfigSpec.IntValue gasPipeAmount;
    public final ModConfigSpec.IntValue gasPipeAmountBasic;
    public final ModConfigSpec.IntValue gasPipeAmountImproved;
    public final ModConfigSpec.IntValue gasPipeAmountAdvanced;
    public final ModConfigSpec.IntValue gasPipeAmountUltimate;

    public final ModConfigSpec.IntValue filterLimitBasic;
    public final ModConfigSpec.IntValue filterLimitImproved;
    public final ModConfigSpec.IntValue filterLimitAdvanced;
    public final ModConfigSpec.IntValue filterLimitUltimate;
    public final ModConfigSpec.IntValue filterLimitInfinity;

    public ServerConfig(ModConfigSpec.Builder builder) {
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

        filterLimitBasic = builder
                .comment("The maximum amount of filters per basic upgrade")
                .defineInRange("filter.limit.basic", 16, 0, Integer.MAX_VALUE);

        filterLimitImproved = builder
                .comment("The maximum amount of filters per improved upgrade")
                .defineInRange("filter.limit.improved", 32, 0, Integer.MAX_VALUE);

        filterLimitAdvanced = builder
                .comment("The maximum amount of filters per advanced upgrade")
                .defineInRange("filter.limit.advanced", 64, 0, Integer.MAX_VALUE);

        filterLimitUltimate = builder
                .comment("The maximum amount of filters per ultimate upgrade")
                .defineInRange("filter.limit.ultimate", 128, 0, Integer.MAX_VALUE);

        filterLimitInfinity = builder
                .comment("The maximum amount of filters per infinity upgrade")
                .defineInRange("filter.limit.infinity", 256, 0, Integer.MAX_VALUE);
    }

    public int getFilterLimit(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return 0;
        }
        switch (upgrade) {
            case BASIC:
                return filterLimitBasic.get();
            case IMPROVED:
                return filterLimitImproved.get();
            case ADVANCED:
                return filterLimitAdvanced.get();
            case ULTIMATE:
                return filterLimitUltimate.get();
            case INFINITY:
            default:
                return filterLimitInfinity.get();
        }
    }

}
