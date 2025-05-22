package com.example.codecompass.inputmodemathra.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.core.view.ViewCompat; // Import this

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.codecompass.inputmodemathra.R;
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding;
import com.example.codecompass.inputmodemathra.databinding.FragmentMathQuizBinding;
import com.example.codecompass.inputmodemathra.enums.Difficulty;
import com.example.codecompass.inputmodemathra.utils.RandomValueGenerator;

public class MathQuizFragment extends Fragment {

    private FragmentMathQuizBinding binding;
    private RandomValueGenerator random;

    public MathQuizFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMathQuizBinding.inflate(inflater, container, false);
        random = new RandomValueGenerator();
        generateNewQuestion();
        return binding.getRoot();
    }

    private void generateNewQuestion() {
        int numbers[] = random.generateAdditionValues(Difficulty.EASY);
        String questionText = numbers[0] + " + " + numbers[1] + " = ?";

        binding.answerEt.setText("");
        binding.questionTv.setText(questionText);

        // Update content description to match the new question
        String questionDescription = "Math question. " + numbers[0] + " plus " + numbers[1] + " equals what? Double tap to repeat the question.";
        binding.questionTv.setContentDescription(questionDescription);

        // Announce the updated question to TalkBack
        binding.questionTv.post(() -> binding.questionTv.announceForAccessibility(questionDescription));

        binding.submitAnswerBtn.setOnClickListener(v -> {
            boolean isCorrect = Integer.parseInt(binding.answerEt.getText().toString()) == numbers[2];
            showResultDialog(isCorrect);
        });
    }


    private void showResultDialog(boolean isCorrect) {
        String message = isCorrect ? "Right Answer" : "Wrong Answer";
        int gifResource = isCorrect ? R.drawable.right : R.drawable.wrong;

        LayoutInflater inflater = getLayoutInflater();
        DialogResultBinding dialogBinding = DialogResultBinding.inflate(inflater);
        View dialogView = dialogBinding.getRoot();

        // Load the GIF using Glide
        Glide.with(this)
                .asGif()
                .load(gifResource)
                .into(dialogBinding.gifImageView);

        dialogBinding.messageTextView.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false) // Optional - makes sure user doesn't close it manually
                .create();

        dialog.show();

        // Automatically dismiss the dialog after 4 seconds and generate a new question
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                generateNewQuestion();
            }
        }, 4000); // 4000 milliseconds = 4 seconds
    }
}
