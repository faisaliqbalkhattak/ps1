/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SocialNetwork provides methods that operate on a social network.
 * 
 * A social network is represented by a Map<String, Set<String>> where map[A] is
 * the set of people that person A follows on Twitter, and all people are
 * represented by their Twitter usernames. Users can't follow themselves. If A
 * doesn't follow anybody, then map[A] may be the empty set, or A may not even exist
 * as a key in the map; this is true even if A is followed by other people in the network.
 * Twitter usernames are not case sensitive, so "ernie" is the same as "ERNie".
 * A username should appear at most once as a key in the map or in any given
 * map[A] set.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class SocialNetwork {

    /**
     * Guess who might follow whom, from evidence found in tweets.
     * 
     * @param tweets
     *            a list of tweets providing the evidence, not modified by this
     *            method.
     * @return a social network (as defined above) in which Ernie follows Bert
     *         if and only if there is evidence for it in the given list of
     *         tweets.
     *         One kind of evidence that Ernie follows Bert is if Ernie
     *         @-mentions Bert in a tweet. This must be implemented. Other kinds
     *         of evidence may be used at the implementor's discretion.
     *         All the Twitter usernames in the returned social network must be
     *         either authors or @-mentions in the list of tweets.
     */
    public static Map<String, Set<String>> guessFollowsGraph(List<Tweet> tweets) {

//    	creates adjacentcy lsit for the auther and its following
    	 Map<String, Set<String>> followsGraph = new HashMap<>();

         if (tweets == null) {
             return followsGraph; // empty
         }

/*
 * check all the tweets and get the author create an entry fo this author if not exists and then check the mentioned users if he mentioned
 * him self ignore other wise add all this to his adjacency in a set data structure that will automatically ignore the duplicates*/
         for (Tweet t : tweets) {
             if (t == null) continue;

             // normalize author
             String author = t.getAuthor();
             if (author == null) continue;
             author = author.toLowerCase();

             // ensure author exists as key
             followsGraph.computeIfAbsent(author, k -> new HashSet<>());

             // get the mentioned users for this single tweet
             Set<String> mentioned = Extract.getMentionedUsers(Collections.singletonList(t));

             // add each mentioned user (normalized) to author's follow-set, excluding self
             for (String m : mentioned) {
                 if (m == null) continue;
                 String mentionedUser = m.toLowerCase();
                 if (!mentionedUser.equals(author)) {
                     followsGraph.get(author).add(mentionedUser);
                 }
             }
         }

         return followsGraph;
    }

    /**
     * Find the people in a social network who have the greatest influence, in
     * the sense that they have the most followers.
     * 
     * @param followsGraph
     *            a social network (as defined above)
     * @return a list of all distinct Twitter usernames in followsGraph, in
     *         descending order of follower count.
     */
    public static List<String> influencers(Map<String, Set<String>> followsGraph) {
//    	return empty list is the graph is empty 
    	  if (followsGraph == null) return new ArrayList<>();
    	    // Count followers: user AND TEH NUMBER of followers for intermediate computations
    	    Map<String, Integer> followerCount = new HashMap<>();

    	    // Make sure every user that appears as a key exists in followerCount it may have 0 followers
    	    /*
    	     * the method 
    	     * keySet() give the first entry of all the key value pairs in the graph that in our case we made them the authors
    	     * and the 
    	     * entrySet give all the entries as separate key list pairs
    	     * 
    	     * the program simple gets all the authors make their followers to zero and then for each of the followee of the author increment the 
    	     * count of followers( if this influencer is not in the followerCount map<string,integer> then create it) of the mentioned user it can be another author.*/
    	    for (String author : followsGraph.keySet()) {
    	        followerCount.putIfAbsent(author, 0);
    	    }
    	    // For each author, each followee gets +1 follower
    	    for (Map.Entry<String, Set<String>> e : followsGraph.entrySet()) {
    	        Set<String> followees = e.getValue();
    	        if (followees == null) continue;
    	        for (String followee : followees) {
    	            if (followee == null) continue;
    	            followerCount.put(followee, followerCount.getOrDefault(followee, 0) + 1);
    	        }
    	    }

//    	    at this step we have  list of user and no-of-followers pair but users are not sorted on the followers count and even then they may have same 
//    	    number of followers but  i will sort the users with same following on the alphabetic order of their username
    	    List<String> users = new ArrayList<>(followerCount.keySet());

//    	    comparator function
    	    users.sort((u1, u2) -> {
    	        int c1 = followerCount.getOrDefault(u1, 0);
    	        int c2 = followerCount.getOrDefault(u2, 0);
    	        if (c1 != c2) return Integer.compare(c2, c1); // numeric descending (if not equal)
    	        return u1.compareTo(u2);                     // alphabetic ascending (else)
    	    });

    	    return users;
    }

}
