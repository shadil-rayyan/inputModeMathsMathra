package com.example.codecompass.inputmodemathra.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.codecompass.inputmodemathra.R
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding
import com.example.codecompass.inputmodemathra.databinding.FragmentMathQuizBinding
import com.example.codecompass.inputmodemathra.enums.Difficulty
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator
import java.text.NumberFormat
import java.util.Locale

class MathQuizFragment : Fragment() {

    private var _binding: FragmentMathQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var random: RandomValueGenerator
    private var currentAnswer = 0
    private var questionCount = 0
    private val totalQuestions = 5

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMathQuizBinding.inflate(inflater, container, false)
        random = RandomValueGenerator()

        setupListeners()
        generateNewQuestion()

        return binding.root
    }

    private fun setupListeners() {
        binding.answerEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAnswer()
                true
            } else false
        }

        binding.submitAnswerBtn.setOnClickListener {
            submitAnswer()
        }

        val repeatQuestion = {
            binding.questionTv.announceForAccessibility("Repeating. ${binding.questionTv.text}")
        }
        binding.questionTv.setOnClickListener { repeatQuestion() }
        binding.questionTv.setOnLongClickListener {
            repeatQuestion()
            true
        }
    }

    private fun submitAnswer() {
        val userInput = binding.answerEt.text.toString()
        val userAnswer = try {
            NumberFormat.getInstance(getCurrentLocale()).parse(userInput)?.toInt()
        } catch (e: Exception) {
            null
        } ?: return

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

        val formattedFirst = formatNumber(numbers[0])
        val formattedSecond = formatNumber(numbers[1])

        val questionText = "$formattedFirst + $formattedSecond = ?"
        val questionDescription = "Math question. $formattedFirst plus $formattedSecond equals what?"

        binding.questionTv.text = questionText
        binding.questionTv.contentDescription = questionDescription
        binding.questionTv.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE

        binding.answerEt.setText("")

        binding.questionTv.post {
            binding.questionTv.announceForAccessibility(questionDescription)

            binding.answerEt.postDelayed({
                binding.answerEt.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.answerEt, InputMethodManager.SHOW_IMPLICIT)
            }, 1200)
        }
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val dialogBinding = DialogResultBinding.inflate(layoutInflater)
        val message = if (isCorrect) getString(R.string.right_answer) else getString(R.string.wrong_answer)
        val gifRes = if (isCorrect) R.drawable.right else R.drawable.wrong

        dialogBinding.messageTextView.text = message
        Glide.with(this).asGif().load(gifRes).into(dialogBinding.gifImageView)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                if (isCorrect) {
                    questionCount++
                    binding.questionTv.postDelayed({
                        binding.questionTv.announceForAccessibility(getString(R.string.next_question))
                        generateNewQuestion()
                    }, 300)
                } else {
                    binding.questionTv.postDelayed({
                        binding.questionTv.announceForAccessibility("Try again. ${binding.questionTv.text}")
                    }, 300)
                }
            }
        }, 1500)
    }

    private fun formatNumber(value: Int): String {
        return NumberFormat.getInstance(getCurrentLocale()).format(value)
    }

    private fun getCurrentLocale(): Locale {
        return resources.configuration.locales[0]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
