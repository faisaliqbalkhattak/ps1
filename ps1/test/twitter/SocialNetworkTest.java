/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Testing strategy (brief):
     *
     * partition guessFollowsGraph by:
     *  - empty tweet list
     *  - tweets with no mentions
     *  - single mention in one tweet
     *  - multiple mentions in one tweet
     *  - multiple tweets by same author union of mentions
     *
     * partition influencers by:
     *  - empty graph
     *  - single author with no followers
     *  - single influencer
     *  - multiple influencers with different follower counts
     *  - ties in follower count (tie-breaker order)
     *
     * The tests below implement one test per partition.
     */

	/* time instances for the tweets which does not affect any of the result here so may be any*/
	
	    private static final Instant t1 = Instant.parse("2016-02-17T10:00:00Z");
	    private static final Instant t2 = Instant.parse("2016-02-17T11:00:00Z");
	    private static final Instant t3 = Instant.parse("2016-02-17T12:00:00Z");	
	
	
//	assertions should be enabled else we lose the test the produce true in every case
    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false;
    }

//    empty list of tweets have empty graph and emty influence
    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());
        assertTrue("expected empty graph", followsGraph.isEmpty());
    }

    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        assertTrue("expected empty list", influencers.isEmpty());
    }

//    guessFollowsGraph tests

    @Test
    public void testGuessFollowsGraphNoMentionsProducesNoEdges() {
        // a tweet with no mentions; spec doesn't require keys for authors,
        // but it does require there to be no inferred follow edges.
//    	tweets created by the helper method defined at the bottom
        List<Tweet> tweets = Arrays.asList(
                newTweet(1L, "Alice", "Hello world", t1),
                newTweet(2L, "Bob", "No mentions here", t2)
        );

        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);

        // ensure there are no follow relationships in the map values
        for (Set<String> followees : graph.values()) {
            assertTrue("expected no followees for any author when there are no mentions",
                    followees == null || followees.isEmpty());
        }
    }

    @Test
    public void testGuessFollowsGraphSingleMention() {
        List<Tweet> tweets = Arrays.asList(
                newTweet(1L, "Ernie", "Hi @Bert, how are you?", t3)
        );

        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);

        // Ernie should be inferred to follow bert (lowercased)
        assertTrue("Ernie should follow bert", graph.containsKey("ernie"));
        assertTrue(graph.get("ernie").contains("bert"));
    }

    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        List<Tweet> tweets = Arrays.asList(
                newTweet(1L, "Alice", "Shoutout to @Bob and @Charlie!", t2)
        );

        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);

        assertTrue(graph.containsKey("alice"));
        Set<String> followees = graph.get("alice");
        assertTrue(followees.contains("bob"));
        assertTrue(followees.contains("charlie"));
        assertEquals("exactly two followees expected", 2, followees.size());
    }

    @Test
    public void testGuessFollowsGraphMultipleTweetsFromSameAuthorUnionMentions() {
        List<Tweet> tweets = Arrays.asList(
                newTweet(1L, "UserA", "@one @two hello", t1),
                newTweet(2L, "UserB", "no mentions", t1),
                newTweet(5L, "UserA", "@two @five another", t1)
        );

        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);

        assertTrue(graph.containsKey("usera"));
        Set<String> followees = graph.get("usera");
        // union of mentions across tweets: one, two, five
        assertTrue(followees.contains("one"));
        assertTrue(followees.contains("two"));
        assertTrue(followees.contains("five"));
//        mentioned four time but one is duplicate
        assertEquals(3, followees.size());
    }

//    influencers tests

    @Test
    public void testInfluencersSingleUserWithoutFollowers() {
        // If the graph contains a single author with no followees, influencers
        // should include that user (as spec: all distinct usernames in followsGraph).
        Map<String, Set<String>> g = new HashMap<>();
        g.put("alice", new HashSet<>()); // alice present but no followees

        List<String> influencers = SocialNetwork.influencers(g);
        assertEquals(1, influencers.size());
        assertEquals("alice", influencers.get(0));
    }

    @Test
    public void testInfluencersSingleInfluencer() {
        // alice follows bob -> bob has 1 follower; alice has 0
        Map<String, Set<String>> g = new HashMap<>();
        g.put("alice", new HashSet<>(Arrays.asList("bob")));
        // ensure a different user with 0 followers is present as a key
        g.put("charlie", new HashSet<>());

        List<String> res = SocialNetwork.influencers(g);
        // expected order: bob (1 follower), then alice and charlie (0 each, alphabetical)
        assertEquals("bob", res.get(0));
        assertTrue(res.contains("alice"));
        assertTrue(res.contains("charlie"));
    }

    @Test
    public void testInfluencersMultipleInfluencersAndOrdering() {
        // create follower map so that alice and bob each have 2 followers
        Map<String, Set<String>> g = new HashMap<>();
        // u1 follows alice and bob
        g.put("u1", new HashSet<>(Arrays.asList("alice", "bob")));
        // u2 follows alice and bob -> now alice and bob have 2 followers each
        g.put("u2", new HashSet<>(Arrays.asList("alice", "bob")));
        // v follows charlie -> charlie has 1 follower
        g.put("v", new HashSet<>(Arrays.asList("charlie")));

        List<String> res = SocialNetwork.influencers(g);

        // alice and bob should be first two (each 2 followers), tie-broken alphabetically:
        assertEquals("alice", res.get(0));
        assertEquals("bob", res.get(1));

        // charlie should come after since it has fewer followers
        assertTrue(res.indexOf("charlie") > 1);
        // keys u1, u2 and v may appear later as they have 0 followers
        assertTrue(res.contains("u1"));
        assertTrue(res.contains("u2"));
        assertTrue(res.contains("v"));
    }

    @Test
    public void testInfluencersTieBreakAlphabetic() {
        // Create two users each with 1 follower; ensure tie broken alphabetically
        Map<String, Set<String>> g = new HashMap<>();
        g.put("a1", new HashSet<>(Arrays.asList("alice")));
        g.put("b1", new HashSet<>(Arrays.asList("bob")));
        // Now alice has 1 follower and bob has 1 follower -> tie
        List<String> res = SocialNetwork.influencers(g);

        // The highest counts are 1 for both alice and bob; they should be first
        int idxAlice = res.indexOf("alice");
        int idxBob = res.indexOf("bob");
        
        // alphabetical: "alice" < "bob" so alice before bob
        assertTrue(idxAlice < idxBob);
    }

    /*helper give me new tweets on the tweet parameters */

    
//      Convenience helper to construct Tweet objects used in tests.
     
    private Tweet newTweet(long id, String author, String text, Instant ts) {
        // The Tweet constructor used in the course is (long id, String author, String text, Date timestamp).
      
        return new Tweet(id, author, text, ts);
    }
}
