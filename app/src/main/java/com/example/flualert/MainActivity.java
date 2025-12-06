package com.example.flualert;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int[] seekBarIds = {
                R.id.SeekBar_A,
                R.id.SeekBar_A_H1,
                R.id.SeekBar_A_H1N1,
                R.id.SeekBar_A_H3,
                R.id.SeekBar_B,
                R.id.SeekBar_B_Vic,
                R.id.SeekBar_B_Yam
        };

        int[] peakTextViewIds = {
                R.id.PeakPercent_A,
                R.id.PeakPercent_A_H1,
                R.id.PeakPercent_A_H1N1,
                R.id.PeakPercent_A_H3,
                R.id.PeakPercent_B,
                R.id.PeakPercent_B_Vic,
                R.id.PeakPercent_B_Yam
        };

        updatePeakData(seekBarIds[0], peakTextViewIds[0], 10);
        updatePeakData(seekBarIds[1], peakTextViewIds[1], 80);
        updatePeakData(seekBarIds[2], peakTextViewIds[2], 25);
        updatePeakData(seekBarIds[3], peakTextViewIds[3], 70);
        updatePeakData(seekBarIds[4], peakTextViewIds[4], 40);
        updatePeakData(seekBarIds[5], peakTextViewIds[5], 100);
        updatePeakData(seekBarIds[6], peakTextViewIds[6], 30);

    }

    /**
     * Method for updating peak data under the "Peak Outbreak per Subtype" Table
     * @param seekBarID containing the ID of the corresponding seek bar
     * @param peakTextViewID containing the ID of the corresponding text view
     * @param percentage containing the corresponding percentage to output
     */
    private void updatePeakData(int seekBarID, int peakTextViewID, int percentage) {
        SeekBar seekBar = findViewById(seekBarID);
        TextView textView = findViewById(peakTextViewID);

        textView.setText(percentage + "%");
        seekBar.setProgress(percentage);

        seekBar.setOnTouchListener((v, event) -> true);
    }
}