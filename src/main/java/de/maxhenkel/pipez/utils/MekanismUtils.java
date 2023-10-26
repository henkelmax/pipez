package de.maxhenkel.pipez.utils;

import net.minecraftforge.fml.ModList;

public class MekanismUtils {

    private static Boolean isLoaded;

    public static boolean isMekanismInstalled() {
        if (isLoaded == null) {
            isLoaded = ModList.get().isLoaded("mekanism");
        }
        return isLoaded;
    }

}
