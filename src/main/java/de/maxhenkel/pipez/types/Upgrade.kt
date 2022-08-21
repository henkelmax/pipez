package de.maxhenkel.pipez.types

enum class Upgrade(val upgradeName:String) {
    NONE("no_upgrade"),
    BASIC("basic"),
    IMPROVED("improved"),
    ADVANCED("advanced"),
    ULTIMATE("ultimate"),
    INFINITY("infinity");

    fun asTier():Int {
        return when (this) {
            BASIC -> 1
            IMPROVED -> 2
            ADVANCED -> 3
            ULTIMATE -> 4
            INFINITY -> 9999
            else -> 0
        }
    }

    companion object {
        fun fromTier(tier: Int):Upgrade {
            return when (tier) {
                1 -> BASIC
                2 -> IMPROVED
                3 -> ADVANCED
                4 -> ULTIMATE
                9999 -> INFINITY
                else -> NONE
            }
        }
    }
}