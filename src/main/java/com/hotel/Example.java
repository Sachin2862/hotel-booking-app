package com.hotel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Example {
    public static void main(String[] args) {
        Instant instant = Instant.parse ( "2021-05-30T22:06:57.850Z" );
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());

        System.out.println(instant);
    }
}
