package com.racha.rachavoting.util;

import com.racha.rachavoting.annotations.NationalIdValidator;

public class Main {
    public static void main(String[] args) {
        System.out.println(new NationalIdValidator().isValid(
                        "0301135838086", null));
    }
}
