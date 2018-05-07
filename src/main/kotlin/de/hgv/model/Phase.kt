package de.hgv.model

enum class Phase {

    GRUPPE_A, GRUPPE_B, GRUPPE_C, GRUPPE_D, GRUPPE_E, GRUPPE_F, GRUPPE_G, GRUPPE_H, ACHTELFINALE, VIERTELFINALE, HALBFINALE, FINALE;

    fun toLink(): String = this.toString().toLowerCase().replace(" ", "-")

    override fun toString(): String = super.toString().toLowerCase().split("_").joinToString(" ") { it.capitalize() }

    companion object {

        fun getValue(value: String): Phase {
            return Phase.valueOf(value.toUpperCase().replace(" ", "_"))
        }

    }

}