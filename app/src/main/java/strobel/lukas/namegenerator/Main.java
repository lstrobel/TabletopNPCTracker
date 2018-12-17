package strobel.lukas.namegenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Main {

    public static final String dictionary =
            "D:\\Projects\\TabletopNPCTracker\\app\\src\\main\\java\\strobel\\lukas\\namegenerator\\customFantasyMaleNames";

    public static Random random = new Random();

    public static void main(String[] args) {
        try {

            // Place a zero-entry for every two-letter combination
            Map<String, Integer> freqs = new HashMap<>();
            String letters = "abcdefghijklmnopqrstuvwxyz";
            for (char c : letters.toCharArray()) {
                for (char c2 : letters.toCharArray()) {
                    for (char c3 : letters.toCharArray())
                        freqs.put("" + c + c2 + c3, 0);
                }
            }

            // Parse the dictionary and record the frequency of each two-letter combo. Will
            // ignore non-alphabetical characters. Also record the average length of a word in
            // the file
            BufferedReader r = new BufferedReader(new FileReader(new File(dictionary)));
            String s;
            double wordTotal = 0;
            double wordCumSum = 0;
            while ((s = r.readLine()) != null) {
                s = s.toLowerCase();
                wordTotal++;
                wordCumSum += s.length();
                for (int i = 0; i < s.length() - 2; i++) {
                    if (letters.contains(s.charAt(i) + "") && letters.contains(s.charAt(i + 1) + "") && letters.contains(s.charAt(i + 2) + "")) {
                        String combo = "" + s.charAt(i) + s.charAt(i + 1) + s.charAt(i + 2);
                        freqs.put(combo, freqs.get(combo) + 1);
                    }

                }
            }

            //System.out.println(freqs);

            double avgWordLength = wordCumSum / wordTotal;
            System.out.println("Average word length is " + avgWordLength);

            // Create structure for each letter that will randomly add the following letter
            // based on the odds of that combo
            Map<String, Map<String, Double>> probabilities = new HashMap<>();
            for (char c : letters.toCharArray()) {
                for (char c2 : letters.toCharArray()) {
                    probabilities.put("" + c + c2, new HashMap<>());
                    Map<String, Double> map = probabilities.get("" + c + c2);
                    int totalCount = findTotalCount(c, c2, freqs);
                    for (String key : freqs.keySet()) {
                        if (key.charAt(0) == c && key.charAt(1) == c2) {
                            map.put("" + key.charAt(2),
                                    (0.0 + freqs.get(key)) / totalCount);
                        }
                    }
                }

            }

            //System.out.println(probabilities);

            // Generate some names
            for (int i = 0; i < 100; i++) {

                char firstChar = (char) (random.nextInt(26) + 97);
                char secondChar = (char) (random.nextInt(26) + 97);
                String name = "" + firstChar + secondChar;
                double length =
                        Math.max(0, (random.nextGaussian() * (avgWordLength / 3)) + avgWordLength);
                for (int j = 0; j < length / 3 + 2; j++) {
                    name += getNextSequence(probabilities.get("" + name.charAt(name.length() - 1) + name.charAt(name.length() - 2)));
                }
                System.out.println(name + " " + Math.round(length));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Find the total count of all letter combos starting with firstletter in map
    static int findTotalCount(char firstLetter, char secondLetter, Map<String, Integer> map) {
        int total = 0;
        for (String key : map.keySet()) {
            if (key.charAt(0) == firstLetter && key.charAt(1) == secondLetter) {
                total += map.get(key);
            }
        }
        return total;
    }

    // Will generate the next character randomly, weighted according to the probabilities passed in
    static String getNextSequence(Map<String, Double> probs) {
        double numGen = random.nextDouble();
        Iterator<String> i = probs.keySet().iterator();
        String curr = "";
        while (i.hasNext() && numGen > 0) {
            curr = i.next();
            numGen -= probs.get(curr);
        }
        return curr;
    }
}
