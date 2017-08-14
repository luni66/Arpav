package eu.lucazanini.arpav.activity;

import eu.lucazanini.arpav.model.SlideTitles;

/**
 * Interface for callback methods in activities
 */
public interface ActivityCallBack {

    SlideTitles getSlideTitles();

    String getTitle(int page);

    void setTitles(String[] titles);

    void setTitle(String title, int page);

    void keepFragments(int page);

}
