package eu.lucazanini.arpav;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import eu.lucazanini.arpav.service.ServiceNotification;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ServiceNotificationTest {

    @Rule
//    public final ServiceTestRule mServiceRule = ServiceTestRule.withTimeout(60L, TimeUnit.SECONDS);
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    // test for a service which is started with startService
//    @Test
    @Test(timeout=1000 * 60)
    public void testWithStartedService() throws TimeoutException {
        mServiceRule.startService(new Intent(InstrumentationRegistry.getTargetContext(), ServiceNotification.class));
        // test code
    }

}
