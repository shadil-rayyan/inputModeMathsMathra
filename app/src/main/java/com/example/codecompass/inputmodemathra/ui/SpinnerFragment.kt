package com.example.codecompass.inputmodemathra.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.codecompass.inputmodemathra.R
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding
import com.example.codecompass.inputmodemathra.databinding.FragmentSpinnerBinding
import com.example.codecompass.inputmodemathra.enums.Difficulty
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator
import com.example.codecompass.inputmodemathra.utils.TTSUtility
import com.example.codecompass.inputmodemathra.utils.settings.LocaleHelper
import java.text.NumberFormat
import java.util.Locale

class SpinnerFragment : Fragment() {

    private var _binding: FragmentSpinnerBinding? = null
    private val binding get() = _binding!!

    private lateinit var random: RandomValueGenerator
    private lateinit var tts: TTSUtility
    private var correctAnswer: Int = 0
    private val numberPickers = mutableListOf<NumberPicker>()

    // Gesture detector for custom swipe navigation
    private lateinit var gestureDetector: GestureDetectorCompat
    private var focusedIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        random = RandomValueGenerator()
        val languageCode = LocaleHelper.getLanguage(requireContext())
        val locale = Locale(languageCode)
        tts = TTSUtility(requireActivity(), locale)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpinnerBinding.inflate(inflater, container, false)

        setupGestureDetection()

        generateNewQuestion()
        return binding.root
    }

    private fun setupGestureDetection() {
        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Swipe right - move focus to previous picker
                            moveFocusToPrevious()
                        } else {
                            // Swipe left - move focus to next picker
                            moveFocusToNext()
                        }
                        return true
                    }
                }
                return false
            }
        })

        // Attach touch listener to the container holding the NumberPickers
        binding.spinnerContainer.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun moveFocusToNext() {
        if (numberPickers.isEmpty()) return
        focusedIndex = (focusedIndex + 1) % numberPickers.size
        numberPickers[focusedIndex].requestFocus()
        announceFocusedPicker()
    }

    private fun moveFocusToPrevious() {
        if (numberPickers.isEmpty()) return
        focusedIndex = if (focusedIndex - 1 < 0) numberPickers.size - 1 else focusedIndex - 1
        numberPickers[focusedIndex].requestFocus()
        announceFocusedPicker()
    }

    private fun announceFocusedPicker() {
        val picker = numberPickers[focusedIndex]
        val announcement = "Digit ${focusedIndex + 1}, currently ${picker.value}"
        tts.speak(announcement)
    }

    private fun generateNewQuestion() {
        val values = random.generateAdditionValues(Difficulty.EASY)
        correctAnswer = values[2]

        // Get current locale
        val languageCode = LocaleHelper.getLanguage(requireContext())
        val locale = Locale(languageCode)
        val nf = NumberFormat.getInstance(locale)

        val formattedNum1 = nf.format(values[0])
        val formattedNum2 = nf.format(values[1])
        val questionText = "$formattedNum1 + $formattedNum2 = ?"

        val spokenQuestion = "${numberToSpokenDigits(values[0])} plus ${numberToSpokenDigits(values[1])} equals question mark"

        binding.questionTv.text = questionText
        binding.questionTv.contentDescription = "Question: $spokenQuestion."

        Handler(Looper.getMainLooper()).postDelayed({
            binding.questionTv.requestFocus()
            binding.questionTv.announceForAccessibility(spokenQuestion)
        }, 500)

        binding.spinnerContainer.removeAllViews()
        numberPickers.clear()
        focusedIndex = 0

        val digitCount = correctAnswer.toString().length

        for (i in 0 until digitCount) {
            val digitPosition = i
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
                contentDescription = "Digit $digitPosition. Swipe up or down to select number."
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                setFormatter { nf.format(it.toLong()) }

                setOnValueChangedListener { _, _, newVal ->
                    tts.speak("Digit $digitPosition: ${nf.format(newVal.toLong())}")
                }

                isFocusable = true
                isFocusableInTouchMode = true
            }

            binding.spinnerContainer.addView(numberPicker)
            numberPickers.add(numberPicker)
        }

        numberPickers.getOrNull(0)?.apply {
            requestFocus()
            tts.speak("Digit 1, currently ${nf.format(value.toLong())}")
        }

        binding.submitBtn.setOnClickListener {
            val userInputDigits = numberPickers.mapIndexed { index, picker ->
                "Digit ${index + 1}: ${nf.format(picker.value.toLong())}"
            }
            val spokenInput = userInputDigits.joinToString(", ")

            tts.speak("You entered: $spokenInput")

            val userInput = numberPickers.joinToString("") { it.value.toString() }
            val isCorrect = userInput == correctAnswer.toString()
            showResultDialog(isCorrect)
        }
    }

    private fun numberToSpokenDigits(number: Int): String {
        return number.toString().map { it.toString() }.joinToString(" ")
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
