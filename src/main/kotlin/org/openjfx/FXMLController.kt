package org.openjfx

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import java.net.URL
import java.util.*

class FXMLController : Initializable {
    @FXML
    private val label: Label? = null

    override fun initialize(url: URL, rb: ResourceBundle) {
        val javaVersion   = System.getProperty("java.version")
        val javafxVersion = System.getProperty("javafx.version")
        label!!.text = "Hello, JavaFX $javafxVersion\nRunning on KOTLIN $javaVersion."
    }
}