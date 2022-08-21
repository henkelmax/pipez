package de.maxhenkel.pipez.events

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.PipeBlock
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity
import de.maxhenkel.pipez.connections.CapabilityCache
import de.maxhenkel.pipez.connections.PipeNetworkManager
import de.maxhenkel.pipez.connections.PipeNetworkQueue
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent


class ServerTickEvents {


    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        // capabilityCache.addTick()
        PipeNetworkQueue.INSTANCE.taskTick()
    }

    @SubscribeEvent
    fun onServerStarted(event: ServerStartedEvent) {
        CapabilityCache.INSTANCE.clear()
        PipeNetworkQueue.INSTANCE.clear()
    }

}