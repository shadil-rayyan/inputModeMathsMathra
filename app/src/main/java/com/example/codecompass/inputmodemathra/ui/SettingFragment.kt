package com.example.codecompass.inputmodemathra.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.codecompass.inputmodemathra.R
import com.example.codecompass.inputmodemathra.databinding.FragmentSettingsBinding
import com.example.codecompass.inputmodemathra.utils.settings.LocaleHelper

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPreferences
    private lateinit var prefsEditor: SharedPreferences.Editor

    private val languageCodeMap = mapOf(
        0 to "default",
        1 to "en",
        2 to "ml"
    )

    private var languageSpinnerInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefsEditor = prefs.edit()

        setupLanguageSpinner()
    }

    private fun setupLanguageSpinner() {
        val currentLang = LocaleHelper.getLanguage(requireContext())
        val selectedIndex = languageCodeMap.entries.find { it.value == currentLang }?.key ?: 0

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.language_levels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.languageSpinner.adapter = adapter
        }


        binding.languageSpinner.setSelection(selectedIndex)

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!languageSpinnerInitialized) {
                    languageSpinnerInitialized = true
                    return
                }

                val selectedLangCode = languageCodeMap[position]
                val currentLang = LocaleHelper.getLanguage(requireContext())

                if (selectedLangCode == "default") {
                    LocaleHelper.setLocale(requireContext(), null)
                    prefsEditor.remove("Locale.Helper.Selected.Language").apply()
                    requireActivity().recreate()
                } else if (selectedLangCode != null && selectedLangCode != currentLang) {
                    LocaleHelper.setLocale(requireContext(), selectedLangCode)
                    prefsEditor.putString("Locale.Helper.Selected.Language", selectedLangCode).apply()
                    requireActivity().recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
