package fr.kissy.doodleflood.model;

/**
 * @author Guillaume Le Biller (<i>guillaume.lebiller@gmail.com</i>)
 */
public class Participant {
    private final String id;
    private final String name;

    public Participant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Participant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
