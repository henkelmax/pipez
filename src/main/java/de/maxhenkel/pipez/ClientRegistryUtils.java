package de.maxhenkel.pipez;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;

public class ClientRegistryUtils {

    public static final Minecraft MC = Minecraft.getInstance();

    public static HolderLookup.Provider getProvider() {
        ClientLevel level = MC.level;
        if (level == null) {
            return RegistryAccess.EMPTY;
        }
        return level.registryAccess();
    }

}
