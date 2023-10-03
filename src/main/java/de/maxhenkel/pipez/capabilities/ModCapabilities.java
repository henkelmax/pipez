package de.maxhenkel.pipez.capabilities;

import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {

    public static Capability<IGasHandler> GAS_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static Capability<IInfusionHandler> INFUSION_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static Capability<IPigmentHandler> PIGMENT_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static Capability<ISlurryHandler> SLURRY_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

}
