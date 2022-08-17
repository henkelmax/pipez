package de.maxhenkel.pipez.types

enum class Upgrade(val upgradeName:String) {
    NONE("no_upgrade"),
    BASIC("basic"),
    IMPROVED("improved"),
    ADVANCED("advanced"),
    ULTIMATE("ultimate"),
    INFINITY("infinity"),
}