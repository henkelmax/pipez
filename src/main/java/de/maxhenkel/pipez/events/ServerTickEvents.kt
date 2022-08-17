package de.maxhenkel.pipez.events

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.utils.CapabilityCache
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent


class ServerTickEvents {

    companion object {
        var capabilityCache = CapabilityCache()
    }

    private var tickCount = 0

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        tickCount += 1
        // capabilityCache.addTick()
    }

    @SubscribeEvent
    fun onServerStarted(event: ServerStartedEvent) {
        capabilityCache = CapabilityCache()
        tickCount = 0
        // logger.log(Level.DEBUG, "On server started!");
    }

    @SubscribeEvent
    fun onAttachingCapabilities(event: AttachCapabilitiesEvent<BlockEntity>) {
        val entity = event.getObject()
        Main.logDebug("Entity Position: " + entity.blockPos)
    }

}