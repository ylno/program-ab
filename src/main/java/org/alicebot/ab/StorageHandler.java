package org.alicebot.ab;

public interface StorageHandler {
    void write(String value);

    String read();

    String getKey();
}
