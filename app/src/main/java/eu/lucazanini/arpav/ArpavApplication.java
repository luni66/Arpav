package eu.lucazanini.arpav;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.data.StringFormat;

import eu.lucazanini.arpav.helper.LocaleHelper;
import timber.log.Timber;

@AcraCore(reportContent = {ReportField.APP_VERSION_CODE,
        ReportField.APP_VERSION_NAME,
        ReportField.ANDROID_VERSION,
        ReportField.PACKAGE_NAME,
        ReportField.REPORT_ID,
        ReportField.BUILD,
        ReportField.STACK_TRACE,
        ReportField.SHARED_PREFERENCES,
        ReportField.CUSTOM_DATA},
        reportFormat = org.acra.data.StringFormat.JSON)
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
//            CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
//            builder.setBuildConfigClass(BuildConfig.class).setReportFormat(JSON);
//            builder.setReportContent(ReportField.APP_VERSION_CODE,
//                    ReportField.APP_VERSION_NAME,
//                    ReportField.ANDROID_VERSION,
//                    ReportField.PACKAGE_NAME,
//                    ReportField.REPORT_ID,
//                    ReportField.BUILD,
//                    ReportField.STACK_TRACE);
//            builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder).setResText(R.string.acra_toast_text);
            ACRA.init(this);
//            ACRA.getErrorReporter().handleException(null);
        }

    }
}
