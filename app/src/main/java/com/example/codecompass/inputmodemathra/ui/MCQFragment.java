package com.example.codecompass.inputmodemathra.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.LocaleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;

import com.bumptech.glide.Glide;
import com.example.codecompass.inputmodemathra.R;
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding;
import com.example.codecompass.inputmodemathra.databinding.FragmentMCQBinding;
import com.example.codecompass.inputmodemathra.enums.Difficulty;
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator;
import com.example.codecompass.inputmodemathra.utils.settings.LocaleHelper;
import com.example.codecompass.inputmodemathra.utils.TTSUtility;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MCQFragment extends androidx.fragment.app.Fragment {

    private FragmentMCQBinding binding;
    private RandomValueGenerator random;
    private TTSUtility tts;
    private int correctAnswer;
    private Locale locale;

    public MCQFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMCQBinding.inflate(inflater, container, false);
        random = new RandomValueGenerator();

        String languageCode = LocaleHelper.getLanguage(requireContext());
        locale = new Locale(languageCode);

        Log.d("MCQFragment", "Language code: " + languageCode);
        Log.d("MCQFragment", "Locale set to: " + locale.getDisplayLanguage());

        tts = new TTSUtility(requireActivity(), locale);

        generateNewQuestion();
        return binding.getRoot();
    }

    private void generateNewQuestion() {
        int topic = random.generateQuestionTopic();
        int[] numbers;
        String operator;

        switch (topic) {
            case 1:
                numbers = random.generateSubtractionValues(Difficulty.EASY);
                operator = "-";
                break;
            case 2:
                numbers = random.generateMultiplicationValues(Difficulty.EASY);
                operator = "×";
                break;
            case 3:
                numbers = random.generateDivisionValues(Difficulty.EASY);
                operator = "÷";
                break;
            default:
                numbers = random.generateAdditionValues(Difficulty.EASY);
                operator = "+";
        }

        correctAnswer = numbers[2];

        String num1 = localizeDigits(numbers[0]);
        String num2 = localizeDigits(numbers[1]);
        String questionText = num1 + " " + operator + " " + num2 + " = ?";

        binding.questionTv.setText(questionText);

        String langCode = locale.getLanguage();
        String contentDesc = getString(R.string.question_prefix) + " " + questionText + " " + getString(R.string.option_hint);
        applyAccessibilityLocale(binding.questionTv, contentDesc, langCode);

        tts.speak(questionText);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.questionTv.requestFocus();
        }, 500);

        List<Integer> choices = generateUniqueOptions(correctAnswer);
        updateOption(binding.optionA, choices.get(0), "A");
        updateOption(binding.optionB, choices.get(1), "B");
        updateOption(binding.optionC, choices.get(2), "C");
        updateOption(binding.optionD, choices.get(3), "D");
    }

    private List<Integer> generateUniqueOptions(int correct) {
        Set<Integer> uniqueOptions = new HashSet<>();
        uniqueOptions.add(correct);

        while (uniqueOptions.size() < 4) {
            int wrongOption = correct + random.generateNumberBetween(-5, 5);
            if (wrongOption != correct && wrongOption >= 0) {
                uniqueOptions.add(wrongOption);
            }
        }

        List<Integer> options = new ArrayList<>(uniqueOptions);
        Collections.shuffle(options);
        return options;
    }

    private void updateOption(MaterialButton button, int value, String label) {
        String localizedValue = localizeDigits(value);
        button.setText(localizedValue);

        String langCode = locale.getLanguage();
        String optionText = getString(R.string.option_label, label, localizedValue);
        applyAccessibilityLocale(button, optionText, langCode);

        button.setOnClickListener(v -> showResultDialog(value == correctAnswer));
    }

    private void showResultDialog(boolean isCorrect) {
        String message = isCorrect ? getString(R.string.right_answer) : getString(R.string.wrong_answer);
        int gifResource = isCorrect ? R.drawable.right : R.drawable.wrong;

        tts.speak(message);

        DialogResultBinding dialogBinding = DialogResultBinding.inflate(getLayoutInflater());
        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView);
        dialogBinding.messageTextView.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            tts.speak(getString(R.string.next_question));
            generateNewQuestion();
        }, 2000);
    }

    private String localizeDigits(int input) {
        return NumberFormat.getInstance(locale).format(input);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        tts.shutdown();
    }

    private void applyAccessibilityLocale(View view, String text, String langCode) {
        view.setContentDescription(text);

        view.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                if (info != null) {
                    SpannableString spannable = new SpannableString(text);
                    spannable.setSpan(new LocaleSpan(new Locale(langCode)), 0, text.length(), 0);
                    info.setText(spannable);
                }
            }
        });
    }
}
