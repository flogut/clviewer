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

    /**
     * Prüft, ob diese Phase vor der als Parameter übergebenen Phase stattfindet.
     */
    fun bevor(phase: Phase): Boolean = if (this.ordinal < 7 && phase.ordinal < 7) {
        false
    } else {
        this.ordinal < phase.ordinal
    }

    /**
     * Gibt die Phase zurück, die zeitlich als nächstes Stattfindet.
     * @return Die nächste Phase, oder null wenn die aktuelle Phase das Finale ist
     */
    fun naechste(): Phase? = when (this) {
        ACHTELFINALE -> VIERTELFINALE
        VIERTELFINALE -> HALBFINALE
        HALBFINALE -> FINALE
        FINALE -> null
        else -> ACHTELFINALE
    }

    companion object {

        /**
         * Parst den Wert der Phase.
         * @param value Name der Phase in natürlicher Sprache (z.B. Gruppe A)
         */
        fun getValue(value: String): Phase = Phase.valueOf(value.toUpperCase().replace(" ", "_"))
    }
}
