package com.example.codecompass.inputmodemathra.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.codecompass.inputmodemathra.R
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding
import com.example.codecompass.inputmodemathra.databinding.FragmentMCQTenBinding
import com.example.codecompass.inputmodemathra.enums.Difficulty
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator
import com.example.codecompass.inputmodemathra.utils.TTSUtility
import com.example.codecompass.inputmodemathra.utils.settings.LocaleHelper
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.*
class MCQTENFragment : Fragment() {
    private var binding: FragmentMCQTenBinding? = null
    private var random: RandomValueGenerator? = null
    private var tts: TTSUtility? = null
    private var correctAnswer = 0
    private lateinit var currentLocale: Locale
    private lateinit var numberFormatter: NumberFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val languageCode = LocaleHelper.getLanguage(requireContext())
        currentLocale = Locale(languageCode)
        numberFormatter = NumberFormat.getInstance(currentLocale)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMCQTenBinding.inflate(inflater, container, false)
        random = RandomValueGenerator()

        // Initialize and set TTS locale
        tts = TTSUtility(requireActivity(), currentLocale)


        generateNewQuestion()
        return binding!!.root
    }

    private fun generateNewQuestion() {
        val topic = random!!.generateQuestionTopic()
        val numbers: IntArray
        val operator: String

        when (topic) {
            1 -> {
                numbers = random!!.generateSubtractionValues(Difficulty.EASY)
                operator = "-"
            }
            2 -> {
                numbers = random!!.generateMultiplicationValues(Difficulty.EASY)
                operator = "×"
            }
            3 -> {
                numbers = random!!.generateDivisionValues(Difficulty.EASY)
                operator = "÷"
            }
            else -> {
                numbers = random!!.generateAdditionValues(Difficulty.EASY)
                operator = "+"
            }
        }

        correctAnswer = numbers[2]
        val questionText = "${numbers[0]} $operator ${numbers[1]} = ?"
        binding!!.questionTv.text = questionText
        binding!!.questionTv.contentDescription = "Question. $questionText. There are ten options below."

        Handler(Looper.getMainLooper()).postDelayed({
            binding!!.questionTv.requestFocus()
            binding!!.questionTv.announceForAccessibility(questionText)
        }, 500)

        val choices = generateUniqueOptions(correctAnswer)
        val optionButtons = listOf(
            binding!!.optionA, binding!!.optionB, binding!!.optionC, binding!!.optionD,
            binding!!.optionE, binding!!.optionF, binding!!.optionG, binding!!.optionH,
            binding!!.optionI, binding!!.optionJ
        )

        val labels = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")

        for (i in 0..9) {
            updateOption(optionButtons[i], choices[i], labels[i])
        }
    }

    private fun generateUniqueOptions(correct: Int): List<Int> {
        val uniqueOptions = mutableSetOf<Int>()
        uniqueOptions.add(correct)

        while (uniqueOptions.size < 10) {
            val wrongOption = correct + random!!.generateNumberBetween(-20, 20)
            if (wrongOption != correct && wrongOption >= 0) {
                uniqueOptions.add(wrongOption)
            }
        }

        val options = uniqueOptions.toMutableList()
        options.shuffle()
        return options
    }

    private fun updateOption(button: MaterialButton, value: Int, label: String) {
        val formatted = numberFormatter.format(value)
        button.text = formatted
        button.contentDescription = "Option $label. $formatted."
        button.setOnClickListener { showResultDialog(value == correctAnswer) }
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val message = if (isCorrect) "Right Answer" else "Wrong Answer"
        val gifResource = if (isCorrect) R.drawable.right else R.drawable.wrong
        tts!!.speak(message)

        val dialogBinding = DialogResultBinding.inflate(layoutInflater)
        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView)
        dialogBinding.messageTextView.text = message

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            speakNumber(correctAnswer)
            tts!!.speak("Next Question")
            generateNewQuestion()
        }, 2000)
    }

    private fun speakNumber(number: Int) {
        val formattedNumber = numberFormatter.format(number)
        tts!!.speak(formattedNumber)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        tts!!.shutdown()
    }
}
