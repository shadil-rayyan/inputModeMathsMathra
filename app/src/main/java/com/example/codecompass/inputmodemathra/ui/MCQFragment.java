package com.example.codecompass.inputmodemathra.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.example.codecompass.inputmodemathra.R;
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding;
import com.example.codecompass.inputmodemathra.databinding.FragmentMCQBinding;
import com.example.codecompass.inputmodemathra.enums.Difficulty;
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator;
import com.example.codecompass.inputmodemathra.utils.TTSUtility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MCQFragment extends androidx.fragment.app.Fragment {

    private FragmentMCQBinding binding;
    private RandomValueGenerator random;
    private TTSUtility tts;
    private int correctAnswer;

    public MCQFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMCQBinding.inflate(inflater, container, false);
        random = new RandomValueGenerator();
        tts = new TTSUtility(requireActivity());
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
        String questionText = numbers[0] + " " + operator + " " + numbers[1] + " = ?";
        binding.questionTv.setText(questionText);

        // Accessibility description
        binding.questionTv.setContentDescription("Question. " + questionText + ". Double tap to repeat. There are four options below.");

        // Set focus on the question
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.questionTv.requestFocus();
            binding.questionTv.announceForAccessibility(questionText);
        }, 500);

        // Generate unique answer choices
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
        button.setText(String.valueOf(value));
        button.setContentDescription("Option " + label + ". " + value + ". Double tap to select.");

        button.setOnClickListener(v -> showResultDialog(value == correctAnswer));
    }

    private void showResultDialog(boolean isCorrect) {
        String message = isCorrect ? "Right Answer" : "Wrong Answer";
        int gifResource = isCorrect ? R.drawable.right : R.drawable.wrong;
        tts.speak(message);

        DialogResultBinding dialogBinding = DialogResultBinding.inflate(getLayoutInflater());
        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView);
        dialogBinding.messageTextView.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialog.show();

        // Auto-dismiss the dialog after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            tts.speak("Next Question");
            generateNewQuestion();
        }, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        tts.shutdown();
    }
}
