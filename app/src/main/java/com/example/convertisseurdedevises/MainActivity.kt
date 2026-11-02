package com.example.convertisseurdedevises

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.convertisseurdedevises.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Taux fixes exprimés pour 1 EUR (l'euro est la devise pivot)
    private val taux = mapOf(
        "EUR" to 1.0, "USD" to 1.17, "TND" to 3.40,
        "GBP" to 0.87, "JPY" to 172.50, "CAD" to 1.62
    )

    // Nombre maximal de caractères dans le montant
    private val longueurMaxMontant = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurerSpinners()
        configurerClavier()

        binding.btnConvertir.setOnClickListener { lancerConversion() }
        binding.btnInverser.setOnClickListener { inverserDevises() }
    }

    /** Remplit les deux Spinner depuis R.array.devises (USD en source, EUR en cible). */
    private fun configurerSpinners() {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.devises, R.layout.spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spDeviseSource.adapter = adapter
        binding.spDeviseCible.adapter = adapter

        // Positions par défaut d'après l'ordre du tableau : USD = 0, EUR = 1
        binding.spDeviseSource.setSelection(0)
        binding.spDeviseCible.setSelection(1)
    }

    /** Associe chaque touche du clavier à son action. */
    private fun configurerClavier() {
        val touches = mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9", binding.btnPoint to "."
        )
        touches.forEach { (bouton, caractere) ->
            bouton.setOnClickListener { ajouterCaractere(caractere) }
        }
        binding.btnClear.setOnClickListener { reinitialiser() }
    }

    /** Ajoute un chiffre ou un point à la fin du montant, en respectant les règles de gestion. */
    private fun ajouterCaractere(c: String) {
        val montantActuel = binding.tvMontant.text.toString()

        // Un seul point décimal autorisé
        if (c == "." && montantActuel.contains(".")) return

        // Le « 0 » initial est remplacé par le premier chiffre (sauf si on tape « . » → « 0. »)
        if (montantActuel == "0" && c != ".") {
            binding.tvMontant.text = c
            return
        }

        // Limite de 10 caractères
        if (montantActuel.length >= longueurMaxMontant) return

        binding.tvMontant.text = montantActuel + c
    }

    /** Touche « C » : montant remis à 0 et résultat vidé. */
    private fun reinitialiser() {
        binding.tvMontant.text = getString(R.string.montant_initial)
        binding.tvResultat.text = ""
    }

    /** Lit l'interface, vérifie le montant puis affiche le résultat. */
    private fun lancerConversion() {
        val montant = binding.tvMontant.text.toString().toDoubleOrNull() ?: 0.0

        if (montant == 0.0) {
            Toast.makeText(this, R.string.toast_montant_vide, Toast.LENGTH_SHORT).show()
            return
        }

        val source = binding.spDeviseSource.selectedItem.toString()
        val cible = binding.spDeviseCible.selectedItem.toString()
        val resultat = convertir(montant, source, cible)

        // Locale.US pour garantir le point décimal (et non la virgule) quelle que soit la langue du téléphone
        binding.tvResultat.text = String.format(Locale.US, "%.2f", resultat)
    }

    /** Conversion via l'euro : montant / taux(source) × taux(cible). */
    private fun convertir(montant: Double, source: String, cible: String): Double {
        if (source == cible) return montant
        val tauxSource = taux.getValue(source)
        val tauxCible = taux.getValue(cible)
        val montantEnEuro = montant / tauxSource
        return montantEnEuro * tauxCible
    }

    /** Échange les deux devises et relance la conversion si un résultat est déjà affiché. */
    private fun inverserDevises() {
        val positionSource = binding.spDeviseSource.selectedItemPosition
        val positionCible = binding.spDeviseCible.selectedItemPosition

        binding.spDeviseSource.setSelection(positionCible)
        binding.spDeviseCible.setSelection(positionSource)

        if (binding.tvResultat.text.isNotEmpty()) {
            lancerConversion()
        }
    }
}