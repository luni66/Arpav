package eu.lucazanini.arpav;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by luke on 08/12/16.
 */

@RunWith(AndroidJUnit4.class)
public class TownTaskTest {

    @Test
    public void loadTowns(){

        Context appContext = InstrumentationRegistry.getTargetContext();

//        List<Town> towns = Town.loadTowns(appContext);
        List<Town> towns = TownList.getInstance(appContext).loadTowns();

        Collections.sort(towns, new Town.NameComparator());

        assertThat(towns.get(0).getName(), is("Abano Terme"));

    }

}

