package com.example.flualert;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // Loop through all widget instances
        for (int widgetId : appWidgetIds) {
            try {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_4x1);

                // Load JSON
                InputStream is = context.getAssets().open("weekly_forecast.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                JSONArray weeklyArray = new JSONArray(new String(buffer, "UTF-8"));

                // Get current week Monday
                Calendar today = Calendar.getInstance();
                int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
                int diffToMonday = (dayOfWeek + 5) % 7; // days to subtract to get Monday
                Calendar weekStart = (Calendar) today.clone();
                weekStart.add(Calendar.DAY_OF_MONTH, -diffToMonday);

                // Monday as string to match JSON date
                SimpleDateFormat sdfJson = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                String mondayStr = sdfJson.format(weekStart.getTime());

                // Calculate week end (Sunday)
                Calendar weekEnd = (Calendar) weekStart.clone();
                weekEnd.add(Calendar.DAY_OF_MONTH, 6);

                // Format week range for display
                SimpleDateFormat sdfDisplay = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
                String weekLabel = sdfDisplay.format(weekStart.getTime()) + " - " + sdfDisplay.format(weekEnd.getTime());

                // Find the highest percentage for current week
                double maxPct = -1;
                String maxSubtype = "";

                for (int i = 0; i < weeklyArray.length(); i++) {
                    JSONObject weekObj = weeklyArray.getJSONObject(i);
                    String jsonDate = weekObj.getString("Date").split("T")[0];

                    if (jsonDate.equals(mondayStr)) {
                        String[] subtypes = {"a_h1","a_h1n1pdm09","a_h3","a_not_subtyped",
                                "b_victoria","b_yamagata","b_lineage_not_determined"};

                        for (String subtype : subtypes) {
                            double pct = weekObj.optDouble(subtype + "_Pct", 0);
                            if (pct > maxPct) {
                                maxPct = pct;
                                maxSubtype = subtype;
                            }
                        }
                        break; // stop after finding current week
                    }
                }

                // Map subtype to display
                String typeDisplay = "Influenza Type";
                switch(maxSubtype) {
                    case "a_h1": typeDisplay="A(H1)"; break;
                    case "a_h1n1pdm09": typeDisplay="A(H1N1)pdm09"; break;
                    case "a_h3": typeDisplay="A(H3)"; break;
                    case "a_not_subtyped": typeDisplay="A"; break;
                    case "b_victoria": typeDisplay="B(Victoria)"; break;
                    case "b_yamagata": typeDisplay="B(Yamagata)"; break;
                    case "b_lineage_not_determined": typeDisplay="B"; break;
                }

                // Update widget views
                views.setTextViewText(R.id.widget_value, Math.round(maxPct) + "%");
                views.setTextViewText(R.id.widget_type, typeDisplay);
                views.setTextViewText(R.id.widget_week, "Week " + getWeekOfMonth(today));
                views.setTextViewText(R.id.widget_date, weekLabel);

                // Push update
                appWidgetManager.updateAppWidget(widgetId, views);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getWeekOfMonth(Calendar cal) {
        return cal.get(Calendar.WEEK_OF_MONTH);
    }
}
