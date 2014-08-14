package app;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Entity {

    @JsonProperty("_id")
    private String id;

    public Entity() {
    }

    public Entity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
