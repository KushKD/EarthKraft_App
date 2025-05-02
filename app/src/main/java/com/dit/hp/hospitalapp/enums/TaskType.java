package com.dit.hp.hospitalapp.enums;

public enum TaskType {


    LOGIN(1),
    GET_GENDERS(2),
    GET_TESTS(3),
    GET_REGISTRATION_MODES(4),
    GET_REFERRED_BY(5),
    SAVE_PATIENT(6),

    LAB_REPORTS(7);



    int value;
    private TaskType(int value) { this.value = value; }
}
