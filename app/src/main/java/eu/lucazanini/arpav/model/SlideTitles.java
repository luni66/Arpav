package eu.lucazanini.arpav.model;

import java.util.Observable;

/**
 * SlideTitles of the fragments
 */
public class SlideTitles extends Observable {

    private int pages;
    private String[] titles;

    public SlideTitles(int pages) {
        this.pages = pages;
        titles = new String[pages];
        for (int i = 0; i < pages; i++) {
            titles[i] = Integer.toString(i);
        }
    }

    public SlideTitles(String[] slideTitles) {
        this.pages = slideTitles.length;
        this.titles = new String[pages];
        for (int i = 0; i < pages; i++) {
            if (i < slideTitles.length) {
                this.titles[i] = slideTitles[i];
            } else {
                this.titles[i] = null;
            }
        }
        update();
    }

    public String[] getTitles() {
        return titles;
    }

    public void setTitles(String[] titles) {
        this.pages = titles.length;
        this.titles = titles;
        update();
    }

    public String getSlideTitle(int index) {
        if (index > -1 && index < pages) {
            return titles[index];
        } else {
            return null;
        }
    }

    public void setSlideTitle(String title, int index) {
        if (index > -1 && index < pages) {
            if(!titles[index].equals(title)) {
                titles[index] = title;
                update();
            }
        }
    }

    private void update() {
        setChanged();
        notifyObservers(titles);
    }
}
