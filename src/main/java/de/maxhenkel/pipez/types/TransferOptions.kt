package de.maxhenkel.pipez.types

import de.maxhenkel.pipez.Filter
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity.Distribution

data class TransferOptions<T>(
    val upgrade: Upgrade,
    val distribution: Distribution,
    val localFilters: List<Filter<T>>,
    val invertFilter: Boolean,
    val globalCanInsert: Boolean,
    val simulate: Boolean,
) {
    val nearestFirst = distribution != Distribution.FURTHEST
}