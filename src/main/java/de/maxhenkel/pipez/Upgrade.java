package de.maxhenkel.pipez;

import javax.annotation.Nullable;

public enum Upgrade {

    BASIC("basic", true, false, false),
    IMPROVED("improved", true, true, false),
    ADVANCED("advanced", true, true, true),
    ULTIMATE("ultimate", true, true, true),
    INFINITY("infinity", true, true, true);

    private final String name;
    private final boolean canChangeRedstoneMode;
    private final boolean canChangeDistributionMode;
    private final boolean canChangeFilter;


    Upgrade(String name, boolean canChangeRedstoneMode, boolean canChangeDistributionMode, boolean canChangeFilter) {
        this.name = name;
        this.canChangeRedstoneMode = canChangeRedstoneMode;
        this.canChangeDistributionMode = canChangeDistributionMode;
        this.canChangeFilter = canChangeFilter;
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

}
