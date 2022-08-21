package de.maxhenkel.pipez.utils

import de.maxhenkel.pipez.Filter

object FilterUtil {
    /**
     * Separate source / destination filter
     */
    fun <T> separateFilter(filters: List<Filter<T>>): Pair<List<Filter<T>>, List<Filter<T>>> {
        val sourceList = mutableListOf<Filter<T>>()
        val destList = mutableListOf<Filter<T>>()
        for (filter in filters) {
            if (filter.destination == null) {
                sourceList.add(filter)
            } else {
                destList.add(filter)
            }
        }
        return Pair(sourceList, destList)
    }
}