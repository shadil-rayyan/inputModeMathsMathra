package com.example.codecompass.inputmodemathra.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.codecompass.inputmodemathra.R
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding
import com.example.codecompass.inputmodemathra.databinding.FragmentSpinnerBinding
import com.example.codecompass.inputmodemathra.enums.Difficulty
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator
import com.example.codecompass.inputmodemathra.utils.TTSUtility

class SpinnerFragment : Fragment() {

    private var _binding: FragmentSpinnerBinding? = null
    private val binding get() = _binding!!

    private lateinit var random: RandomValueGenerator
    private lateinit var tts: TTSUtility
    private var correctAnswer: Int = 0
    private val numberPickers = mutableListOf<NumberPicker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        random = RandomValueGenerator()
        tts = TTSUtility(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpinnerBinding.inflate(inflater, container, false)
        generateNewQuestion()
        return binding.root
    }

    private fun generateNewQuestion() {
        // Generate a simple addition question
        val values = random.generateAdditionValues(Difficulty.EASY)
        val questionText = "${values[0]} + ${values[1]} = ?"
        correctAnswer = values[2]

        // Set question and announce for accessibility
        binding.questionTv.text = questionText
        binding.questionTv.contentDescription = "Question: $questionText. Double tap to repeat."

        Handler(Looper.getMainLooper()).postDelayed({
            binding.questionTv.requestFocus()
            binding.questionTv.announceForAccessibility(questionText)
        }, 500)

        // Clear old pickers
        binding.spinnerContainer.removeAllViews()
        numberPickers.clear()

        val digitCount = correctAnswer.toString().length

        for (i in 0 until digitCount) {
            val numberPicker = NumberPicker(requireContext()).apply {
                minValue = 0
                maxValue = 9
                wrapSelectorWheel = true
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 0, 16, 0)
                }
                contentDescription = "Digit ${i + 1}. Swipe up or down to select number."
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                setFormatter { it.toString() }
            }

            binding.spinnerContainer.addView(numberPicker)
            numberPickers.add(numberPicker)
        }

        binding.submitBtn.setOnClickListener {
            val userInput = numberPickers.joinToString("") { it.value.toString() }
            val isCorrect = userInput == correctAnswer.toString()
            showResultDialog(isCorrect)
        }
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val message = if (isCorrect) "Right Answer" else "Wrong Answer"
        val gifResource = if (isCorrect) R.drawable.right else R.drawable.wrong

        tts.speak(message)

        val dialogBinding = DialogResultBinding.inflate(layoutInflater)
        dialogBinding.messageTextView.text = message
        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            tts.speak("Next question")
            generateNewQuestion()
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tts.shutdown()
    }
}
