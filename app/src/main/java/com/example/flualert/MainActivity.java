package com.example.flualert;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerID;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // <-- must come first

        // Update current week peak dynamically
        updateCurrentWeekPeak();

        Calendar today = Calendar.getInstance();

        // Find the current day of week (1 = Sunday, 2 = Monday, ..., 7 = Saturday)
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);

        // Calculate difference to Monday (Calendar.MONDAY = 2)
        int diffToMonday = (dayOfWeek + 5) % 7; // number of days to subtract to get Monday

        // Start of week (Monday)
        Calendar weekStartCal = (Calendar) today.clone();
        weekStartCal.add(Calendar.DAY_OF_MONTH, -diffToMonday);

        // End of week (Sunday)
        Calendar weekEndCal = (Calendar) weekStartCal.clone();
        weekEndCal.add(Calendar.DAY_OF_MONTH, 6); // Monday + 6 = Sunday

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
        String weekStart = sdf.format(weekStartCal.getTime());
        String weekEnd = sdf.format(weekEndCal.getTime());

        // Set the TextView
        TextView weekRangeText = findViewById(R.id.WeekRangeText);
        weekRangeText.setText(weekStart + " - " + weekEnd);



        spinnerID = findViewById(R.id.monthSelection);

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        // Load weekly forecast data
        Object[][][] sampleData = loadWeeklyForecastData();

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

        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<>(this, R.layout.month_spinner, months);
        monthsAdapter.setDropDownViewResource(R.layout.month_dropdown);
        spinnerID.setAdapter(monthsAdapter);

        // Handle month selection
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
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        // Update peak outbreaks from JSON
        updateAllPeakData();
    }


    /**
     * Update peak data SeekBar & TextView
     */
    private void updatePeakData(int seekBarID, int peakTextViewID, int percentage) {
        SeekBar seekBar = findViewById(seekBarID);
        TextView textView = findViewById(peakTextViewID);

        textView.setText(percentage + "%");
        seekBar.setProgress(percentage);
        seekBar.setOnTouchListener((v, event) -> true); // make non-interactive
    }

    /**
     * Update month-week forecast SeekBar & TextView
     */
    private void updateMonthWeekPercent(int monthSeekBarID, int subtypeID, int monthTextPercentID, String subtype, int monthPercentage) {
        SeekBar seekBar = findViewById(monthSeekBarID);
        TextView subtypeView = findViewById(subtypeID);
        TextView percentView = findViewById(monthTextPercentID);

        subtypeView.setText(subtype);
        percentView.setText(monthPercentage + "%");
        seekBar.setProgress(monthPercentage);
        seekBar.setOnTouchListener((v, event) -> true); // non-interactive
    }

    /**
     * Load weekly forecast data from JSON
     */
    private Object[][][] loadWeeklyForecastData() {
        Object[][][] monthData = new Object[12][4][2]; // 12 months, 4 weeks, 2 items (subtype, percentage)
        try {
            InputStream is = getAssets().open("weekly_forecast.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray weeklyArray = new JSONArray(json);

            String[] subtypes = {"a_h1", "a_h1n1pdm09", "a_h3", "a_not_subtyped",
                    "b_victoria", "b_yamagata", "b_lineage_not_determined"};

            for (int i = 0; i < weeklyArray.length(); i++) {
                JSONObject week = weeklyArray.getJSONObject(i);
                String monthName = week.getString("Month");
                int monthIndex = monthNameToIndex(monthName);
                int weekOfMonth = week.getInt("Week_of_Month") - 1;

                for (int s = 0; s < 4; s++) { // first 4 subtypes
                    String subtype = subtypes[s];
                    int pct = week.getInt(subtype + "_Pct");
                    if (weekOfMonth < 4) {
                        monthData[monthIndex][weekOfMonth][0] = subtype;
                        monthData[monthIndex][weekOfMonth][1] = pct;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return monthData;
    }

    /**
     * Load peak outbreaks from JSON
     */
    private JSONArray loadPeakOutbreaks() {
        try {
            InputStream is = getAssets().open("peak_outbreaks.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new JSONArray(new String(buffer, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    /**
     * Update all peaks using peak_outbreaks.json
     */
    private void updateAllPeakData() {
        // Map JSON subtypes to UI IDs
        HashMap<String, Integer> seekBarMap = new HashMap<>();
        HashMap<String, Integer> textViewMap = new HashMap<>();
        HashMap<String, Integer> weekTextMap = new HashMap<>();

        // SeekBars
        seekBarMap.put("a_not_subtyped", R.id.SeekBar_A);
        seekBarMap.put("a_h1", R.id.SeekBar_A_H1);
        seekBarMap.put("a_h1n1pdm09", R.id.SeekBar_A_H1N1);
        seekBarMap.put("a_h3", R.id.SeekBar_A_H3);
        seekBarMap.put("b_lineage_not_determined", R.id.SeekBar_B);
        seekBarMap.put("b_victoria", R.id.SeekBar_B_Vic);
        seekBarMap.put("b_yamagata", R.id.SeekBar_B_Yam);

        // Percent TextViews
        textViewMap.put("a_not_subtyped", R.id.PeakPercent_A);
        textViewMap.put("a_h1", R.id.PeakPercent_A_H1);
        textViewMap.put("a_h1n1pdm09", R.id.PeakPercent_A_H1N1);
        textViewMap.put("a_h3", R.id.PeakPercent_A_H3);
        textViewMap.put("b_lineage_not_determined", R.id.PeakPercent_B);
        textViewMap.put("b_victoria", R.id.PeakPercent_B_Vic);
        textViewMap.put("b_yamagata", R.id.PeakPercent_B_Yam);

        // Week TextViews (you need to add these IDs in your XML)
        weekTextMap.put("a_not_subtyped", R.id.WeekText_A);
        weekTextMap.put("a_h1", R.id.WeekText_A_H1);
        weekTextMap.put("a_h1n1pdm09", R.id.WeekText_A_H1N1);
        weekTextMap.put("a_h3", R.id.WeekText_A_H3);
        weekTextMap.put("b_lineage_not_determined", R.id.WeekText_B);
        weekTextMap.put("b_victoria", R.id.WeekText_B_Vic);
        weekTextMap.put("b_yamagata", R.id.WeekText_B_Yam);

        JSONArray peaks = loadPeakOutbreaks();

        for (int i = 0; i < peaks.length(); i++) {
            try {
                JSONObject peak = peaks.getJSONObject(i);
                String subtype = peak.getString("Subtype").toLowerCase();
                double probability = peak.getDouble("Probability (%)");
                int percentage = (int) Math.round(probability);

                String month = peak.getString("Month");
                int week = peak.getInt("Week_of_Month");

                String weekLabel = (month.equals("N/A") || week == 0) ? "N/A" : month.substring(0, 3) + " Week " + week;

                Integer seekBarId = seekBarMap.get(subtype);
                Integer textViewId = textViewMap.get(subtype);
                Integer weekTextId = weekTextMap.get(subtype);

                if (seekBarId != null && textViewId != null && weekTextId != null) {
                    // Update SeekBar & percentage
                    updatePeakData(seekBarId, textViewId, percentage);

                    // Update Week label
                    TextView weekText = findViewById(weekTextId);
                    weekText.setText(weekLabel);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * Convert month name to index
     */
    private int monthNameToIndex(String month) {
        switch (month) {
            case "January": return 0;
            case "February": return 1;
            case "March": return 2;
            case "April": return 3;
            case "May": return 4;
            case "June": return 5;
            case "July": return 6;
            case "August": return 7;
            case "September": return 8;
            case "October": return 9;
            case "November": return 10;
            case "December": return 11;
            default: return 0;
        }
    }

    private void updateCurrentWeekPeak() {
        try {
            // Load JSON
            InputStream is = getAssets().open("weekly_forecast.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray weeklyArray = new JSONArray(json);

            // Get current week's Monday and Sunday
            Calendar today = Calendar.getInstance();
            int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
            int diffToMonday = (dayOfWeek + 5) % 7; // days to subtract to get Monday
            Calendar weekStartCal = (Calendar) today.clone();
            weekStartCal.add(Calendar.DAY_OF_MONTH, -diffToMonday); // Monday
            Calendar weekEndCal = (Calendar) weekStartCal.clone();
            weekEndCal.add(Calendar.DAY_OF_MONTH, 6); // Sunday

            // Format week range for display
            SimpleDateFormat sdfDisplay = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
            String weekLabel = sdfDisplay.format(weekStartCal.getTime()) + " - " + sdfDisplay.format(weekEndCal.getTime());

            // Keep track of highest percentage
            double maxPct = -1;
            String maxSubtype = "";

            // Map subtype keys to display names
            HashMap<String, String> subtypeDisplay = new HashMap<>();
            subtypeDisplay.put("a_h1", "A(H1)");
            subtypeDisplay.put("a_h1n1pdm09", "A(H1N1)pdm09");
            subtypeDisplay.put("a_h3", "A(H3)");
            subtypeDisplay.put("a_not_subtyped", "A");
            subtypeDisplay.put("b_victoria", "B(Victoria)");
            subtypeDisplay.put("b_yamagata", "B(Yamagata)");
            subtypeDisplay.put("b_lineage_not_determined", "B");

            // Current week Monday as string for easy comparison
            String mondayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(weekStartCal.getTime());

            JSONObject currentWeekObj = null;

            // Iterate JSON to find the current week
            for (int i = 0; i < weeklyArray.length(); i++) {
                JSONObject weekObj = weeklyArray.getJSONObject(i);
                String jsonDate = weekObj.getString("Date").split("T")[0]; // e.g., "2025-12-08"

                if (jsonDate.equals(mondayStr)) { // exact Monday match
                    currentWeekObj = weekObj;

                    // Compare all subtypes to find max
                    String[] subtypes = {"a_h1", "a_h1n1pdm09", "a_h3", "a_not_subtyped",
                            "b_victoria", "b_yamagata", "b_lineage_not_determined"};

                    for (String subtype : subtypes) {
                        double pct = weekObj.getDouble(subtype + "_Pct");
                        if (pct > maxPct) {
                            maxPct = pct;
                            maxSubtype = subtype;
                        }
                    }
                    break; // found the current week
                }
            }

            // Update main peak TextViews
            TextView percentText = findViewById(R.id.CurrentWeekPercent);
            TextView subtypeText = findViewById(R.id.CurrentWeekSubtype);
            TextView weekRangeText = findViewById(R.id.WeekRangeText);

            if (maxPct >= 0) {
                percentText.setText(Math.round(maxPct) + "%");
                subtypeText.setText("Influenza Type: " + subtypeDisplay.get(maxSubtype));

                // Update remaining subtypes table
                if (currentWeekObj != null) {
                    updateCurrentWeekSubtypesTable(currentWeekObj, maxSubtype);
                }

            } else {
                percentText.setText("N/A");
                subtypeText.setText("Influenza Type: N/A");
            }

            weekRangeText.setText(weekLabel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update current week subtype table (excluding peak)
     */
    private void updateCurrentWeekSubtypesTable(JSONObject weekObj, String highestSubtype) {
        try {
            TableRow subtypeRow = findViewById(R.id.subtypeRow);
            subtypeRow.removeAllViews();

            HashMap<String, String> subtypeDisplay = new HashMap<>();
            subtypeDisplay.put("a_h1", "A(H1)");
            subtypeDisplay.put("a_h1n1pdm09", "A(H1N1)pdm09");
            subtypeDisplay.put("a_h3", "A(H3)");
            subtypeDisplay.put("a_not_subtyped", "A");
            subtypeDisplay.put("b_victoria", "B(Victoria)");
            subtypeDisplay.put("b_yamagata", "B(Yamagata)");
            subtypeDisplay.put("b_lineage_not_determined", "B");

            String[] subtypes = {"a_h1", "a_h1n1pdm09", "a_h3", "a_not_subtyped",
                    "b_victoria", "b_yamagata", "b_lineage_not_determined"};

            for (String subtype : subtypes) {
                if (subtype.equals(highestSubtype)) continue;

                double pct = weekObj.getDouble(subtype + "_Pct");

                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(Gravity.CENTER);
                layout.setPadding(3, 3, 3, 3);

                TextView subtypeText = new TextView(this);
                subtypeText.setText(subtypeDisplay.get(subtype));
                subtypeText.setTextColor(Color.WHITE);
                subtypeText.setGravity(Gravity.CENTER);
                subtypeText.setTextSize(11);
                layout.addView(subtypeText);

                ImageView img = new ImageView(this);
                img.setImageResource(R.drawable.virus_image);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
                img.setLayoutParams(params);
                layout.addView(img);

                TextView percentText = new TextView(this);
                percentText.setText(Math.round(pct) + "%");
                percentText.setTextColor(Color.WHITE);
                percentText.setGravity(Gravity.CENTER);
                layout.addView(percentText);

                subtypeRow.addView(layout);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}