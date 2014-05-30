package com.kolich.http;

import com.kolich.common.functional.either.Either;
import com.kolich.http.helpers.StringClosures;

public class TimeoutTester {

    public static void main(String[] args) {

        final Either<Void,String> result = new StringClosures.StringOrNullClosure(){}
            .get("http://localhost:8080/wait/10");

        if(result.success()) {
            System.out.println("Worked!");
        } else {
            System.out.println("Failed!");
        }

    }

}
