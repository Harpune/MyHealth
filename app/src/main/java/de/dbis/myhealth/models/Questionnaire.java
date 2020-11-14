package de.dbis.myhealth.models;

public class Questionnaire {
    private String id;
    private String description;

    public Questionnaire(int i){
        this.id = String.valueOf(i);
        this.description = "asdasdasd "+ i;
    }
}
