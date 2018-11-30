package me.pjookim.arkq;

import java.util.List;

public class AbilityItem {
    private String ability;
    private String value;
    private List<String> description;


    public AbilityItem(String ability, String value, List<String> description){
        this.ability = ability;
        this.value = value;
        this.description = description;
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }
}