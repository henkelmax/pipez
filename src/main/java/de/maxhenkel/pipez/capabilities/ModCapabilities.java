package de.maxhenkel.pipez.capabilities;

import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public class ModCapabilities {

    public static final BlockCapability<IGasHandler, @Nullable Direction> GAS_HANDLER_CAPABILITY = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "gas_handler"), IGasHandler.class);
    public static final ItemCapability<IGasHandler, Void> GAS_HANDLER_ITEM_CAPABILITY = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "gas_handler"), IGasHandler.class);

    public static final BlockCapability<IInfusionHandler, @Nullable Direction> INFUSION_HANDLER_CAPABILITY = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "infusion_handler"), IInfusionHandler.class);
    public static final ItemCapability<IInfusionHandler, Void> INFUSION_HANDLER_ITEM_CAPABILITY = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "infusion_handler"), IInfusionHandler.class);

    public static final BlockCapability<IPigmentHandler, @Nullable Direction> PIGMENT_HANDLER_CAPABILITY = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "pigment_handler"), IPigmentHandler.class);
    public static final ItemCapability<IPigmentHandler, Void> PIGMENT_HANDLER_ITEM_CAPABILITY = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "pigment_handler"), IPigmentHandler.class);

    public static final BlockCapability<ISlurryHandler, @Nullable Direction> SLURRY_HANDLER_CAPABILITY = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "slurry_handler"), ISlurryHandler.class);
    public static final ItemCapability<ISlurryHandler, Void> SLURRY_HANDLER_ITEM_CAPABILITY = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "slurry_handler"), ISlurryHandler.class);

}
