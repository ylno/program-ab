package net.seibertmedia.chatbot;

public interface UserInteraction {

    void outputForUser(String output);

    void outputForUserWithNewline(String output);

    void outputForUserWithNewline(boolean history);

    void outputForUserWithNewline(int count);
}
