package org.alicebot.ab;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import net.seibertmedia.chatbot.CommandLineInteraction;
import net.seibertmedia.chatbot.UserInteraction;

import org.alicebot.ab.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by User on 5/13/2014.
 */
public class TestAB {
    private static final Logger logger = LoggerFactory.getLogger(TestAB.class);

    public static String sample_file = "sample.random.txt";

  private static UserInteraction userinteraction = new CommandLineInteraction();

    public static void testChat (Bot bot, boolean doWrites, boolean traceMode) {
        Chat chatSession = new Chat(bot, doWrites);
        bot.brain.nodeStats();
        MagicBooleans.trace_mode = traceMode;
        String textLine="";
        while (true) {
            textLine = IOUtils.readInputTextLine("Human");
            if (textLine == null || textLine.length() < 1)  textLine = MagicStrings.null_input;
            if (textLine.equals("q")) System.exit(0);
            else if (textLine.equals("wq")) {
                bot.writeQuit();
                System.exit(0);
            }
            else if (textLine.equals("sc")) sraixCache("c:/ab/data/sraixdata6.txt", chatSession);
            else if (textLine.equals("iqtest")) {
                ChatTest ct = new ChatTest(bot);
                try {
                    ct.testMultisentenceRespond();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            else if (textLine.equals("ab")) testAB(bot, sample_file);
            else {
                String request = textLine;
                if (MagicBooleans.trace_mode) logger.debug("STATE="+request+":THAT="+chatSession.thatHistory.get(0).get(0)+":TOPIC="+chatSession.predicates.get("topic"));
                String response = chatSession.multisentenceRespond(request);
                while (response.contains("&lt;")) response = response.replace("&lt;","<");
                while (response.contains("&gt;")) response = response.replace("&gt;",">");
                IOUtils.writeOutputTextLine("Robot", response);
                //logger.debug("Learn graph:");
                //bot.learnGraph.printgraph();
            }
        }
    }
    public static void testBotChat () {
        Bot bot = new Bot("alice");
        logger.debug(bot.brain.upgradeCnt+" brain upgrades");

        //bot.brain.printgraph();
        Chat chatSession = new Chat(bot);
        String request = "Hello.  How are you?  What is your name?  Tell me about yourself.";
        String response = chatSession.multisentenceRespond(request);
        userinteraction.outputForUserWithNewline("Human: "+request);
        userinteraction.outputForUserWithNewline("Robot: "+response);
    }

    public static void runTests(Bot bot, boolean traceMode) {
        MagicBooleans.qa_test_mode = true;
        Chat chatSession = new Chat(bot, false);
        //        bot.preProcessor.normalizeFile("c:/ab/bots/super/aiml/thats.txt", "c:/ab/bots/super/aiml/normalthats.txt");
        bot.brain.nodeStats();
        MagicBooleans.trace_mode = traceMode;
        IOUtils testInput = new IOUtils(MagicStrings.root_path + "/data/lognormal-500.txt", "read");
        //IOUtils testInput = new IOUtils(MagicStrings.root_path + "/data/callmom-inputs.txt", "read");
        IOUtils testOutput = new IOUtils(MagicStrings.root_path + "/data/lognormal-500-out.txt", "write");
        //IOUtils testOutput = new IOUtils(MagicStrings.root_path + "/data/callmom-outputs.txt", "write");
        String textLine = testInput.readLine();
        int i = 1;
        userinteraction.outputForUser(new Integer(0).toString());
        while (textLine != null) {
            if (textLine == null || textLine.length() < 1)  textLine = MagicStrings.null_input;
            if (textLine.equals("q")) System.exit(0);

            else if (textLine.equals("wq")) {
                bot.writeQuit();
                System.exit(0);
            }
            else if (textLine.equals("ab")) testAB(bot, sample_file);
            else if (textLine.equals(MagicStrings.null_input)) testOutput.writeLine("");
            else if (textLine.startsWith("#")) testOutput.writeLine(textLine);
            else {
                String request = textLine;
                if (MagicBooleans.trace_mode) logger.debug("STATE="+request+":THAT="+chatSession.thatHistory.get(0).get(0)+":TOPIC="+chatSession.predicates.get("topic"));
                String response = chatSession.multisentenceRespond(request);
                while (response.contains("&lt;")) response = response.replace("&lt;","<");
                while (response.contains("&gt;")) response = response.replace("&gt;",">");
                testOutput.writeLine("Robot: " + response);
            }
            textLine = testInput.readLine();

            userinteraction.outputForUser(".");
            if (i % 10 == 0) userinteraction.outputForUser(" ");
            if (i % 100 == 0) { logger.debug(""); userinteraction.outputForUser(i + " "); }
            i++;
        }
        testInput.close();
        testOutput.close();
        userinteraction.outputForUserWithNewline("");
    }
    public static void testAB (Bot bot, String sampleFile) {
        MagicBooleans.trace_mode = true;
        AB ab = new AB(bot, sampleFile);
        ab.ab();
        logger.debug("Begin Pattern Suggestor Terminal Interaction");
        ab.terminalInteraction();
    }

    public static void testShortCuts () {
        //testChat(new Bot("alice"));
        //Graphmaster.enableShortCuts = false;
        //Bot bot = new Bot("alice");
        //bot.brain.printgraph();
        //bot.brain.nodeStats();
        //Graphmaster.enableShortCuts = true;
        //bot = new Bot("alice");
        //bot.brain.printgraph();
        //bot.brain.nodeStats();
    }


    public static void sraixCache (String filename, Chat chatSession) {
        int limit = 650000;
        MagicBooleans.cache_sraix = true;
        try {
            FileInputStream fstream = new FileInputStream(filename);
            // Get the object
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            //Read File Line By Line
            int count = 0;
            while ((strLine = br.readLine()) != null && count++ < limit) {
                userinteraction.outputForUserWithNewline("Human: " + strLine);

                String response = chatSession.multisentenceRespond(strLine);
                userinteraction.outputForUserWithNewline("Robot: " + response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
