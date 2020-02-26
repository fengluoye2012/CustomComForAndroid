package com.test.customplugin.test

class Person {
    String name
    int age

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}'
    }
}