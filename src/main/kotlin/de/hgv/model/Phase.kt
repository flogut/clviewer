package de.hgv.model

/**
 * @author Florian Gutekunst
 */
enum class Phase {

    GRUPPE_A, GRUPPE_B, GRUPPE_C, GRUPPE_D, GRUPPE_E, GRUPPE_F, GRUPPE_G, GRUPPE_H, ACHTELFINALE, VIERTELFINALE,
    HALBFINALE, FINALE;

    /**
     * Wandelt den Namen der Phase für den Link um (Kleinbuchstaben, Leerzeichen durch Bindestriche ersetzt).
     */
    fun toLink(): String = this.toString().toLowerCase().replace(" ", "-")

    /**
     * Gibt den Namen der Phase in natürlicher Sprache zurück (z.B. GRUPPE_A => Gruppe A).
     */
    override fun toString(): String =
        super.toString()
            .toLowerCase()
            .split("_")
            .joinToString(" ") { it.capitalize() }

    companion object {

        /**
         * Parst den Wert der Phase.
         * @param value Name der Phase in natürlicher Sprache (z.B. Gruppe A)
         */
        fun getValue(value: String): Phase = Phase.valueOf(value.toUpperCase().replace(" ", "_"))
    }
}
