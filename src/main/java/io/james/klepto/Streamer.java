/**
 * Copyright 2008 James Teer
 */

package io.james.klepto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class Streamer {

    private Timer _timer = null;
    private boolean _active = false;
    private boolean _current = false;

    private SyndFeedInput _input = new SyndFeedInput();

    private URL _trendsURL = null;
    private URL _scienceNewsURL = null;
    private Matcher _trendsMatcher = Pattern.compile(">([^><]*)</a>").matcher("");

    Map<String,String> _streamCache = new HashMap<String,String>(2);

    public Streamer(){
        System.setProperty("http.agent", "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3");
        _streamCache.put("science", "");
        _streamCache.put("life", "");
        try {
            _trendsURL = new URL("http://www.google.com/trends/hottrends/atom/hourly");
            //_scienceNewsURL = new URL("http://news.google.com/news?ned=us&topic=t&output=rss");
            _scienceNewsURL = new URL("http://rss.news.yahoo.com/rss/science");

            //*** setup one hour timer
            _timer = new Timer();
            _timer.scheduleAtFixedRate(new TimerTask(){
                public void run(){
                    _current=false;
                }
            }, 0, 1000*60*60);
            _active = true;
        } catch (MalformedURLException e) {
            System.out.println(getClass().getSimpleName()+" could not parse feed URL.");
            System.out.println(e);
        }
    }

    public String get(String name){
        if(_active && !_current) update();
        return StringUtils.defaultString(_streamCache.get(name));
    }

    private void update(){
        String parsedText="";
        //*** parse Trends Feed
        try {
            SyndFeed feed = _input.build(new XmlReader(_trendsURL));
            SyndEntry entry = (SyndEntry)feed.getEntries().get(0);
            SyndContent content = (SyndContent)entry.getContents().get(0);
            _trendsMatcher.reset(content.getValue());
            while( _trendsMatcher.find() ){
                parsedText += _trendsMatcher.group(1)+"<br />";
            }
            _streamCache.put("life", parsedText);
        } catch (IllegalArgumentException e) {
            System.out.println(getClass().getSimpleName()+" error while updating text stream.");
            System.out.println(e);
        } catch (FeedException e) {
            System.out.println(getClass().getSimpleName()+" error while updating text stream.");
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(getClass().getSimpleName()+" error while updating text stream.");
            System.out.println(e);
        }

        //*** parse News Feed
        parsedText="";
        try {
            SyndFeed feed = _input.build(new XmlReader(_scienceNewsURL));
            List<SyndEntry> entries = feed.getEntries();
            for (SyndEntry entry : entries)
                parsedText += entry.getDescription().getValue();
            _streamCache.put("science", parsedText);
        } catch (IllegalArgumentException e) {
            System.out.println(getClass().getSimpleName()+" error while updating text stream.");
            System.out.println(e);
        } catch (FeedException e) {
            System.out.println(getClass().getSimpleName()+" error while updating text stream.");
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(getClass().getSimpleName()+" error while updating text stream.");
            System.out.println(e);
        }
        assert !_streamCache.containsKey(null) : "null key found in the streamer cache";
    }

}
