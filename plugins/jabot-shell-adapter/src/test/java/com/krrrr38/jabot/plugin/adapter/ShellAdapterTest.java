package com.krrrr38.jabot.plugin.adapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class ShellAdapterTest {
    // catch System.in & System.out
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private PrintStream tmpOutput;
    private ByteArrayInputStream inContent;
    private InputStream tmpInput;
    private Adapter adapter;
    private Deque<String> queue = new ArrayDeque<>();

    @Before
    public void setUp() throws Exception {
        tmpInput = System.in;
        tmpOutput = System.out;
        System.setOut(new PrintStream(outContent));

        adapter = new ShellAdapter();
        adapter.setup("jabot", queue::add, new HashMap<>());
    }

    @After
    public void tearDown() throws Exception {
        System.setIn(tmpInput);
        System.setOut(tmpOutput);
    }

    @Test
    @Ignore("never stop, because of Scanner(System.in).nextLine(). fix it later")
    public void testReceive() throws Exception {
        assertThat(queue.isEmpty(), is(true));

        String message = "message\nexit\n";
        inContent = new ByteArrayInputStream(message.getBytes("UTF-8"));
        System.setIn(inContent);

        adapter.receive();
        assertThat(queue.size(), is(1));
        assertThat(queue.getLast(), is("message"));

        adapter.receive();
        assertThat(queue.size(), is(2));
        assertThat(queue.getLast(), is("exit"));

        // stop correctly
    }

    @Test
    public void testPost() throws Exception {
        adapter.post("Hello");
        assertThat(outContent.toString(), containsString("Hello"));
    }
}