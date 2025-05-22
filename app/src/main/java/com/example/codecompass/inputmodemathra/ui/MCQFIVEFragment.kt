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
import com.google.android.material.button.MaterialButton
import java.util.Collections
class MCQFiveFragment : Fragment() {
    private var binding: FragmentMCQFiveBinding? = null
    private var random: RandomValueGenerator? = null
    private var tts: TTSUtility? = null
    private var correctAnswer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMCQFiveBinding.inflate(inflater, container, false)
        random = RandomValueGenerator()
        tts = TTSUtility(requireActivity())
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
        binding!!.questionTv.contentDescription =
            "Question. $questionText. Double tap to repeat. There are five options below."

        Handler(Looper.getMainLooper()).postDelayed({
            binding!!.questionTv.requestFocus()
            binding!!.questionTv.announceForAccessibility(questionText)
        }, 500)

        val choices = generateUniqueOptions(correctAnswer)
        val optionButtons = listOf(
            binding!!.optionA,
            binding!!.optionB,
            binding!!.optionC,
            binding!!.optionD,
            binding!!.optionE
        )

        val labels = listOf("A", "B", "C", "D", "E")

        for (i in 0 until 5) {
            updateOption(optionButtons[i], choices[i], labels[i])
        }
    }

    private fun generateUniqueOptions(correct: Int): List<Int> {
        val uniqueOptions = mutableSetOf<Int>()
        uniqueOptions.add(correct)

        while (uniqueOptions.size < 5) {
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
        button.text = value.toString()
        button.contentDescription = "Option $label. $value. Double tap to select."
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
            tts!!.speak("Next Question")
            generateNewQuestion()
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        tts!!.shutdown()
    }
}
