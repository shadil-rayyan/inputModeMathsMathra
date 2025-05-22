package com.example.codecompass.inputmodemathra.ui//package com.zendalona.mathmantra.ui
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import com.zendalona.mathmantra.databinding.FragmentSettingBinding
//import com.zendalona.mathmantra.enums.Difficulty
//import com.zendalona.mathmantra.enums.ThemeMode
//import com.zendalona.mathmantra.utils.LocaleHelper
//import com.zendalona.mathmantra.viewModels.SettingViewModel
//
//class SettingFragment : Fragment() {
//
//    private lateinit var binding: FragmentSettingBinding
//    private lateinit var settingViewModel: SettingViewModel
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentSettingBinding.inflate(inflater, container, false)
//        settingViewModel = ViewModelProvider(requireActivity())[SettingViewModel::class.java]
//
//        // Observers
//        settingViewModel.musicVolume.observe(viewLifecycleOwner) {
//            binding.musicVolumeValue.text = it.toString()
//        }
//
//        settingViewModel.speechVolume.observe(viewLifecycleOwner) {
//            binding.speechVolumeValue.text = it.toString()
//        }
//
//        settingViewModel.fontSize.observe(viewLifecycleOwner) {
//            binding.fontSizeValue.text = it.toString()
//        }
//
//        settingViewModel.backgroundMusicEnabled.observe(viewLifecycleOwner) {
//            binding.backgroundMusicToggle.isChecked = it
//        }
//
//        settingViewModel.difficulty.observe(viewLifecycleOwner) {
//            when (it) {
//                Difficulty.EASY -> binding.difficultyEasy.isChecked = true
//                Difficulty.MEDIUM -> binding.difficultyMedium.isChecked = true
//                Difficulty.HARD -> binding.difficultyHard.isChecked = true
//            }
//        }
//
//        settingViewModel.themeMode.observe(viewLifecycleOwner) {
//            when (it) {
//                ThemeMode.LIGHT -> binding.themeLight.isChecked = true
//                ThemeMode.DARK -> binding.themeDark.isChecked = true
//                ThemeMode.SYSTEM -> binding.themeSystem.isChecked = true
//            }
//        }
//
//        settingViewModel.selectedLanguage.observe(viewLifecycleOwner) { languageCode ->
//            // Avoid infinite loop or crash due to recreation
//            if (LocaleHelper.getPersistedData(requireContext(), "es") != languageCode) {
//                // Apply locale change
//                LocaleHelper.setLocale(requireContext(), languageCode)
//
//                // Recreate the activity to reflect the new language
//                requireActivity().recreate()
//            }
//        }
//
//
//
//        // Click Listeners
//        with(binding) {
//            difficultyEasy.setOnClickListener {
//                settingViewModel.changeDifficulty(Difficulty.EASY)
//            }
//
//            difficultyMedium.setOnClickListener {
//                settingViewModel.changeDifficulty(Difficulty.MEDIUM)
//            }
//
//            difficultyHard.setOnClickListener {
//                settingViewModel.changeDifficulty(Difficulty.HARD)
//            }
//
//            themeLight.setOnClickListener {
//                settingViewModel.changeThemeMode(ThemeMode.LIGHT)
//            }
//
//            themeDark.setOnClickListener {
//                settingViewModel.changeThemeMode(ThemeMode.DARK)
//            }
//
//            themeSystem.setOnClickListener {
//                settingViewModel.changeThemeMode(ThemeMode.SYSTEM)
//            }
//
//            languageEnglish.setOnClickListener {
//                settingViewModel.changeLanguage("en")
//            }
//
//            languageSpanish.setOnClickListener {
//                settingViewModel.changeLanguage("es")
//            }
//
//            backgroundMusicToggle.setOnCheckedChangeListener { _, isChecked ->
//                settingViewModel.setBackgroundMusicEnabled(isChecked)
//            }
//
//            // Volume Buttons
//            musicVolumeIncrease.setOnClickListener {
//                settingViewModel.increaseMusicVolume()
//            }
//
//            musicVolumeDecrease.setOnClickListener {
//                settingViewModel.decreaseMusicVolume()
//            }
//
//            speechVolumeIncrease.setOnClickListener {
//                settingViewModel.increaseSpeechVolume()
//            }
//
//            speechVolumeDecrease.setOnClickListener {
//                settingViewModel.decreaseSpeechVolume()
//            }
//
//            // Font size buttons
//            fontSizeIncrease.setOnClickListener {
//                settingViewModel.increaseFontSize()
//            }
//
//            fontSizeDecrease.setOnClickListener {
//                settingViewModel.decreaseFontSize()
//            }
//
//            resetSettingsButton.setOnClickListener {
//                settingViewModel.resetSettings()
//            }
//        }
//
//        return binding.root
//    }
//}
