package strobel.lukas.namegenerator.improved;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ProbabilityTable {

    private Set<String> names;
    private Map<String, Map<Double, Character>> table;
    private Map<Character, Map<Double, Character>> endTable;
    private DoubleStatistics nameStatistics;
    private Random random;


    public ProbabilityTable(String nameFilePath) {
        this(nameFilePath, "^[a-zA-Z]+$");
    }

    public ProbabilityTable(String nameFilePath, String alphabetRegex) {

        // Read in the names file, only using lowercase versions of names that match the regex
        try (BufferedReader br = Files.newBufferedReader(Paths.get(nameFilePath))) {
            names = br.lines()                                       // Get Stream
                    .filter(string -> string.matches(alphabetRegex)) // Match regex
                    .map(String::toLowerCase)                        // Turn to lowercase
                    .collect(Collectors.toSet());                    // Add to set
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize fields and fill nameStatistics
        this.table = new HashMap<>();
        this.endTable = new HashMap<>();
        this.random = new Random();
        this.nameStatistics = names
                .parallelStream()
                .mapToDouble(String::length)
                .collect(
                        DoubleStatistics::new,
                        DoubleStatistics::accept,
                        DoubleStatistics::combine
                );

        // Find the total number of appearances for each possible 3-Char combo in each word
        Map<String, Integer> counts = new HashMap<>();
        Map<String, Integer> endCounts = new HashMap<>();
        for (String name : names) {
            name = "  " + name; // Add spaces to facilitate probability for first chars
            for (int i = 0; i < name.length() - 2; i++) {
                // Sets the value to 1 if no such key exists, otherwise increments the count
                counts.merge(name.substring(i, i + 3), 1, Integer::sum);
            }
            endCounts.merge(name.substring(name.length() - 2), 1, Integer::sum);
        }

        // Use the counts to find the probability of a character appearing after a two-char combo
        // First create structure to hold the sums of each two-char combo to save computation
        Map<String, Integer> sums = counts.entrySet()
                .parallelStream()
                .collect(
                        Collectors.groupingBy(
                                // Group all keys with same first two chars together
                                entry -> entry.getKey().substring(0, 2),
                                Collectors.summingInt(Map.Entry::getValue)
                        )
                );
        // Do the same but for the end chars
        Map<Character, Integer> endSums = endCounts.entrySet()
                .parallelStream()
                .collect(
                        Collectors.groupingBy(
                                // Group all keys with same first char together
                                entry -> entry.getKey().charAt(0),
                                Collectors.summingInt(Map.Entry::getValue)
                        )
                );

        // Fill table
        for (String key : counts.keySet()) {
            String newKey = key.substring(0, 2);
            double probability = ((double) counts.get(key)) / sums.get(newKey);
            if (!table.containsKey(newKey)) {
                table.put(newKey, new TreeMap<>());
                table.get(newKey).put(probability, key.charAt(2));
            } else {
                Map<Double, Character> subMap = table.get(newKey);
                probability = probability + Collections.max(subMap.keySet());

                // Round off last value to avoid floating point errors
                if (Math.abs(1.0 - probability) < 0.00000002) {
                    probability = 1.0;
                }
                subMap.put(probability, key.charAt(2));
            }
        }
        // Fill end  table
        for (String key : endCounts.keySet()) {
            Character newKey = key.charAt(0);
            double probability = ((double) endCounts.get(key)) / endSums.get(newKey);
            if (!endTable.containsKey(newKey)) {
                endTable.put(newKey, new TreeMap<>());
                endTable.get(newKey).put(probability, key.charAt(1));
            } else {
                Map<Double, Character> subMap = endTable.get(newKey);
                probability = probability + Collections.max(subMap.keySet());

                // Round off last value to avoid floating point errors
                if (Math.abs(1.0 - probability) < 0.00000002) {
                    probability = 1.0;
                }
                subMap.put(probability, key.charAt(1));
            }
        }
    }

    /*
      Returns a weighted, randomly generated next character based on the input two characters.
      Input String must be of length two otherwise will throw an IllegalArgumentException.
     */
    public char getNextChar(String firstTwoChars) {
        if (firstTwoChars.length() != 2) {
            throw new IllegalArgumentException("Input string must be of length 2");
        }
        if (table.containsKey(firstTwoChars)) {
            Map<Double, Character> subMap = table.get(firstTwoChars);
            return getNext(subMap);
        }
        return '!';
    }

    /*
      Returns a weighted, randomly generated next character based on the input character.
     */
    public char getNextEndChar(Character firstChar) {
        if (endTable.containsKey(firstChar)) {
            Map<Double, Character> subMap = endTable.get(firstChar);
            return getNext(subMap);
        }
        return '!';
    }

    private char getNext(Map<Double, Character> probabilityMap) {
        double choice = random.nextDouble();
        for (double key : probabilityMap.keySet()) {
            if (key >= choice) {
                return probabilityMap.get(key);
            }
        }
        return '!';
    }

    public DoubleStatistics getNameStatistics() {
        return nameStatistics;
    }
}
