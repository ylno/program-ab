package org.alicebot.ab;
/*
 * Program AB Reference AIML 2.0 implementation Copyright (C) 2013 ALICE A.I. Foundation Contact: info@alicebot.org
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA.
 */

import org.alicebot.ab.utils.JapaneseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Manage client predicates
 */
public class Predicates extends HashMap<String, String> {

  private static final Logger logger = LoggerFactory.getLogger(Predicates.class);

  public static final String GENERAL = "general";

  private final Jedis jedis;

  private String botName;

  private final String customerId;

  private boolean logErrors = false;

  public Predicates(final String name, final String customerId) {
    botName = name;
    this.customerId = customerId;
    jedis = new Jedis("localhost");

  }

  public StorageHandler buildStorageHandlerFromKey(String key) {
    StringBuilder stringBuilder = new StringBuilder(botName);
    String[] split = key.split("\\.");
    if (split.length > 0 && split[0].equals(GENERAL)) {
      key = key.replace(GENERAL + ".", "");
      stringBuilder.append(".").append(GENERAL);
    } else {
      stringBuilder.append(".").append(customerId);
    }

    String[] split2 = key.split("\\.");
    if (split2.length > 0 && split2[0].equals("list")) {
      key = key.replace("list" + ".", "");
      stringBuilder.append(".").append(key);
      return new ListStorageHandler(jedis, stringBuilder.toString());
    } else {

      stringBuilder.append(".").append(key);
      return new StringStorageHandler(jedis, stringBuilder.toString());
    }

    // logger.trace("storage main key: {}", stringBuilder.toString());
  }

  /**
   * save a predicate value
   *
   * @param key   predicate name
   * @param value predicate value
   * @return predicate value
   */
  public String put(String key, String value) {
    // MagicBooleans.trace("predicates.put(key: " + key + ", value: " + value + ")");
    if (MagicBooleans.jp_tokenize) {
      if (key.equals("topic")) value = JapaneseUtils.tokenizeSentence(value);
    }
    if (key.equals("topic") && value.length() == 0) value = MagicStrings.default_get;
    if (value.equals(MagicStrings.too_much_recursion)) value = MagicStrings.default_list_item;
    // MagicBooleans.trace("Setting predicate key: " + key + " to value: " + value);
    String result = super.put(key, value);
    StorageHandler storageHandler = buildStorageHandlerFromKey(key);
    logger.debug("storing value: {}, {}", key, value);
    try {
      storageHandler.write(value);
    } catch (JedisConnectionException e) {
      if (logErrors) {
        logger.error("error connecting", e);
      }
    }
    // MagicBooleans.trace("in predicates.put, returning: " + result);
    return result;
  }

  /**
   * get a predicate value
   *
   * @param key predicate name
   * @return predicate value
   */
  public String get(String key) {
    // MagicBooleans.trace("predicates.get(key: " + key + ")");
    String result = super.get(key);
    StorageHandler storageHandler = buildStorageHandlerFromKey(key);
    try {
      result = storageHandler.read();
    } catch (JedisConnectionException e) {
      if (logErrors) {

        logger.error("error connecting to jedis", e);
      }
    }
    logger.trace("read value {}, {}", key, result);
    if (result == null) result = MagicStrings.default_get;

    // MagicBooleans.trace("in predicates.get, returning: " + result);
    return result;
  }

  @Override
  public boolean containsKey(final Object key) {
    StorageHandler storageHandler = buildStorageHandlerFromKey(key.toString());
    try {
      String s = storageHandler.read();
      if (s != null) {
        return true;
      }
    } catch (JedisConnectionException e) {
      if (logErrors) {
        logger.debug("Error", e);
      }
    }
    return super.containsKey(key);
  }

  /**
   * Read predicate default values from an input stream
   *
   * @param in input stream
   */
  public void getPredicateDefaultsFromInputStream(InputStream in) {
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String strLine;
    try {
      // Read File Line By Line
      while ((strLine = br.readLine()) != null) {
        if (strLine.contains(":")) {
          String property = strLine.substring(0, strLine.indexOf(":"));
          String value = strLine.substring(strLine.indexOf(":") + 1);
          put(property, value);
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * read predicate defaults from a file
   *
   * @param filename name of file
   */
  public void getPredicateDefaults(String filename) {
    try {
      // Open the file that is the first
      // command line parameter
      File file = new File(filename);
      if (file.exists()) {
        FileInputStream fstream = new FileInputStream(filename);
        // Get the object
        getPredicateDefaultsFromInputStream(fstream);
        fstream.close();
      }
    } catch (Exception e) {// Catch exception if any
      System.err.println("Error: " + e.getMessage());
    }
  }
}
