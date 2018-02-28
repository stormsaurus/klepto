/**
 * Copyright 2008 James Teer
 */

package io.james.klepto;

import org.testng.annotations.Test;

@Test( groups={ "functest", "checkintest" })
public class StreamerTest {

    public void generalFunction(){
        Streamer streamer = new Streamer();
        assert(streamer.get("life").length()>1) : "keyword did not return text stream";
        assert(streamer.get("science").length()>1) : "keyword did not return text stream";
        assert(streamer.get(null).length()==0);
    }
}
