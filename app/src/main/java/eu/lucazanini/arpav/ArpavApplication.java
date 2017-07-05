package eu.lucazanini.arpav;

import android.app.Application;

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

//        } else {
//            Timber.plant(new CrashReportingTree());
        }
    }

}
