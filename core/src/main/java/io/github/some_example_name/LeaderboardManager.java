package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardManager {

    // Simple helper class to hold a name and score together
    public static class PlayerScore {
        public String name;
        public int score;
        public PlayerScore(String name, int score) { 
            this.name = name; 
            this.score = score; 
        }
    }

    public static void updateLeaderboard(String newName, int newScore) {
        Preferences prefs = Gdx.app.getPreferences("SortAlgoLeaderboard");
        
        // 1. Get existing scores + the new score
        List<PlayerScore> allScores = new ArrayList<>();
        allScores.add(new PlayerScore(newName, newScore));
        
        for (int i = 0; i < 10; i++) {
            String savedName = prefs.getString("name_" + i, "---");
            int savedScore = prefs.getInteger("score_" + i, 0);
            allScores.add(new PlayerScore(savedName, savedScore));
        }

        // 2. Find max score for dynamic Bucket sizing
        int maxScore = 0;
        for (PlayerScore ps : allScores) {
            if (ps.score > maxScore) maxScore = ps.score;
        }
        if (maxScore == 0) maxScore = 1; // Prevent division by zero

        // 3. Create 10 empty Buckets
        int numBuckets = 10;
        List<List<PlayerScore>> buckets = new ArrayList<>(numBuckets);
        for (int i = 0; i < numBuckets; i++) {
            buckets.add(new ArrayList<>());
        }

        // 4. Distribute scores into Buckets
        for (PlayerScore ps : allScores) {
            int bucketIndex = (ps.score * (numBuckets - 1)) / maxScore; 
            buckets.get(bucketIndex).add(ps);
        }

        // 5. Sort each bucket and merge (Descending Order: Highest to Lowest)
        List<PlayerScore> sortedList = new ArrayList<>();
        for (int i = numBuckets - 1; i >= 0; i--) { 
            Collections.sort(buckets.get(i), (a, b) -> Integer.compare(b.score, a.score));
            sortedList.addAll(buckets.get(i));
        }

        // 6. Save ONLY the Top 10 back to Preferences
        for (int i = 0; i < 10; i++) {
            prefs.putString("name_" + i, sortedList.get(i).name);
            prefs.putInteger("score_" + i, sortedList.get(i).score);
        }
        
        // CRITICAL: flush() tells LibGDX to actually write to the physical file!
        prefs.flush(); 
    }
}