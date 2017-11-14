package eu.lucazanini.arpav;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import eu.lucazanini.arpav.service.NotificationService2;
import timber.log.Timber;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class NotificationService2Test {

//    @Rule
//    public final ServiceTestRule mServiceRule = ServiceTestRule.withTimeout(60L, TimeUnit.SECONDS);
@Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    // test for a service which is started with startService
//    @Test
    @Test(timeout=1000 * 60)
    public void testWithStartedService() throws TimeoutException {
        Timber.d("testWithStartedService");
//        AcraResources.sendLog("testWithStartedService", null);
        mServiceRule.startService(new Intent(InstrumentationRegistry.getTargetContext(), NotificationService2.class));
        // test code
    }

}
