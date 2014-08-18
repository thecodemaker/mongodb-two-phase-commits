package app.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Entity {

    @JsonProperty("_id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
