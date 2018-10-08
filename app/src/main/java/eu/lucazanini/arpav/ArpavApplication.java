package eu.lucazanini.arpav;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;

import eu.lucazanini.arpav.helper.LocaleHelper;
import timber.log.Timber;

@AcraCore(buildConfigClass = BuildConfig.class,
        reportFormat = StringFormat.KEY_VALUE_LIST)
@AcraMailSender(mailTo = "lucazanini66@gmail.com")
public class ArpavApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));

        Resources resources = newBase.getResources();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(newBase);

        String acraKey = resources.getString(R.string.pref_acra);
        boolean isEnabledAcra = sharedPreferences.getBoolean(acraKey, false);

        if (isEnabledAcra) {
//            ACRA.init(this);
//            ACRA.getErrorReporter().handleException(null);
        }

    }
}
