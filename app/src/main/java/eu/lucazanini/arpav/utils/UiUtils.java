package eu.lucazanini.arpav.utils;


import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;

public class UiUtils {

    public static class GoneView implements ButterKnife.Action<View>{
        @Override
        public void apply(@NonNull View view, int index) {
            view.setVisibility(View.GONE);
        }
    }

    public static class VisibleView implements ButterKnife.Action<View>{
        @Override
        public void apply(@NonNull View view, int index) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private final static ButterKnife.Action<View> GONE = new GoneView();
    private final static ButterKnife.Action<View> VISIBLE = new VisibleView();

    public static void setViewText(TextView view, String text) {
        if (text != null && text.length() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
            } else {
                view.setText(Html.fromHtml(text));
            }
        } else {
            view.setText("");
        }
    }

    public static void setViewVisibility(TextView view, View image) {
        String caption = view.getText().toString();
        if (caption.equals("")) {
            ButterKnife.apply(image, GONE);
            ButterKnife.apply(view, GONE);
        } else {
            ButterKnife.apply(image, VISIBLE);
            ButterKnife.apply(view, VISIBLE);
        }
    }

    public static void setViewVisibility(TextView view) {
        String caption = view.getText().toString();
        if (caption.equals("")) {
            ButterKnife.apply(view, GONE);
        } else {
            ButterKnife.apply(view, VISIBLE);
        }
    }

}

