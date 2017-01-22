package eu.lucazanini.arpav.preference;

import android.content.Context;

import eu.lucazanini.arpav.model.Previsione;

/**
 * Created by luke on 31/12/16.
 */

public class MockPreferences implements Preferences {

    private Context context;

    public MockPreferences(Context context){
        this.context = context;
    }

    @Override
    public Previsione.Language getDefaultLanguage() {
        return Previsione.Language.IT;
    }
}
