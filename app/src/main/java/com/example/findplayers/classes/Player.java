package com.example.findplayers.classes;

public class Player {
    private String email;
    private String fullname;
    private int age;
    private int phone;
    private String country;

    public Player(String email, String fullname, int age, int phone, String country) {
        this.email = email;
        this.fullname = fullname;
        this.age = age;
        this.phone = phone;
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
