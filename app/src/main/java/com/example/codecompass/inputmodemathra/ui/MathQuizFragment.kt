package com.example.codecompass.inputmodemathra.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
        // Submit on IME action (Done on keyboard)
        binding.answerEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAnswer()
                true
            } else false
        }

        // Submit on button click
        binding.submitAnswerBtn.setOnClickListener {
            submitAnswer()
        }

        // Repeat question on tap or long press
        binding.questionTv.setOnClickListener {
            binding.questionTv.announceForAccessibility("Repeating. ${binding.questionTv.text}")
        }
        binding.questionTv.setOnLongClickListener {
            binding.questionTv.announceForAccessibility("Repeating. ${binding.questionTv.text}")
            true
        }
    }

    private fun submitAnswer() {
        val answerText = binding.answerEt.text.toString()
        val userAnswer = answerText.toIntOrNull() ?: return
        val isCorrect = userAnswer == currentAnswer
        showResultDialog(isCorrect)
    }

    private fun generateNewQuestion() {
        if (questionCount >= totalQuestions) {
            parentFragmentManager.popBackStack()
            return
        }

        val numbers = random.generateAdditionValues(Difficulty.EASY)
        currentAnswer = numbers[2]
        val questionText = "${numbers[0]} + ${numbers[1]} = ?"
        val questionDescription = "Math question. ${numbers[0]} plus ${numbers[1]} equals what?"

        binding.questionTv.text = questionText
        binding.questionTv.contentDescription = questionDescription
        binding.questionTv.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE // less aggressive

        binding.answerEt.setText("")

        // Step 1: Announce question first without forcing focus
        binding.questionTv.post {
            binding.questionTv.announceForAccessibility(questionDescription)

            // Step 2: Then delay keyboard opening after announcement
            binding.answerEt.postDelayed({
                binding.answerEt.requestFocus()

                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.answerEt, InputMethodManager.SHOW_IMPLICIT)
            }, 1200) // delay longer to allow TalkBack to finish speaking
        }
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val inflater = layoutInflater
        val dialogBinding = DialogResultBinding.inflate(inflater)
        val dialogView = dialogBinding.root

        val message = if (isCorrect) "Right Answer" else "Wrong Answer"
        val gifRes = if (isCorrect) R.drawable.right else R.drawable.wrong

        dialogBinding.messageTextView.text = message
        Glide.with(this).asGif().load(gifRes).into(dialogBinding.gifImageView)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
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
                    binding.questionTv.postDelayed({
                        binding.questionTv.announceForAccessibility("Try again. ${binding.questionTv.text}")
                    }, 300)
                }
            }
        }, 1500) // 1.5 seconds dialog animation time
    }
}
