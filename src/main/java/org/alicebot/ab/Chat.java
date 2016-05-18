package org.alicebot.ab;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.alicebot.ab.utils.IOUtils;
import org.alicebot.ab.utils.JapaneseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.seibertmedia.chatbot.CommandLineInteraction;
import net.seibertmedia.chatbot.UserInteraction;

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
/**
 * Class encapsulating a chat session between a bot and a client
 */
public class Chat {
  private static final Logger logger = LoggerFactory.getLogger(Chat.class);

  private UserInteraction userInteraction;

  private Bot bot;
  private boolean doWrites;
  private String customerId = MagicStrings.default_Customer_id;
  private History<History> thatHistory = new History<History>("that");
  private History<String> requestHistory = new History<String>("request");
  private History<String> responseHistory = new History<String>("response");
  // public History<String> repetitionHistory = new History<String>("repetition");
  private History<String> inputHistory = new History<String>("input");

  private Predicates predicates = new Predicates();
  private static String matchTrace = "";
  private static boolean locationKnown = false;
  private static String longitude;
  private static String latitude;
  private TripleStore tripleStore = new TripleStore("anon", this);

  public boolean getLocationKnown() {
    return locationKnown;
  }

  public Bot getBot() {
    return bot;
  }

  public boolean isDoWrites() {
    return doWrites;
  }

  public String getCustomerId() {
    return customerId;
  }

  public History<History> getThatHistory() {
    return thatHistory;
  }

  public History<String> getRequestHistory() {
    return requestHistory;
  }

  public History<String> getResponseHistory() {
    return responseHistory;
  }

  public History<String> getInputHistory() {
    return inputHistory;
  }

  public Predicates getPredicates() {
    return predicates;
  }

  public static String getMatchTrace() {
    return matchTrace;
  }

  public static boolean isLocationKnown() {
    return locationKnown;
  }

  public static String getLongitude() {
    return longitude;
  }

  public static String getLatitude() {
    return latitude;
  }

  public TripleStore getTripleStore() {
    return tripleStore;
  }

  /**
   * Constructor (defualt customer ID)
   *
   * @param bot
   *          the bot to chat with
   */
  public Chat(Bot bot) {
    this(bot, true, "0");
  }

  public Chat(Bot bot, boolean doWrites) {
    this(bot, doWrites, "0");
  }

  /**
   * Constructor
   * 
   * @param bot
   *          bot to chat with
   * @param customerId
   *          unique customer identifier
   */
  public Chat(Bot bot, boolean doWrites, String customerId) {
    userInteraction = new CommandLineInteraction();
    this.customerId = customerId;
    this.bot = bot;
    this.doWrites = doWrites;
    History<String> contextThatHistory = new History<String>();
    contextThatHistory.add(MagicStrings.default_that);
    thatHistory.add(contextThatHistory);
    addPredicates();
    addTriples();
    predicates.put("topic", MagicStrings.default_topic);
    predicates.put("jsenabled", MagicStrings.js_enabled);
    if (MagicBooleans.trace_mode)
      logger.debug("Chat Session Created for bot " + bot.getName());
  }

