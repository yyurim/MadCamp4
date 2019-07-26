package com.madcamp.petclub.MyPets.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class  Pet {

    private String uid;
    private String petName;
    private String petAge;
    private String petGender;
    private String petSpecies;
    private String petFirstDate;
    private String petNeutralization;
    private String petBff;

    public Pet() {
        // Default constructor required for calls to DataSnapshot.getValue(Pet.class)
    }

    public Pet(String uid, String petName,String petAge, String petGender, String petSpecies,
               String petFirstDate, String petNeutralization, String petBff) {
        this.uid = uid;
        this.petName = petName;
        this.petAge = petAge;
        this.petGender = petGender;
        this.petSpecies = petSpecies;
        this.petFirstDate = petFirstDate;
        this.petNeutralization = petNeutralization;
        this.petBff = petBff;
    }

    public String getUid() {return uid;}
    public String getPetName() {return petName;}
    public String getPetGender() {return petGender;}
    public String getPetSpecies() {return petSpecies;}
    public String getPetAge() {return petAge;}
    public String getPetFirstDate() {return petFirstDate;}
    public String getPetNeutralization() {return petNeutralization;}
    public String getPetBff() {return petBff;}

    public void setUid(String uid) {this.uid = uid;}
    public void setpetName(String petName) {this.petName = petName;}
    public void setpetGender(String petGender) {this.petGender = petGender;}
    public void setpetSpecies(String petSpecies) {this.petSpecies = petSpecies;}
    public void setpetAge(String petAge) {this.petAge = petAge;}
    public void setPetFirstDate(String petFirstDate) {this.petFirstDate = petFirstDate;}
    public void setPetNeutralization(String petNeutralization) {this.petNeutralization = petNeutralization;}
    public void setPetBff(String petBff) {this.petBff = petBff;}

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("petName", petName);
        result.put("petAge", petAge);
        result.put("petGender", petGender);
        result.put("petSpecies", petSpecies);
        result.put("petFirstDate", petFirstDate);
        result.put("petBff", petBff);
        result.put("petNeutralization", petNeutralization);
        return result;
    }
    // [END post_to_map]

}