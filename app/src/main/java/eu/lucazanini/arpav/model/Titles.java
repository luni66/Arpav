package eu.lucazanini.arpav.model;

import java.util.Observable;

/**
 * Titles of the fragments
 */
public class Titles extends Observable {

    public static int PAGES = 7;
    private String[] titles;

    public Titles() {
        titles = new String[PAGES];
        for (int i = 0; i < PAGES; i++) {
            titles[i] = Integer.toString(i);
        }
    }

    public Titles(String[] titles) {
        this.titles = new String[PAGES];
        for (int i = 0; i < PAGES; i++) {
            if (i < titles.length) {
                this.titles[i] = titles[i];
            } else {
                this.titles[i] = null;
            }
        }
        isChanged();
    }

    public String[] getTitles() {
        return titles;
    }

    public void setTitles(String[] titles) {
        this.titles = titles;
        isChanged();
    }

    public String getTitle(int index) {
        if (index > -1 && index < PAGES) {
            return titles[index];
        } else {
            return null;
        }
    }

    public void setTitle(String title, int index) {
        if (index > -1 && index < PAGES) {
            titles[index] = title;
            isChanged();
        }
    }

    private void isChanged() {
        setChanged();
        notifyObservers(titles);
    }

}