  /**
   * Load all predicate defaults
   */
  void addPredicates() {
    try {
      predicates.getPredicateDefaults(bot.getConfig_path() + "/predicates.txt");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Load Triple Store knowledge base
   */

  int addTriples() {
    int tripleCnt = 0;
    if (MagicBooleans.trace_mode)
      logger.debug("Loading Triples from " + bot.getConfig_path() + "/triples.txt");
    File f = new File(bot.getConfig_path() + "/triples.txt");
    if (f.exists())
      try {
        InputStream is = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String strLine;
        // Read File Line By Line
        while ((strLine = br.readLine()) != null) {
          String[] triple = strLine.split(":");
          if (triple.length >= 3) {
            String subject = triple[0];
            String predicate = triple[1];
            String object = triple[2];
            tripleStore.addTriple(subject, predicate, object);
            // Log.i(TAG, "Added Triple:" + subject + " " + predicate + " " + object);
            tripleCnt++;
          }
        }
        is.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    if (MagicBooleans.trace_mode)
      logger.debug("Loaded " + tripleCnt + " triples");
    return tripleCnt;
  }

  /**
   * Chat session terminal interaction
   */
  public void chat() {
    BufferedWriter bw = null;
    String logFile = bot.getLog_path() + "/log_" + customerId + ".txt";
    try {
      // Construct the bw object
      bw = new BufferedWriter(new FileWriter(logFile, true));
      String request = "SET PREDICATES";
      String response = multisentenceRespond(request);
      while (true) {
        userInteraction.outputForUser("Human: ");
        request = IOUtils.readInputTextLine();
        response = multisentenceRespond(request);
        userInteraction.outputForUserWithNewline("Robot: " + response);
        bw.write("Human: " + request);
        bw.newLine();
        bw.write("Robot: " + response);
        bw.newLine();
        bw.flush();
      }
      // bw.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Return bot response to a single sentence input given conversation context
   *
   * @param input
   *          client input
   * @param that
   *          bot's last sentence
   * @param topic
   *          current topic
   * @param contextThatHistory
   *          history of "that" values for this request/response interaction
   * @return bot's reply
   */
  String respond(String input, String that, String topic, History contextThatHistory) {
    // MagicBooleans.trace("chat.respond(input: " + input + ", that: " + that + ", topic: " + topic + ", contextThatHistory: " +
    // contextThatHistory + ")");
    boolean repetition = true;
    // inputHistory.printHistory();
    for (int i = 0; i < MagicNumbers.repetition_count; i++) {
      // userinteraction.outputForUserWithNewline(request.toUpperCase()+"=="+inputHistory.get(i)+"?
      // "+request.toUpperCase().equals(inputHistory.get(i)));
      if (inputHistory.get(i) == null || !input.toUpperCase().equals(inputHistory.get(i).toUpperCase()))
        repetition = false;
    }
    if (input.equals(MagicStrings.null_input))
      repetition = false;
    inputHistory.add(input);
    if (repetition) {
      input = MagicStrings.repetition_detected;
    }

    String response;

    response = AIMLProcessor.respond(input, that, topic, this);
    // MagicBooleans.trace("in chat.respond(), response: " + response);
    String normResponse = bot.getPreProcessor().normalize(response);
    // MagicBooleans.trace("in chat.respond(), normResponse: " + normResponse);
    if (MagicBooleans.jp_tokenize)
      normResponse = JapaneseUtils.tokenizeSentence(normResponse);
    String sentences[] = bot.getPreProcessor().sentenceSplit(normResponse);
    for (int i = 0; i < sentences.length; i++) {
      that = sentences[i];
      // userinteraction.outputForUserWithNewline("That "+i+" '"+that+"'");
      if (that.trim().equals(""))
        that = MagicStrings.default_that;
      contextThatHistory.add(that);
    }
    String result = response.trim() + "  ";
    // MagicBooleans.trace("in chat.respond(), returning: " + result);
    return result;
  }

  /**
   * Return bot response given an input and a history of "that" for the current conversational interaction
   *
   * @param input
   *          client input
   * @param contextThatHistory
   *          history of "that" values for this request/response interaction
   * @return bot's reply
   */
  String respond(String input, History<String> contextThatHistory) {
    History hist = thatHistory.get(0);
    String that;
    if (hist == null)
      that = MagicStrings.default_that;
    else
      that = hist.getString(0);
    return respond(input, that, predicates.get("topic"), contextThatHistory);
  }

  /**
   * return a compound response to a multiple-sentence request. "Multiple" means one or more.
   *
   * @param request
   *          client's multiple-sentence input
   * @return
   */
  public String multisentenceRespond(String request) {

    // MagicBooleans.trace("chat.multisentenceRespond(request: " + request + ")");
    String response = "";
    matchTrace = "";
    try {
      String normalized = bot.getPreProcessor().normalize(request);
      normalized = JapaneseUtils.tokenizeSentence(normalized);
      // MagicBooleans.trace("in chat.multisentenceRespond(), normalized: " + normalized);
      String sentences[] = bot.getPreProcessor().sentenceSplit(normalized);
      History<String> contextThatHistory = new History<String>("contextThat");
      for (int i = 0; i < sentences.length; i++) {
        // userinteraction.outputForUserWithNewline("Human: "+sentences[i]);
        AIMLProcessor.trace_count = 0;
        String reply = respond(sentences[i], contextThatHistory);
        response += "  " + reply;
        // userinteraction.outputForUserWithNewline("Robot: "+reply);
      }
      requestHistory.add(request);
      responseHistory.add(response);
      thatHistory.add(contextThatHistory);
      response = response.replaceAll("[\n]+", "\n");
      response = response.trim();
    } catch (Exception ex) {
      ex.printStackTrace();
      return MagicStrings.error_bot_response;
    }

    if (doWrites) {
      bot.writeLearnfIFCategories();
    }
    // MagicBooleans.trace("in chat.multisentenceRespond(), returning: " + response);
    return response;
  }

  public static void setMatchTrace(String newMatchTrace) {
    matchTrace = newMatchTrace;
  }
}
