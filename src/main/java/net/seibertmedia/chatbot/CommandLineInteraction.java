package net.seibertmedia.chatbot;

public class CommandLineInteraction implements tempor {

    @Override
    public void outputForUser(final String output) {
        System.out.print(output);
    }

    @Override
    public void outputForUserWithNewline(final String output) {
        System.out.println(output);
    }

    @Override
    public void outputForUserWithNewline(final boolean bool) {
        System.out.println(bool);
    }

    @Override
    public void outputForUserWithNewline(final int count) {
        System.out.println(count);
    }
}
