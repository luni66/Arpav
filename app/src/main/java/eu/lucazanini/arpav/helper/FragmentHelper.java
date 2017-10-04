package eu.lucazanini.arpav.helper;


import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.widget.TextView;

/**
 * Common code in fragments
 */
public class FragmentHelper {

    private Fragment fragment;

    public FragmentHelper(Fragment fragment) {
        this.fragment = fragment;
    }

    public void setViewText(TextView view, String text) {
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
}
