package de.maxhenkel.pipez;

import de.maxhenkel.pipez.integration.kubejs.KJSCustomUpgradeData;

import javax.annotation.Nullable;

// HowXu: add kubejs support, to pass the data from kubejs we have to change it from enum to class
// for mostly keeping the origin code, I add id form switch. memory for speed.
public class Upgrade {
    
    // 0 BASIC
    // 1 IMPROVED
    // 2 ADVANCED
    // 3 ULTIMATE
    // -1 INFINITY
    public static final Upgrade BASIC = new Upgrade("basic", true, false, false,0);
    public static final Upgrade IMPROVED = new Upgrade("improved", true, true, false,1);
    public static final Upgrade ADVANCED = new Upgrade("advanced", true, true, true,2);
    public static final Upgrade ULTIMATE = new Upgrade("ultimate", true, true, true,3);
    public static final Upgrade INFINITY = new Upgrade("infinity", true, true, true,-1);

    private final String name;
    private final boolean canChangeRedstoneMode;
    private final boolean canChangeDistributionMode;
    private final boolean canChangeFilter;
    
    // add kubejs support
    @Nullable
    private KJSCustomUpgradeData customData = null;
    
    // I think byte is all we need, but for JS int
    private final int id;

    public Upgrade(String name, boolean canChangeRedstoneMode, boolean canChangeDistributionMode, boolean canChangeFilter,int id) {
        this.name = name;
        this.canChangeRedstoneMode = canChangeRedstoneMode;
        this.canChangeDistributionMode = canChangeDistributionMode;
        this.canChangeFilter = canChangeFilter;
        // HowXu: add kubejs support
        this.customData = null;
        this.id = id;
    }

    public Upgrade(String name, boolean canChangeRedstoneMode, boolean canChangeDistributionMode, boolean canChangeFilter,KJSCustomUpgradeData customData) {
        this.name = name;
        this.canChangeRedstoneMode = canChangeRedstoneMode;
        this.canChangeDistributionMode = canChangeDistributionMode;
        this.canChangeFilter = canChangeFilter;
        // add kubejs support
        this.customData = customData;
        // Just a number
        this.id = 4;
    }

    public String getName() {
        return name;
    }

    public boolean canChangeRedstoneMode() {
        return canChangeRedstoneMode;
    }

    public boolean canChangeDistributionMode() {
        return canChangeDistributionMode;
    }

    public boolean canChangeFilter() {
        return canChangeFilter;
    }

    public static boolean canChangeRedstoneMode(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return false;
        }
        return upgrade.canChangeRedstoneMode();
    }

    public static boolean canChangeDistributionMode(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return false;
        }
        return upgrade.canChangeDistributionMode();
    }

    public static boolean canChangeFilter(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return false;
        }
        return upgrade.canChangeFilter();
    }
    
    // HowXu: add kubejs support
    public KJSCustomUpgradeData getKubeJSCustomData(){
        if(customData == null) {
            // default ultimate
            return new KJSCustomUpgradeData(
                    Main.SERVER_CONFIG.itemPipeSpeedUltimate.get(),
                    Main.SERVER_CONFIG.itemPipeSpeedUltimate.get(),
                    Main.SERVER_CONFIG.fluidPipeAmountUltimate.get(),
                    Main.SERVER_CONFIG.energyPipeAmountUltimate.get(),
                    Main.SERVER_CONFIG.gasPipeAmountUltimate.get()
            );        
        }
        return this.customData;
    }

    public int getId() {
        return id;
    }
}
