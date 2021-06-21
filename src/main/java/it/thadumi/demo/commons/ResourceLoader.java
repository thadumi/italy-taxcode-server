package it.thadumi.demo.commons;

import io.vavr.collection.CharSeq;
import io.vavr.collection.Seq;
import io.vavr.control.Try;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ResourceLoader {

     public static Try<Seq<CharSeq>> getResourceFileAsSeq(String fileName) {
        return getResourceFile(fileName)
                  .map(cs -> cs.split("\n"));
    }

    public static Try<CharSeq> getResourceFile(String fileName) {
        return Try.of(() -> loadFile(fileName))
                .map(CharSeq::of);
    }

    private static String loadFile(String file) throws IOException {
        var classLoader = ResourceLoader.class.getClassLoader();

        try (InputStream is = classLoader.getResourceAsStream(file)) {
            if (is == null) return null;
            try (var isr = new InputStreamReader(is);
                 var reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
