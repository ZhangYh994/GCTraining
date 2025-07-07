package com.zyh.Training3;

public class Goods {
    private String name;

    public Goods(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public  void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Goods{" + "name='" + name + '\'' + '}';
    }

}