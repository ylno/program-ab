package org.alicebot.ab;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

@Ignore
public class RedisTest {
    private static final Logger logger = LoggerFactory.getLogger(RedisTest.class);
    @Test
    public void testRedis() throws Exception {
        Jedis jedis = new Jedis("localhost");
        jedis.set("foo", "1");
        String value = jedis.get("foo");
        logger.debug("redis-anwer: " + value);
        jedis.incr("foo");
        String value2 = jedis.get("foo");
        logger.debug("redis-anwer: " + value2);
    }
}
