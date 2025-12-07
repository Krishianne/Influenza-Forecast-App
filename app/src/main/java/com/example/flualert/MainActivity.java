package com.example.flualert;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerID;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerID = findViewById(R.id.monthSelection);

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        // Sample Data
        Object[][][] sampleData = createMonthsData();

        int[] monthSubtypes = {
                R.id.subtypeW1,
                R.id.subtypeW2,
                R.id.subtypeW3,
                R.id.subtypeW4
        };

        int[] monthSeekBars = {
                R.id.seekBarW1,
                R.id.seekBarW2,
                R.id.seekBarW3,
                R.id.seekBarW4
        };

        int[] monthPercentageText = {
                R.id.percentW1,
                R.id.percentW2,
                R.id.percentW3,
                R.id.percentW4
        };

        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<String>(this, R.layout.month_spinner, months);
        monthsAdapter.setDropDownViewResource(R.layout.month_dropdown);

        spinnerID.setAdapter(monthsAdapter);

        spinnerID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object[][] selectedMonthData = sampleData[i];

                for (int weekIndex = 0; weekIndex < 4; weekIndex++) {
                    Object[] weekData = selectedMonthData[weekIndex];

                    String subtype = (String) weekData[0];
                    int percentage = (int) weekData[1];

                    updateMonthWeekPercent(monthSeekBars[weekIndex], monthSubtypes[weekIndex], monthPercentageText[weekIndex], subtype, percentage);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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

    private void updateMonthWeekPercent(int monthSeekBarID, int subtypeID, int monthTextPercentID, String subtype, int monthPercentage) {
        SeekBar seekBar = findViewById(monthSeekBarID);
        TextView subtypeView = findViewById(subtypeID);
        TextView percentView = findViewById(monthTextPercentID);

        subtypeView.setText(subtype);
        percentView.setText(monthPercentage + "%");
        seekBar.setProgress(monthPercentage);

        // Keep the SeekBar non-interactive
        seekBar.setOnTouchListener((v, event) -> true);
    }

    private Object[][][] createMonthsData(){
        Object[][][] sampleData = new Object[12][4][2];

        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        int perc = 1;

        for (int x = 0; x < 12; x++) {
            String monthPrefix = monthNames[x];
            for (int y = 0; y < 4; y++) {

                String subtype = monthPrefix + (y + 1);

                if (perc > 100) {
                    perc = 1;
                }

                sampleData[x][y][0] = subtype;
                sampleData[x][y][1] = perc;

                perc += 1;
            }
        }
        return sampleData;
    }
}
