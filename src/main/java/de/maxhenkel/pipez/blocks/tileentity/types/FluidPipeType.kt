package de.maxhenkel.pipez.blocks.tileentity.types

import de.maxhenkel.pipez.Filter
import de.maxhenkel.pipez.FluidFilter
import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.ModBlocks
import de.maxhenkel.pipez.types.*
import de.maxhenkel.pipez.connections.CapabilityCache
import de.maxhenkel.pipez.connections.CapabilityCache.Companion.asValue
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

class FluidPipeType : AbsTankPipeType<Fluid, IFluidHandler, FluidStack>() {

    override val hardLimit: Long = Int.MAX_VALUE.toLong()

    companion object {
        val INSTANCE = FluidPipeType()
    }

    override fun getCapability(
        level: Level,
        blockPos: BlockPos,
        direction: Direction,
        cacheMode: CacheMode
    ): IFluidHandler? {
        return CapabilityCache.INSTANCE.getFluidCapability(
            level,
            blockPos,
            direction,
            cacheMode,
        ).asValue()
    }

    override fun getTag(material: FluidStack): CompoundTag? {
        return material.tag
    }

    override fun getType(material: FluidStack): Fluid {
        return material.fluid
    }

    override fun canInsert(tileEntity: BlockEntity, direction: Direction): Boolean {
        return CapabilityCache.INSTANCE.getFluidCapability(
            level = tileEntity.level ?: return false,
            blockPos = tileEntity.blockPos,
            direction = direction,
            cacheMode = CacheMode.PREFER_CACHE,
        ).isPresent
    }

    override fun canMerge(materialA: FluidStack, materialB: FluidStack): Boolean {
        return materialA.isEmpty || materialB.isEmpty || materialA.isFluidEqual(materialB)
    }

    override fun insertMaterial(
        capability: IFluidHandler,
        tankIndex: Int,
        material: FluidStack,
        simulate: Boolean
    ): Long {
        return capability.fill(material, isSimulate(simulate)).toLong()
    }

    override fun makeMaterial(material: FluidStack, amount: Long): FluidStack {
        return FluidStack(material, amount.toInt())
    }

    override fun getMaterialAmount(material: FluidStack): Long {
        if (material.isEmpty) {
            return 0L
        }
        return material.amount.toLong()
    }

    override fun extractMaterial(
        capability: IFluidHandler,
        tankIndex: Int,
        amount: Long,
        simulate: Boolean
    ): FluidStack {
        return capability.drain(limit(amount).toInt(), isSimulate(simulate))
    }

    override fun canInsert(capability: IFluidHandler, tankIndex: Int, material: FluidStack): Boolean {
        return capability.isFluidValid(tankIndex, material)
    }

    override fun canExtract(capability: IFluidHandler, tankIndex: Int): Boolean {
        return true
    }

    override fun getTankMaterial(capability: IFluidHandler, tankIndex: Int): FluidStack {
        return capability.getFluidInTank(tankIndex)
    }

    override fun getTankMaxAmount(capability: IFluidHandler, tankIndex: Int): Long {
        return capability.getTankCapacity(tankIndex).toLong()
    }

    override fun getTanks(capability: IFluidHandler): Int {
        return capability.tanks
    }

    override fun getKey(): String {
        return "Fluid"
    }

    override fun createFilter(): Filter<Fluid> {
        return FluidFilter()
    }

    override fun getTranslationKey(): String {
        return Translates.ToolTip.Fluid
    }

    override fun getIcon(): ItemStack {
        return ItemStack(ModBlocks.FLUID_PIPE)
    }

    override fun getTransferText(upgrade: Upgrade): Component {
        return TranslatableComponent(Translates.ToolTip.Rate.Fluid, getRate(upgrade))
    }

    override fun getRate(upgrade: Upgrade): Int {
        return Main.SERVER_CONFIG.getPipeValue(CapabilityType.FLUID, upgrade)
    }

    private fun isSimulate(simulate: Boolean): IFluidHandler.FluidAction {
        return if (simulate) {
            IFluidHandler.FluidAction.SIMULATE
        } else {
            IFluidHandler.FluidAction.EXECUTE
        }
    }
}