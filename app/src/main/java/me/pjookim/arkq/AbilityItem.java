package me.pjookim.arkq;

public class AbilityItem {
    private String ability;
    private String value;


    public AbilityItem(String ability, String value){
        this.ability = ability;
        this.value = value;
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
}