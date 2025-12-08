package de.maxhenkel.pipez.capabilities;

import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public class ModCapabilities {

    public static final BlockCapability<IChemicalHandler, @Nullable Direction> CHEMICAL_HANDLER_CAPABILITY = BlockCapability.createSided(Identifier.fromNamespaceAndPath("mekanism", "chemical_handler"), IChemicalHandler.class);
    public static final ItemCapability<IChemicalHandler, Void> CHEMICAL_HANDLER_ITEM_CAPABILITY = ItemCapability.createVoid(Identifier.fromNamespaceAndPath("mekanism", "chemical_handler"), IChemicalHandler.class);

}
