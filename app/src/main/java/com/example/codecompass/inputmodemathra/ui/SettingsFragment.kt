package com.example.codecompass.inputmodemathra.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.codecompass.inputmodemathra.R
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var languageSpinner: Spinner
    private lateinit var prefs: SharedPreferences

    private val languageMap = mapOf(
        "English" to "en",
        "Hindi" to "hi",
        "French" to "fr",
        "Spanish" to "es",
        "German" to "de"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        prefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        languageSpinner = view.findViewById(R.id.language_spinner)

        val languageNames = languageMap.keys.toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languageNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        // Set previously saved language selection
        val savedLang = prefs.getString("app_language", getDeviceLanguage())
        val savedIndex = languageMap.values.indexOf(savedLang)
        if (savedIndex != -1) {
            languageSpinner.setSelection(savedIndex)
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguageName = languageNames[position]
                val selectedLangCode = languageMap[selectedLanguageName] ?: return

                // Save selection and update locale
                prefs.edit().putString("app_language", selectedLangCode).apply()
                setLocale(selectedLangCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }

    private fun getDeviceLanguage(): String {
        return Locale.getDefault().language
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)

        // Recreate the activity to apply the new language
        requireActivity().recreate()
    }
}
