package de.hgv.controller

import de.hgv.download.ParsingProvider
import de.hgv.download.Provider
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

class DataController(provider: Provider = ParsingProvider()): Controller() {
    val providerProperty = SimpleObjectProperty<Provider>(provider)
    var provider by providerProperty
}
