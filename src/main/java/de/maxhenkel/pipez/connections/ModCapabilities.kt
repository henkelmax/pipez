package de.maxhenkel.pipez.connections

import mekanism.api.chemical.gas.IGasHandler
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.CapabilityToken

object ModCapabilities {
    @JvmStatic
    val GAS_HANDLER_CAPABILITY = CapabilityManager.get(object : CapabilityToken<IGasHandler>() {})
}