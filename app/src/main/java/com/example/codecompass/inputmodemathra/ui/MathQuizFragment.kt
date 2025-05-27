package com.example.codecompass.inputmodemathra.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.codecompass.inputmodemathra.R
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding
import com.example.codecompass.inputmodemathra.databinding.FragmentMathQuizBinding
import com.example.codecompass.inputmodemathra.enums.Difficulty
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator

class MathQuizFragment : Fragment() {

    private lateinit var binding: FragmentMathQuizBinding
    private lateinit var random: RandomValueGenerator
    private var currentAnswer = 0
    private var questionCount = 0
    private val totalQuestions = 5

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMathQuizBinding.inflate(inflater, container, false)
        random = RandomValueGenerator()

        setupListeners()
        generateNewQuestion()
        return binding.root
    }

    private fun setupListeners() {
        // When "Done" is pressed on dial pad
        binding.answerEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAnswer()
                true
            } else {
                false
            }
        }

        // On click Submit
        binding.submitAnswerBtn.setOnClickListener {
            submitAnswer()
        }
    }

    private fun submitAnswer() {
        val answerText = binding.answerEt.text.toString()
        if (answerText.isEmpty()) return

        val userAnswer = answerText.toIntOrNull()
        val isCorrect = userAnswer == currentAnswer
        showResultDialog(isCorrect)
    }

    private fun generateNewQuestion() {
        if (questionCount >= totalQuestions) {
            // Go back to previous screen or home
            requireActivity().finish() // or any other navigation logic you have
            return
        }

        val numbers = random.generateAdditionValues(Difficulty.EASY)
        currentAnswer = numbers[2]
        val questionText = "${numbers[0]} + ${numbers[1]} = ?"
        val questionDescription = "Math question. ${numbers[0]} plus ${numbers[1]} equals what? Double tap to repeat."

        binding.questionTv.text = questionText
        binding.questionTv.contentDescription = questionDescription
        binding.answerEt.setText("")
        binding.answerEt.requestFocus()
        binding.questionTv.post {
            binding.questionTv.announceForAccessibility(questionDescription)
        }
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val inflater = layoutInflater
        val dialogBinding = DialogResultBinding.inflate(inflater)
        val dialogView = dialogBinding.root

        dialogBinding.messageTextView.text = if (isCorrect) "Right Answer" else "Wrong Answer"
        val gifRes = if (isCorrect) R.drawable.right else R.drawable.wrong

        Glide.with(this)
            .asGif()
            .load(gifRes)
            .into(dialogBinding.gifImageView)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                if (isCorrect) {
                    questionCount++
                    binding.questionTv.postDelayed({
                        binding.questionTv.announceForAccessibility("Next question.")
                        generateNewQuestion()
                    }, 300)
                } else {
                    // Replay same question
                    binding.questionTv.postDelayed({
                        binding.questionTv.announceForAccessibility("Try again. ${binding.questionTv.text}")
                    }, 300)
                }
            }
        }, 1500) // 1.5 seconds for animation
    }
}
