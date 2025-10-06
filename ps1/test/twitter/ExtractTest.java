/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:30:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }


    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */
    
/*
 *
 * getTimespan:
 * - single tweet: start == end.
 * - two tweets where earlier timestamp is first in list.
 * - two tweets where earlier timestamp appears later in the list (unordered input).
 * - multiple tweets with duplicate timestamps (min == some timestamp and max == some timestamp).*/
 
    @Test
    public void testGetTimeSpanSingleTweet() {
    	Tweet t = new Tweet(1 , "ahmad", "hello" , Instant.parse("2016-02-17T10:00:00Z"));
    	Timespan ts = Extract.getTimespan(Arrays.asList(t));
    	assertEquals(t.getTimestamp(),ts.getStart());
    	assertEquals(t.getTimestamp(), ts.getEnd());
    }
    @Test
    public void testGetTimespanSingleTweet() {
        Timespan ts = Extract.getTimespan(Arrays.asList(tweet1));
        assertEquals("start should equal tweet timestamp", d1, ts.getStart());
        assertEquals("end should equal tweet timestamp", d1, ts.getEnd());
    }

    @Test
    public void testGetTimespanTwoTweetsOrdered() {
        Timespan ts = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        assertEquals("expected start", d1, ts.getStart());
        assertEquals("expected end", d2, ts.getEnd());
    }

    @Test
    public void testGetTimespanTwoTweetsUnordered() {
        // earlier timestamp appears second
        Timespan ts = Extract.getTimespan(Arrays.asList(tweet2, tweet1));
        assertEquals("expected start (unordered)", d1, ts.getStart());
        assertEquals("expected end (unordered)", d2, ts.getEnd());
    }

    @Test
    public void testGetTimespanMultipleWithDuplicates() {
        Tweet t3 = new Tweet(3, "charlie", "another", d2); // same as tweet2's time
        Tweet t4 = new Tweet(4, "dan", "later", d3);
        Timespan ts = Extract.getTimespan(Arrays.asList(tweet1, t3, t4));
        assertEquals("min from earliest", d1, ts.getStart());
        assertEquals("max from latest", d3, ts.getEnd());
    }

    
    
/*Testing partitions / edge cases

No mentions.

Single mention at start of text.

Mention with punctuation after (@bob, or @bob.).

Multiple mentions in one tweet and across tweets.

Repeated mentions across tweets (set: unique).

Case differences (@Bob, @bOB) — test case-insensitive behavior (store lower-case).

Email addresses should not be recognized as mentions.

Username with underscore and numbers (@charlie_123).

Mentions adjacent to punctuation and parentheses.	*/
    
    @Test
    public void testGetMentionedUsersVariety() {
    	Tweet t1= new Tweet(1, "x",  "hello @hAris and @yasir_123!", d1);
    	Tweet t2= new Tweet(2, "y",  "email me faisalkhattak@em.com user @faisal", d2);
    	Set<String> mentions = Extract.getMentionedUsers(Arrays.asList(t1, t2));
    	
    	Set<String> expected = new HashSet<>(Arrays.asList("haris", "yasir_123", "faisal"));
    	System.out.println(mentions);
        assertEquals(expected, mentions);
    }
    
    

    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(tweet1));
        assertTrue("expected empty set when no mentions", mentioned.isEmpty());
    }


    @Test
    public void testGetMentionedUsersSimpleMention() {
        Tweet t = new Tweet(10, "alicekhan", "@BobKing how are you?", d1);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t));
        Set<String> expected = new HashSet<>(Arrays.asList("bobking"));
        assertEquals(expected, mentioned);
    }
    

    @Test
    public void testGetMentionedUsersPunctuationAndCase() {
        Tweet t = new Tweet(11, "a", "shoutout to @Carol804, and @carol804 again.", d2);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t));
        Set<String> expected = new HashSet<>(Arrays.asList("carol804"));
        assertEquals(expected, mentioned);
    }
    @Test
    public void testGetMentionedUsersEmailNotCounted() {
        Tweet t = new Tweet(12, "b", "contact: bitdiddle@mit.edu or @MITnews123", d2);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t));
        Set<String> expected = new HashSet<>(Arrays.asList("mitnews123"));
        assertEquals(expected, mentioned);
    }
    

    @Test
    public void testGetMentionedUsersMultipleAndUnderscoreDigits() {
        Tweet t1 = new Tweet(13, "x", "hello @yasir_123 and @daniyal99!", d1);
        Tweet t2 = new Tweet(14, "y", "also @yasir_123 again", d2);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t1, t2));
        Set<String> expected = new HashSet<>(Arrays.asList("yasir_123", "daniyal99"));
        assertEquals(expected, mentioned);
    }

    @Test
    public void testGetMentionedUsersPrecededByUsernameCharNotMatched() {
        // 'a@bob' should not count because character before '@' is a username-valid char
        Tweet t = new Tweet(15, "z", "a@bob is not a mention but @bob is.", d3);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t));
        Set<String> expected = new HashSet<>(Arrays.asList("bob"));
        assertEquals(expected, mentioned);
    }


}
