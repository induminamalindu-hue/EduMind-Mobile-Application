package com.edumind.app.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DashboardMenuHelper {

    /** Adds a tappable rounded card ("Attendance", "Marks", ...) to the given vertical container. */
    public static void addMenuItem(Context context, LinearLayout container, String title, String subtitle,
                                    int accentColor, Runnable onClick) {
        float density = context.getResources().getDisplayMetrics().density;

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setClickable(true);
        card.setFocusable(true);
        int pad = (int) (16 * density);
        card.setPadding(pad, pad, pad, pad);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(12 * density);
        bg.setStroke((int) (1 * density), Color.parseColor("#E1E4EC"));
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int) (12 * density);
        card.setLayoutParams(cardParams);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(accentColor);
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(titleView);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subtitleView = new TextView(context);
            subtitleView.setText(subtitle);
            subtitleView.setTextColor(Color.parseColor("#88000000"));
            subtitleView.setTextSize(13);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subParams.topMargin = (int) (4 * density);
            subtitleView.setLayoutParams(subParams);
            card.addView(subtitleView);
        }

        card.setOnClickListener(v -> onClick.run());
        container.addView(card);
    }

    /** A small section header ("Manage", "Reports"...) above a group of cards. */
    public static void addSectionHeader(Context context, LinearLayout container, String text) {
        float density = context.getResources().getDisplayMetrics().density;
        TextView header = new TextView(context);
        header.setText(text.toUpperCase());
        header.setTextColor(Color.parseColor("#88000000"));
        header.setTextSize(12);
        header.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) (12 * density);
        params.bottomMargin = (int) (8 * density);
        header.setLayoutParams(params);
        container.addView(header);
    }
}
