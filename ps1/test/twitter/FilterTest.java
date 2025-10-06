/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.util.Collections;
import org.junit.Test;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     */
    

/**
 * FilterTest JUnit tests for Problem 2: Filter.writtenBy, Filter.inTimespan, Filter.containing
 *
 */
    private static final Instant t1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant t2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant t3 = Instant.parse("2016-02-17T12:00:00Z");

    private static final Tweet tweetA = new Tweet(1, "Alice", "I love space exploration", t1);
    private static final Tweet tweetB = new Tweet(2, "bob", "SpaceX is launching today!", t2);
    private static final Tweet tweetC = new Tweet(3, "alice", "Check out artifact and art.", t3);
    private static final Tweet tweetD = new Tweet(4, "charlie", "Hello, world!", t2);
    

    /* ---------------- Tests for writtenBy ---------------- */
/*
 * Testing strategy
 * ----------------
 *
 * writtenBy:
 * - no tweets match (empty result).
 * - single tweet matches when author equals username (case-insensitive).
 * - multiple tweets from same author mixed with others -> maintain original order.*/
    @Test
    public void testWrittenByNoMatch() {
        List<Tweet> tweets = Arrays.asList(tweetA, tweetB, tweetD);
        List<Tweet> result = Filter.writtenBy(tweets, "nobody");
        assertTrue("expected empty list when no tweets by username", result.isEmpty());
    }

    @Test
    public void testWrittenByCaseInsensitiveAndOrder() {
        List<Tweet> tweets = Arrays.asList(tweetA, tweetB, tweetC, tweetD);
        // both tweetA and tweetC have author "Alice" (different cases)
        List<Tweet> result = Filter.writtenBy(tweets, "alice");
        // Expect tweets in input order: tweetA (id 1), tweetC (id 3)
        assertEquals(Arrays.asList(tweetA, tweetC), result);
    }

    /* ---------------- Tests for inTimespan ---------------- */
    /* inTimespan:
 * - tweets exactly at start and end (inclusive) are included.
 * - tweets strictly inside timespan are included.
 * - tweets outside timespan before start or after end are excluded.
 * - order preserved.*/

    @Test
    public void testInTimespanInclusiveBoundsAndOrder() {
        List<Tweet> tweets = Arrays.asList(tweetA, tweetB, tweetC, tweetD);
        // timespan from t1 (10:00) to t2 (11:00) inclusive should include tweetA (t1) and tweetB (t2) and tweetD (t2)
        Timespan span = new Timespan(t1, t2);
        List<Tweet> result = Filter.inTimespan(tweets, span);
        // result order should be as in input: tweetA then tweetB then tweetD
        assertEquals(Arrays.asList(tweetA, tweetB, tweetD), result);
    }

    @Test
    public void testInTimespanExcludesOutside() {
        List<Tweet> tweets = Arrays.asList(tweetA, tweetB, tweetC);
        // timespan strictly between t1+ and t3- (use t2 only)
        Timespan span = new Timespan(t2, t2);
        List<Tweet> result = Filter.inTimespan(tweets, span);
        assertEquals(Arrays.asList(tweetB), result);
    }

    /* ---------------- Tests for containing ---------------- */
    
    /* *

 * containing:
 * - single word match, simple case.
 * - case-insensitive match (different case).
 * - punctuation adjacent to word (e.g., "hello!" or "(hello)") should still match via word boundaries.
 * - partial word should NOT match (e.g., searching "art" should NOT match "artifact").
 * - multiple query words: any one match returns the tweet.
 * - empty words list -> no tweets returned.
 * - order preserved.*/

    @Test
    public void testContainingSingleWordSimple() {
        List<Tweet> tweets = Arrays.asList(tweetA, tweetB, tweetC);
        List<String> words = Arrays.asList("space");
        // tweetA contains "space" as part of "space exploration" (space as whole word),
        // tweetB contains "SpaceX" which should NOT match "space" as whole word.
        List<Tweet> result = Filter.containing(tweets, words);
        assertEquals(Arrays.asList(tweetA), result);
    }

    @Test
    public void testContainingCaseInsensitiveAndPunctuation() {
        Tweet t = new Tweet(5, "dan", "Hello, SPACE! (We love it).", t1);
        List<Tweet> tweets = Arrays.asList(t, tweetB);
        List<String> words = Arrays.asList("space");
        // t should match due to "SPACE" with punctuation; tweetB has "SpaceX" which should not match.
        List<Tweet> result = Filter.containing(tweets, words);
        assertEquals(Arrays.asList(t), result);
    }

    @Test
    public void testContainingPartialWordNotMatched() {
        List<Tweet> tweets = Arrays.asList(tweetC); // "artifact and art."
        List<String> words = Arrays.asList("art");
        // "artifact" should NOT count; "art." at end should match "art" via word boundary
        List<Tweet> result = Filter.containing(tweets, words);
        // Because tweetC contains both "artifact" (no) and "art." (yes), it should be returned.
        assertEquals(Arrays.asList(tweetC), result);
    }

    @Test
    public void testContainingMultipleQueryWordsAndEmptyWordsList() {
        List<Tweet> tweets = Arrays.asList(tweetA, tweetB, tweetD);
        List<String> words = Arrays.asList("hello", "space");
        // tweetA contains "space", tweetD contains "Hello" -> both included in input order
        List<Tweet> result = Filter.containing(tweets, words);
        assertEquals(Arrays.asList(tweetA, tweetD), result);

        // empty words list -> no tweets returned
        List<Tweet> none = Filter.containing(tweets, Collections.emptyList());
        assertTrue("expected empty list when query words list is empty", none.isEmpty());
    }

   

}
