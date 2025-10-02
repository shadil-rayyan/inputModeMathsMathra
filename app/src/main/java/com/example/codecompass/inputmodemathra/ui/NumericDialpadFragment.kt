package com.example.codecompass.inputmodemathra.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.LocaleSpan
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
import com.example.codecompass.inputmodemathra.utils.common.AccessibilityLocaleWrapper
import com.example.codecompass.inputmodemathra.utils.settings.LocaleHelper
import java.text.NumberFormat
import java.util.Locale

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
        val buttonMap = mapOf(
            binding.btn0 to "0",
            binding.btn1 to "1",
            binding.btn2 to "2",
            binding.btn3 to "3",
            binding.btn4 to "4",
            binding.btn5 to "5",
            binding.btn6 to "6",
            binding.btn7 to "7",
            binding.btn8 to "8",
            binding.btn9 to "9"
        )

        val langCode = LocaleHelper.getLanguage(requireContext())
        val currentLocale = Locale(langCode)
        val nf = NumberFormat.getInstance(currentLocale)

        buttonMap.forEach { (button, digitStr) ->
            // Show digit as-is visually
            button.text = digitStr

            // Localized spoken digit (in Malayalam or current locale)
            val localizedSpokenDigit = nf.format(digitStr.toInt())

            // Use your helper function to set contentDescription with LocaleSpan
            AccessibilityLocaleWrapper.setLocalizedContentDescription(requireContext(), button, localizedSpokenDigit)

            button.setOnClickListener {
                currentInput.append(digitStr)
                binding.answerDisplay.text = currentInput.toString()
            }
        }

        // Also set for submit button (optional)
        AccessibilityLocaleWrapper.setLocalizedContentDescription(requireContext(), binding.submitBtn, getString(R.string.desc_submit))
    }



    private fun generateNewQuestion() {
        val numbers = random.generateAdditionValues(Difficulty.EASY)
        correctAnswer = numbers[2]

        val languageCode = LocaleHelper.getLanguage(requireContext())
        val currentLocale = Locale(languageCode)
        val nf = NumberFormat.getInstance(currentLocale)
        val num1Str = nf.format(numbers[0])
        val num2Str = nf.format(numbers[1])

        val questionText = getString(R.string.question_format, num1Str, num2Str)
        binding.questionTv.text = questionText

        val rawDesc = getString(R.string.question_desc, num1Str, num2Str)
        val spannableDesc = SpannableString(rawDesc).apply {
            setSpan(LocaleSpan(currentLocale), 0, rawDesc.length, 0)
        }

        binding.questionTv.contentDescription = spannableDesc

        binding.questionTv.post {
            binding.questionTv.announceForAccessibility(spannableDesc)
        }

        currentInput.clear()
        binding.answerDisplay.text = ""
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val langCode = LocaleHelper.getLanguage(requireContext())
        val locale = Locale(langCode)

        val message = if (isCorrect)
            getString(R.string.right_answer) else getString(R.string.wrong_answer)

        val spannableMessage = SpannableString(message).apply {
            setSpan(LocaleSpan(locale), 0, message.length, 0)
        }

        val gifRes = if (isCorrect) R.drawable.right else R.drawable.wrong

        val dialogBinding = DialogResultBinding.inflate(layoutInflater)
        val dialogView = dialogBinding.root

        Glide.with(this)
            .asGif()
            .load(gifRes)
            .into(dialogBinding.gifImageView)

        dialogBinding.messageTextView.text = spannableMessage
        dialogBinding.messageTextView.contentDescription = spannableMessage

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
