package eu.lucazanini.arpav;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;
import org.acra.sender.HttpSender;

import eu.lucazanini.arpav.helper.LocaleHelper;
import timber.log.Timber;

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

//        if (BuildConfig.DEBUG) {
//            AcraResources acraResources = new AcraResources();
//
//            try {
//                final ACRAConfiguration config = new ConfigurationBuilder(this)
//                        .setReportingInteractionMode(ReportingInteractionMode.SILENT)
//                        .setFormUri(new String(acraResources.getR1()))
//                        .setFormUriBasicAuthLogin(new String(acraResources.getR2()))
//                        .setFormUriBasicAuthPassword(new String(acraResources.getR3()))
//                        .setReportType(HttpSender.Type.JSON)
//                        .setHttpMethod(HttpSender.Method.PUT)
//                        .build();
//
//                ACRA.init(this, config);
//            } catch (ACRAConfigurationException e) {
//                Timber.e(e.getLocalizedMessage());
//            }
//        }
    }
}
