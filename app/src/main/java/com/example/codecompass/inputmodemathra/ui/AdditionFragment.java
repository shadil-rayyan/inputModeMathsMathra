package com.example.codecompass.inputmodemathra.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.codecompass.inputmodemathra.
R;
import com.example.codecompass.inputmodemathra.databinding.DialogResultBinding;
import com.example.codecompass.inputmodemathra.databinding.FragmentAdditionBinding;

import java.util.Random;

public class AdditionFragment extends Fragment {

    private FragmentAdditionBinding binding;
    private int correctAnswer;

    private final String[] questions = {
            "There were %d books on the shelf. The librarian added %d more books. How many books are on the shelf now?",
            "A shop had %d customers in the morning and %d in the afternoon. How many total customers were there?",
            "A bakery sold %d cakes in the morning and %d cakes in the evening. How many cakes were sold in total?",
            "A farmer collected %d apples in the morning and %d in the afternoon. How many apples did he collect?",
            "A classroom had %d students. %d more students joined. How many students are in the classroom now?"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdditionBinding.inflate(inflater, container, false);

        generateNewQuestion();
        binding.submitAnswerBtn.setOnClickListener(v -> checkAnswer());

        return binding.getRoot();
    }

    private void generateNewQuestion() {
        Random random = new Random();

        // Pick a random question template
        String questionTemplate = questions[random.nextInt(questions.length)];

        // Generate two random numbers
        int a = random.nextInt(50) + 1; // 1 - 50
        int b = random.nextInt(50) + 1; // 1 - 50

        correctAnswer = a + b;

        // Format the question
        String question = String.format(questionTemplate, a, b);

        binding.questionTv.setText(question);
        binding.questionTv.setContentDescription(question); // TalkBack description
        binding.answerEt.setText(""); // Clear input field
        binding.answerEt.setHint("Enter the total count");
    }

    private void checkAnswer() {
        try {
            int userAnswer = Integer.parseInt(binding.answerEt.getText().toString());
            showResultDialog(userAnswer == correctAnswer);
        } catch (NumberFormatException e) {
            binding.answerEt.setError("Please enter a valid number");
            binding.answerEt.setContentDescription("Invalid input. Enter only numbers.");
        }
    }

    private void showResultDialog(boolean isCorrect) {
        String message = isCorrect ? "Right Answer!" : "Wrong Answer!";
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
        dialogBinding.messageTextView.setContentDescription("Result: " + message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.show();

        // Dismiss after 3 seconds and generate a new question
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                generateNewQuestion();
            }
        }, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
