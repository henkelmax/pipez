package de.maxhenkel.pipez.utils

import de.maxhenkel.pipez.types.PipeSide
import net.minecraft.core.Direction
import net.minecraftforge.common.util.LazyOptional
import thedarkcolour.kotlinforforge.kotlin.enumMapOf
import java.util.function.Consumer
import java.util.function.Function

class DirectionalLazyOptionalCache<T : Any> {
    private var cache: MutableMap<PipeSide, LazyOptional<T>> = enumMapOf()

    operator fun get(side: PipeSide): LazyOptional<T> {
        return cache.computeIfAbsent(side) { LazyOptional.empty() }
    }

    @Deprecated(
        message = "Deprecated by using Direction. Please use PipeSide instead.",
    )
    operator fun get(side: Direction): LazyOptional<T> {
        return cache.computeIfAbsent(PipeSide.fromDirection(side)) { LazyOptional.empty() }
    }

    @Deprecated(
        message = "Deprecated by using Direction. Please use PipeSide instead.",
    )
    fun revalidate(side: Direction, validFunction: Function<Direction?, Boolean>, cachePopulator: Function<Direction?, T>) {
        val pipeSide = PipeSide.fromDirection(side)
        cache[pipeSide]?.invalidate()
        cache[pipeSide] = if (validFunction.apply(side)) {
            LazyOptional.of {
                cachePopulator.apply(side)
            }
        } else {
            LazyOptional.empty()
        }
    }

    fun revalidate(side: PipeSide, validFunction: (PipeSide) -> Boolean, cachePopulator: (PipeSide) -> T) {
        cache[side]?.invalidate()
        cache[side] = if (validFunction(side)) {
            LazyOptional.of {
                cachePopulator(side)
            }
        } else {
            LazyOptional.empty()
        }
    }

    @Deprecated(
        message = "Deprecated by using Direction. Please use PipeSide instead.",
    )
    fun revalidate(validFunction: Function<Direction?, Boolean>, cachePopulator: Function<Direction?, T>) {
        for (side in Direction.values()) {
            revalidate(side, validFunction, cachePopulator)
        }
    }

    fun revalidate(validFunction: (PipeSide) -> Boolean, cachePopulator: (PipeSide) -> T) {
        for (side in PipeSide.values()) {
            revalidate(side, validFunction, cachePopulator)
        }
    }

    fun invalidate() {
        cache.values.forEach(Consumer { obj: LazyOptional<T> -> obj.invalidate() })
    }
}