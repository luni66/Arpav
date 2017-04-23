package eu.lucazanini.arpav.model;

import java.util.Observable;

/**
 * SlideTitles of the fragments
 */
public class SlideTitles extends Observable {

    public static int PAGES = 7;
    private String[] sideTitles;

    public SlideTitles() {
        sideTitles = new String[PAGES];
        for (int i = 0; i < PAGES; i++) {
            sideTitles[i] = Integer.toString(i);
        }
    }

    public SlideTitles(String[] sideTitles) {
        this.sideTitles = new String[PAGES];
        for (int i = 0; i < PAGES; i++) {
            if (i < sideTitles.length) {
                this.sideTitles[i] = sideTitles[i];
            } else {
                this.sideTitles[i] = null;
            }
        }
        update();
    }

    public String[] getSideTitles() {
        return sideTitles;
    }

    public void setSideTitles(String[] sideTitles) {
        this.sideTitles = sideTitles;
        update();
    }

    public String getSlideTitle(int index) {
        if (index > -1 && index < PAGES) {
            return sideTitles[index];
        } else {
            return null;
        }
    }

    public void setSlideTitle(String title, int index) {
        if (index > -1 && index < PAGES) {
            sideTitles[index] = title;
            update();
        }
    }

    private void update() {
        setChanged();
        notifyObservers(sideTitles);
    }

}
