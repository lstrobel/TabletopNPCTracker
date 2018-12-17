package strobel.lukas.namegenerator.improved;

import java.util.Random;

public class NameGenerator {

    private ProbabilityTable pTable;
    private Random random;

    public NameGenerator(String nameFilePath) {
        pTable = new ProbabilityTable(nameFilePath);
        random = new Random();
    }

    public String getName() {
        DoubleStatistics nameStats = pTable.getNameStatistics();
        // Generate a name with a length taken from a gaussian distribution resembling the data
        return getName((int) Math.round(
                random.nextGaussian() * nameStats.getStandardDeviation() + nameStats.getAverage()
        ));
    }

    public String getName(int length) {
        String name = "  ";
        for (int i = 0; i < length - 1; i++) {
            name += pTable.getNextChar(name.substring(name.length() - 2));
        }
        name += pTable.getNextEndChar(name.charAt(name.length() - 1));
        name = name.substring(2, 3).toUpperCase() + name.substring(3);
        // TODO: Write something a little more safe from stack overflows than this
        return name.contains("!") ? "failure" : name; // Redo if error
    }
}
