package com.example.flualert;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerID;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Update current week peak and table
        updateCurrentWeekPeak();

        // Other UI setup (month spinner etc.)...
        setupMonthSpinner();
    }

    private void setupMonthSpinner() {
        spinnerID = findViewById(R.id.monthSelection);
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<>(this, R.layout.month_spinner, months);
        monthsAdapter.setDropDownViewResource(R.layout.month_dropdown);
        spinnerID.setAdapter(monthsAdapter);

        Object[][][] sampleData = loadWeeklyForecastData();

        int[] monthSubtypes = {R.id.subtypeW1, R.id.subtypeW2, R.id.subtypeW3, R.id.subtypeW4};
        int[] monthSeekBars = {R.id.seekBarW1, R.id.seekBarW2, R.id.seekBarW3, R.id.seekBarW4};
        int[] monthPercentageText = {R.id.percentW1, R.id.percentW2, R.id.percentW3, R.id.percentW4};

        spinnerID.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, android.view.View view, int i, long l) {
                Object[][] selectedMonthData = sampleData[i];
                for (int weekIndex = 0; weekIndex < 4; weekIndex++) {
                    Object[] weekData = selectedMonthData[weekIndex];
                    String subtype = (String) weekData[0];
                    int percentage = (int) weekData[1];
                    updateMonthWeekPercent(monthSeekBars[weekIndex], monthSubtypes[weekIndex], monthPercentageText[weekIndex], subtype, percentage);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {}
        });
    }

    private void updateMonthWeekPercent(int monthSeekBarID, int subtypeID, int monthTextPercentID, String subtype, int monthPercentage) {
        SeekBar seekBar = findViewById(monthSeekBarID);
        TextView subtypeView = findViewById(subtypeID);
        TextView percentView = findViewById(monthTextPercentID);

        subtypeView.setText(subtype);
        percentView.setText(monthPercentage + "%");
        seekBar.setProgress(monthPercentage);
        seekBar.setOnTouchListener((v, event) -> true);
    }

    private Object[][][] loadWeeklyForecastData() {
        Object[][][] monthData = new Object[12][4][2];
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

                if (weekOfMonth < 4) {
                    for (int s = 0; s < subtypes.length; s++) {
                        String subtype = subtypes[s];
                        int pct = week.getInt(subtype + "_Pct");
                        monthData[monthIndex][weekOfMonth][0] = subtype;
                        monthData[monthIndex][weekOfMonth][1] = pct;
                    }
                }
            }

        } catch (Exception e) { e.printStackTrace(); }
        return monthData;
    }

    private int monthNameToIndex(String month) {
        switch (month) {
            case "January": return 0; case "February": return 1; case "March": return 2;
            case "April": return 3; case "May": return 4; case "June": return 5;
            case "July": return 6; case "August": return 7; case "September": return 8;
            case "October": return 9; case "November": return 10; case "December": return 11;
            default: return 0;
        }
    }

    private void updateCurrentWeekPeak() {
        try {
            InputStream is = getAssets().open("weekly_forecast.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONArray weeklyArray = new JSONArray(json);

            Calendar today = Calendar.getInstance();
            int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
            int diffToMonday = (dayOfWeek + 5) % 7;
            Calendar weekStartCal = (Calendar) today.clone();
            weekStartCal.add(Calendar.DAY_OF_MONTH, -diffToMonday);
            Calendar weekEndCal = (Calendar) weekStartCal.clone();
            weekEndCal.add(Calendar.DAY_OF_MONTH, 6);

            String mondayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(weekStartCal.getTime());
            String weekLabel = new SimpleDateFormat("MMM dd", Locale.ENGLISH).format(weekStartCal.getTime())
                    + " - " + new SimpleDateFormat("MMM dd", Locale.ENGLISH).format(weekEndCal.getTime());

            double maxPct = -1;
            String maxSubtype = "";
            JSONObject currentWeekObj = null;

            String[] subtypes = {"a_h1", "a_h1n1pdm09", "a_h3", "a_not_subtyped",
                    "b_victoria", "b_yamagata", "b_lineage_not_determined"};

            for (int i = 0; i < weeklyArray.length(); i++) {
                JSONObject weekObj = weeklyArray.getJSONObject(i);
                String jsonDate = weekObj.getString("Date").split("T")[0];

                if (jsonDate.equals(mondayStr)) {
                    currentWeekObj = weekObj;
                    for (String subtype : subtypes) {
                        double pct = weekObj.getDouble(subtype + "_Pct");
                        if (pct > maxPct) {
                            maxPct = pct;
                            maxSubtype = subtype;
                        }
                    }
                    break;
                }
            }

            TextView percentText = findViewById(R.id.CurrentWeekPercent);
            TextView subtypeText = findViewById(R.id.CurrentWeekSubtype);
            TextView weekRangeText = findViewById(R.id.WeekRangeText);

            if (maxPct >= 0) {
                percentText.setText(Math.round(maxPct) + "%");
                HashMap<String, String> subtypeDisplay = new HashMap<>();
                subtypeDisplay.put("a_h1", "A(H1)");
                subtypeDisplay.put("a_h1n1pdm09", "A(H1N1)pdm09");
                subtypeDisplay.put("a_h3", "A(H3)");
                subtypeDisplay.put("a_not_subtyped", "A");
                subtypeDisplay.put("b_victoria", "B(Victoria)");
                subtypeDisplay.put("b_yamagata", "B(Yamagata)");
                subtypeDisplay.put("b_lineage_not_determined", "B");
                subtypeText.setText("Influenza Type: " + subtypeDisplay.get(maxSubtype));

                // Update table for remaining subtypes
                if (currentWeekObj != null) {
                    updateCurrentWeekSubtypesTable(currentWeekObj, maxSubtype);
                }
            } else {
                percentText.setText("N/A");
                subtypeText.setText("Influenza Type: N/A");
            }

            weekRangeText.setText(weekLabel);

        } catch (Exception e) { e.printStackTrace(); }
    }

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

        } catch (Exception e) { e.printStackTrace(); }
    }
}
