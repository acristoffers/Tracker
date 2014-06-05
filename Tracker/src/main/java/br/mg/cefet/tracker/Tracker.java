package br.mg.cefet.tracker;

public class Tracker {

    private String pack;

    public Tracker(String cod) {
        pack = cod.toUpperCase();
        sync();
    }

    public String getCod() {
        return pack;
    }

    public void sync() {

    }

}
