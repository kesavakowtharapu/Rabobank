package com.example.Rabobank.controller;

import java.text.DecimalFormat;

public class Test {

    public static void main(String[] args) {

        DecimalFormat format = new DecimalFormat("##.00");
        double a = 10.1;
        double b = -0.3;
        double c= a + b;

        double d = 9.8;

        System.out.println(format.format(c).equals(format.format(d)));



    }
}
