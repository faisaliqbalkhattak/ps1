/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Filter consists of methods that filter a list of tweets for those matching a
 * condition.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Filter {

	  /**
     * Find tweets written by a particular user.
     *
     * @param tweets   a list of tweets with distinct ids, not modified by this method.
     * @param username Twitter username (assumed valid). Comparison is case-insensitive.
     * @return all and only the tweets whose author is username, in the same order.
     */
    public static List<Tweet> writtenBy(List<Tweet> tweets, String username) {
        if (tweets == null || username == null) {
            throw new IllegalArgumentException("tweets and username must be non-null");
        }
        List<Tweet> result = new ArrayList<>();
        for (Tweet t : tweets) {
            if (t.getAuthor().equalsIgnoreCase(username)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Find tweets that were sent during a particular timespan (inclusive).
     *
     * @param tweets   a list of tweets with distinct ids, not modified by this method.
     * @param timespan timespan
     * @return all and only the tweets that were sent during the timespan,
     *         in the same order as the input list.
     */
    public static List<Tweet> inTimespan(List<Tweet> tweets, Timespan timespan) {
        if (tweets == null || timespan == null) {
            throw new IllegalArgumentException("tweets and timespan must be non-null");
        }
        Instant start = timespan.getStart();
        Instant end = timespan.getEnd();
        List<Tweet> result = new ArrayList<>();
        for (Tweet t : tweets) {
            Instant ts = t.getTimestamp();
            // inclusive: not before start and not after end
            if (!ts.isBefore(start) && !ts.isAfter(end)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Find tweets that contain certain words.
     *
     * @param tweets a list of tweets with distinct ids, not modified by this method.
     * @param words  a list of words to search for in the tweets.
     *               Word comparison is not case-sensitive. Words are matched as whole words only.
     * @return all and only the tweets that include at least one of the words (case-insensitive),
     *         matching whole words; returned in the same order as input.
     */
    public static List<Tweet> containing(List<Tweet> tweets, List<String> words) {
        if (tweets == null || words == null) {
            throw new IllegalArgumentException("tweets and words must be non-null");
        }

        // If words list is empty then no tweets should be returned (no word to match)
        if (words.isEmpty()) {
            return new ArrayList<>();
        }

        // Build a list of compiled regex Patterns, one per word.
        // Use word boundary \b to match whole words and Pattern.CASE_INSENSITIVE.
        List<Pattern> patterns = words.stream()
                .filter(w -> w != null && !w.isEmpty())
                .map(w -> Pattern.compile("\\b" + Pattern.quote(w) + "\\b", Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());

        List<Tweet> result = new ArrayList<>();
        for (Tweet t : tweets) {
            String text = t.getText();
            if (text == null) {
                continue;
            }
            boolean matched = false;
            for (Pattern p : patterns) {
                Matcher m = p.matcher(text);
                if (m.find()) {
                    matched = true;
                    break;
                }
            }
            if (matched) {
                result.add(t);
            }
        }
        return result;
    }

}
