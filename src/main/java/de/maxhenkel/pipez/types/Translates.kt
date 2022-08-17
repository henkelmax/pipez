package de.maxhenkel.pipez.types

object Translates {
    object ToolTip {
        private const val prefix = "tooltip.pipez"
        object Rate {
            private const val prefix = "${ToolTip.prefix}.rate"
            const val Items = "${prefix}.item"
            const val Energy = "${prefix}.energy"
        }
        const val Energy = "${prefix}.energy"
    }
}