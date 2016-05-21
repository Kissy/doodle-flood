package fr.kissy.doodleflood.model;

import java.util.ArrayList;
import java.util.List;

public class Doodle {

    private String title;
    private String hash;
    private String optionsHash;
    private String optionsAvailable;
    private List<Participant> participants = new ArrayList<>();

    public Doodle(String hash) {
        this.hash = hash;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getOptionsHash() {
        return optionsHash;
    }

    public void setOptionsHash(String optionsHash) {
        this.optionsHash = optionsHash;
    }

    public String getOptionsAvailable() {
        return optionsAvailable;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public void setOptionsAvailable(String optionsAvailable) {
        this.optionsAvailable = optionsAvailable;
    }

    @Override
    public String toString() {
        return "Doodle{" +
                "title='" + title + '\'' +
                ", hash='" + hash + '\'' +
                ", optionsHash='" + optionsHash + '\'' +
                ", optionsAvailable='" + optionsAvailable + '\'' +
                ", participants=" + participants +
                '}';
    }
}
