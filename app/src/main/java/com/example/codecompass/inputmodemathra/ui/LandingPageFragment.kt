package com.example.codecompass.inputmodemathra.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.codecompass.inputmodemathra.databinding.FragmentLandingpageBinding
import com.example.codecompass.inputmodemathra.utils.FragmentNavigation
import android.os.Build

class LandingPageFragment : Fragment() {
    private var binding: FragmentLandingpageBinding? = null
    private var navigationListener: FragmentNavigation? = null

    private fun persist(context: Context, language: String) {
        val preferences = context.getSharedPreferences("LocalePrefs", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("SELECTED_LANGUAGE", language)
        editor.apply()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentNavigation) navigationListener = context
        else throw RuntimeException("$context must implement FragmentNavigation")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLandingpageBinding.inflate(inflater, container, false)

        binding!!.dialpadButton.setOnClickListener { v ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    MathQuizFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        }

        binding!!.fourmcqButton.setOnClickListener { v ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    MCQFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        }
//
        binding!!.fivemcqButton.setOnClickListener { v ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    MCQFiveFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        }

        binding!!.tenmcqButton.setOnClickListener { v ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    MCQTENFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        }

        binding!!.spinnersButton.setOnClickListener { v ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    SpinnerFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        }


        binding!!.numericDialButton.setOnClickListener { v ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    NumericDialpadFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        }
        binding!!.SettingsButtons.setOnClickListener { v ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    SettingFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        }
        //Quit the app button
        binding!!.quitButton.setOnClickListener { v ->
            requireActivity().finish()
        }




        return binding!!.root
    }
}