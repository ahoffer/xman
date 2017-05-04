package com.github.ahoffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

public class XmanTest {

    public static final Charset UTF8 = StandardCharsets.UTF_8;

    private static String booklist;

    @BeforeAll
    public static void setup() throws IOException {
        booklist = IOUtils.toString(XmanTest.class.getResourceAsStream("/booklist.xml"), UTF8);
    }

    @Test
    public void testInputSource() throws IOException {
        Xman xman = Xman.from(booklist);
        InputSource source = xman.asInputSource();
        String actualString = IOUtils.toString(source.getByteStream(), UTF8);
        assertThat(source, any(InputSource.class));
        assertThat(actualString, equalTo(booklist));
    }
}
