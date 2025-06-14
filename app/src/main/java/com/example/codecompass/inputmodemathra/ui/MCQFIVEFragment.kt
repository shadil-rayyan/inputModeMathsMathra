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
import com.example.codecompass.inputmodemathra.databinding.FragmentMCQFiveBinding
import com.example.codecompass.inputmodemathra.enums.Difficulty
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator
import com.example.codecompass.inputmodemathra.utils.TTSUtility
import com.example.codecompass.inputmodemathra.utils.settings.LocaleHelper
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class MCQFiveFragment : Fragment() {

    private var _binding: FragmentMCQFiveBinding? = null
    private val binding get() = _binding!!

    private lateinit var random: RandomValueGenerator
    private lateinit var tts: TTSUtility
    private var correctAnswer = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMCQFiveBinding.inflate(inflater, container, false)
        random = RandomValueGenerator()
        val languageCode = LocaleHelper.getLanguage(requireContext())
        val locale = Locale(languageCode)
        tts = TTSUtility(requireActivity(), locale)
        generateNewQuestion()
        return binding.root
    }

    private fun generateNewQuestion() {
        val topic = random.generateQuestionTopic()
        val (numbers, operator) = when (topic) {
            1 -> random.generateSubtractionValues(Difficulty.EASY) to "-"
            2 -> random.generateMultiplicationValues(Difficulty.EASY) to "×"
            3 -> random.generateDivisionValues(Difficulty.EASY) to "÷"
            else -> random.generateAdditionValues(Difficulty.EASY) to "+"
        }

        correctAnswer = numbers[2]
        val questionText = "${formatNumber(numbers[0])} $operator ${formatNumber(numbers[1])} = ?"
        binding.questionTv.text = questionText
        binding.questionTv.contentDescription = "Question. $questionText. There are five options below."

        Handler(Looper.getMainLooper()).postDelayed({
            binding.questionTv.requestFocus()
            binding.questionTv.announceForAccessibility(questionText)
        }, 500)

        val choices = generateUniqueOptions(correctAnswer)
        val optionButtons = listOf(
            binding.optionA,
            binding.optionB,
            binding.optionC,
            binding.optionD,
            binding.optionE
        )
        val labels = listOf("A", "B", "C", "D", "E")

        for (i in optionButtons.indices) {
            updateOption(optionButtons[i], choices[i], labels[i])
        }
    }

    private fun generateUniqueOptions(correct: Int): List<Int> {
        val uniqueOptions = mutableSetOf(correct)

        while (uniqueOptions.size < 5) {
            val wrongOption = correct + random.generateNumberBetween(-20, 20)
            if (wrongOption != correct && wrongOption >= 0) {
                uniqueOptions.add(wrongOption)
            }
        }

        return uniqueOptions.shuffled()
    }

    private fun updateOption(button: MaterialButton, value: Int, label: String) {
        val formattedValue = formatNumber(value)
        button.text = formattedValue
        button.contentDescription = "Option $label. $formattedValue."
        button.setOnClickListener {
            showResultDialog(value == correctAnswer)
        }
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val message = if (isCorrect) getString(R.string.right_answer) else getString(R.string.wrong_answer)
        val gifResource = if (isCorrect) R.drawable.right else R.drawable.wrong
        tts.speak(message)

        val dialogBinding = DialogResultBinding.inflate(layoutInflater)
        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView)
        dialogBinding.messageTextView.text = message

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            tts.speak(getString(R.string.next_question))
            generateNewQuestion()
        }, 2000)
    }

    private fun formatNumber(value: Int): String {
        val locale: Locale = resources.configuration.locales.get(0)
        return NumberFormat.getInstance(locale).format(value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tts.shutdown()
    }
}
