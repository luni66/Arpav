package eu.lucazanini.arpav.activity;

import eu.lucazanini.arpav.model.SlideTitles;

public interface TitlesCallBack {

    SlideTitles SLIDE_TITLES = new SlideTitles();

    SlideTitles getTitles();

}
