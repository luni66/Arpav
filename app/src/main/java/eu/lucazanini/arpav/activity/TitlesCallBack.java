package eu.lucazanini.arpav.activity;

import eu.lucazanini.arpav.model.SlideTitles;

/**
 * Interface for callback methods in activities
 */
public interface TitlesCallBack {

    SlideTitles getSlideTitles();

    String getTitle(int page);

    void setTitles(String[] titles);

    void setTitle(String title, int page);

}
