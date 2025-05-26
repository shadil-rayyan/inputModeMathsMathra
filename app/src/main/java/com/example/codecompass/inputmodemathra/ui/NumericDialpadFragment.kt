package com.example.codecompass.inputmodemathra.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.codecompass.inputmodemathra.R
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding
import com.example.codecompass.inputmodemathra.databinding.FragmentNumericDialpadBinding
import com.example.codecompass.inputmodemathra.enums.Difficulty
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator

class NumericDialpadFragment : Fragment() {

    private var _binding: FragmentNumericDialpadBinding? = null
    private val binding get() = _binding!!
    private lateinit var random: RandomValueGenerator

    private var currentInput: StringBuilder = StringBuilder()
    private var correctAnswer: Int = 0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNumericDialpadBinding.inflate(inflater, container, false)
        random = RandomValueGenerator()

        setupNumpad()
        generateNewQuestion()

        binding.submitBtn.setOnClickListener {
            val userAnswer = currentInput.toString().toIntOrNull()
            val isCorrect = userAnswer == correctAnswer
            showResultDialog(isCorrect)
        }

        return binding.root
    }

    private fun setupNumpad() {
        val buttonIds = listOf(
                binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
                binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        buttonIds.forEachIndexed { number, button ->
                button.setOnClickListener {
            currentInput.append(number)
            binding.answerDisplay.text = currentInput.toString()
        }
        }
    }

    private fun generateNewQuestion() {
        val numbers = random.generateAdditionValues(Difficulty.EASY)
        correctAnswer = numbers[2]
        val questionText = "${numbers[0]} + ${numbers[1]} = ?"
        binding.questionTv.text = questionText
        binding.questionTv.contentDescription =
                "Math question. ${numbers[0]} plus ${numbers[1]} equals what? Double tap to repeat the question."

        binding.questionTv.post {
            binding.questionTv.announceForAccessibility(binding.questionTv.contentDescription)
        }

        // Reset answer display
        currentInput.clear()
        binding.answerDisplay.text = ""
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val message = if (isCorrect) "Right Answer" else "Wrong Answer"
        val gifRes = if (isCorrect) R.drawable.right else R.drawable.wrong

        val inflater = layoutInflater
        val dialogBinding = DialogResultBinding.inflate(inflater)
        val dialogView = dialogBinding.root

        Glide.with(this)
                .asGif()
                .load(gifRes)
                .into(dialogBinding.gifImageView)

        dialogBinding.messageTextView.text = message

        val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
        if (dialog.isShowing) dialog.dismiss()
        generateNewQuestion()
        }, 4000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
