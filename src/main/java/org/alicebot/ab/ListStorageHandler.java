package org.alicebot.ab;

import redis.clients.jedis.Jedis;

public class ListStorageHandler implements StorageHandler {

    private Jedis jedis;

    private String key;

    public ListStorageHandler(Jedis jedis, String key) {
        this.jedis = jedis;
        this.key = key;
    }

    @Override
    public void write(final String value) {
        jedis.lpush(key, value);
    }

    @Override
    public String read() {
        // not implemenred
        //return jedis.lpop(key);
        return "";
    }

    @Override
    public String getKey() {
        return key;
    }
}
