package com.github.ahoffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class XmanTest {

    public static final Charset UTF8 = StandardCharsets.UTF_8;

    private static String booklist;

    @BeforeAll
    public static void setup() throws IOException {
        booklist = getStringFromResource("/booklist.xml");
    }

    private static String getStringFromResource(String filename) throws IOException {
        return IOUtils.toString(XmanTest.class.getResourceAsStream(filename), UTF8);
    }

    private static String getStringFromFilename(String filename) throws IOException {
        return IOUtils.toString(Paths.get(filename)
                .toUri()
                .toURL(), UTF8);
    }

    @Test
    public void scratch() throws IOException {
        String xmlText = getStringFromFilename(
                "/Users/aaronhoffer/Library/Preferences/IdeaIC2017.1/scratches/csw query.xml");
        Xman xman = Xman.from(xmlText)
                .evaluate("/GetRecords/@outputSchema")
                .mutateFirstResult(n -> n.setNodeValue("HIP HIP HORRAY"));
        Optional<Node> node = xman.asFirstResult();
        Optional<String> input = xman.asPrettyStringInput();
        String output = xman.asPrettyStringResults();
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
