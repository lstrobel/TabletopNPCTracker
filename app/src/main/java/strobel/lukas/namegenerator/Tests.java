package strobel.lukas.namegenerator;

import strobel.lukas.namegenerator.improved.NameGenerator;

public class Tests {

    public static void main(String[] args) {
        NameGenerator n = new NameGenerator("D:\\Projects\\TabletopNPCTracker\\app\\src\\main\\java\\strobel\\lukas\\namegenerator\\customFantasyMaleNames");

        for (int i = 0; i < 100; i++) {
            System.out.println(n.getName());
        }
    }
}
