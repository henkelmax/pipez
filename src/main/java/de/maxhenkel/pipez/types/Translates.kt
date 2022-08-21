package de.maxhenkel.pipez.types

object Translates {
    object ToolTip {
        private const val prefix = "tooltip.pipez"
        object Rate {
            private const val prefix = "${ToolTip.prefix}.rate"
            const val Item = "${prefix}.item"
            const val Fluid = "${prefix}.fluid"
            const val Energy = "${prefix}.energy"
            const val Gas = "${prefix}.gas"
        }
        const val Energy = "${prefix}.energy"
        const val Fluid = "${prefix}.fluid"
        const val Gas = "${prefix}.gas"
        const val Item = "${prefix}.item"
    }
}