package org.alicebot.ab;

import redis.clients.jedis.Jedis;

public class StringStorageHandler implements StorageHandler {

    private Jedis jedis;

    private String key;

    public StringStorageHandler(Jedis jedis, String key) {
        this.jedis = jedis;
        this.key = key;
    }

    @Override
    public void write(final String value) {
        jedis.set(key, value);
    }

    @Override
    public String read() {
        return jedis.get(key);
    }

    @Override
    public String getKey() {
        return key;
    }
}
