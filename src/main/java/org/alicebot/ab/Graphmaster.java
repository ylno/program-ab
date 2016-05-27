package org.alicebot.ab;
/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AIML Pattern matching algorithm and data structure.
 *
 *
 */
public class Graphmaster {
    private static final Logger logger = LoggerFactory.getLogger(Graphmaster.class);

	static private boolean DEBUG = false;

  private Bot bot;

  private String name;

  private final Nodemapper root;

  private int matchCount = 0;

  private int upgradeCnt = 0;

  private HashSet<String> vocabulary;

  private String resultNote = "";

  private int categoryCnt = 0;

  public static boolean isEnableShortCuts() {
    return enableShortCuts;
  }

  private static boolean enableShortCuts = false;

  public Nodemapper getRoot() {
    return root;
  }

    /**
     * Constructor
     *
     * @param bot  the bot the graph belongs to.
     */
    public Graphmaster (Bot bot) {
        this(bot, "brain");
    }

    public Graphmaster (Bot bot, String name) {
        root = new Nodemapper();
        this.bot = bot;
        this.name = name;
        vocabulary = new HashSet<String>();
    }


    /**
     * Convert input, that and topic to a single sentence having the form
     * {@code input <THAT> that <TOPIC> topic}
     *
     * @param input  input (or input pattern)
     * @param that   that (or that pattern)
     * @param topic  topic (or topic pattern)
     * @return
     */
    public static String inputThatTopic (String input, String that, String topic)  {
        return input.trim()+" <THAT> "+that.trim()+" <TOPIC> "+topic.trim();
    }

    /**
     * add an AIML category to this graph.
     *
     * @param category            AIML Category
     */
    String botPropRegex = "<bot name=\"(.*?)\"/>";
    Pattern botPropPattern = Pattern.compile(botPropRegex, Pattern.CASE_INSENSITIVE);

    public String replaceBotProperties(String pattern) {
        if (pattern.contains("<B")) {
            Matcher matcher = botPropPattern.matcher(pattern);
            while (matcher.find()) {
                String propname = matcher.group(1).toLowerCase();
                //logger.debug(matcher.group(1));
        String property = bot.getProperties().get(propname).toUpperCase();
                pattern = pattern.replaceFirst("(?i)"+botPropRegex, property);
                //logger.debug("addCategory: Replaced pattern with: "+inputThatTopic);
            }

        }
        return pattern;
    }
    public void addCategory (Category category) {
        String inputThatTopic = inputThatTopic(category.getPattern(), category.getThat(), category.getTopic());
        //logger.debug("addCategory: "+inputThatTopic);
        inputThatTopic = replaceBotProperties(inputThatTopic);
        /*if (inputThatTopic.contains("<B")) {
        Matcher matcher = botPropPattern.matcher(inputThatTopic);
        while (matcher.find()) {
            String propname = matcher.group(1).toLowerCase();
            //logger.debug(matcher.group(1));
            String property = bot.properties.get(propname).toUpperCase();
            inputThatTopic = inputThatTopic.replaceFirst("(?i)"+botPropRegex, property);
            //logger.debug("addCategory: Replaced pattern with: "+inputThatTopic);
        }
        }*/
        //
        Path p = Path.sentenceToPath(inputThatTopic);
        addPath(p, category);
        categoryCnt++;
    }

    boolean thatStarTopicStar(Path path) {
        String tail = Path.pathToSentence(path).trim();
        //logger.debug("thatStarTopicStar "+tail+" "+tail.equals("<THAT> * <TOPIC> *"));
        return tail.equals("<THAT> * <TOPIC> *");
    }
    void addSets (String type, Bot bot, Nodemapper node, String filename) {
        //logger.debug("adding Set "+type+" from "+bot.setMap);
        String setName = Utilities.tagTrim(type, "SET").toLowerCase();
        //AIMLSet aimlSet;
    if (bot.getSetMap().containsKey(setName)) {
            if (node.sets == null) node.sets = new ArrayList<String>();
            if (!node.sets.contains(setName)) node.sets.add(setName);
            //logger.debug("sets = "+node.sets);
        }
        else {
      logger.debug("No AIML Set found for <set>" + setName + "</set> in " + bot.getName() + " " + filename);
        }
    }
    /**
     * add a path to the graph from the root to a Category
     *
     * @param path            Pattern path
     * @param category        AIML category
     */
    void addPath(Path path, Category category) {
        addPath(root, path, category);

    }

    /**
     * add a Path to the graph from a given node.
     * Shortcuts: Replace all instances of paths "<THAT> * <TOPIC> *" with a direct link to the matching category
     *
     * @param node     starting node in graph
     * @param path     Pattern path to be added
     * @param category    AIML Category
     */
    void addPath(Nodemapper node, Path path, Category category) {
        //if (path != null) logger.debug("Enable shortcuts = "+enableShortCuts+" path="+Path.pathToSentence(path)+" "+thatStarTopicStar(path));
        if (path == null) {
            node.category = category;
            node.height = 0;
        }
        else if (enableShortCuts && thatStarTopicStar(path)) {
            node.category = category;
            node.height = Math.min(4, node.height);
            node.shortCut = true;
        }
    else if (NodemapperOperator.containsKey(node, path.getWord())) {
      if (path.getWord().startsWith("<SET>"))
        addSets(path.getWord(), bot, node, category.getFilename());
      Nodemapper nextNode = NodemapperOperator.get(node, path.getWord());
      addPath(nextNode, path.getNext(), category);
            int offset = 1;
      if (path.getWord().equals("#") || path.getWord().equals("^"))
        offset = 0;
            node.height = Math.min(offset + nextNode.height, node.height);
        }
        else {
            Nodemapper nextNode = new Nodemapper();
      if (path.getWord().startsWith("<SET>")) {
        addSets(path.getWord(), bot, node, category.getFilename());
            }
            if (node.key != null)  {
                NodemapperOperator.upgrade(node);
                upgradeCnt++;
            }
      NodemapperOperator.put(node, path.getWord(), nextNode);
      addPath(nextNode, path.getNext(), category);
            int offset = 1;
      if (path.getWord().equals("#") || path.getWord().equals("^"))
        offset = 0;
            node.height = Math.min(offset + nextNode.height, node.height);
        }
    }
    /**
     *   test if category is already in graph
     *
     *   @return true or false
     */
    public boolean existsCategory(Category c) {
       return (findNode(c) != null);
    }
    /**
     *   test if category is already in graph
     *
     *   @return true or false
     */
    public Nodemapper findNode(Category c) {
        return findNode(c.getPattern(), c.getThat(), c.getTopic());
    }

    /** Given an input pattern, that pattern and topic pattern, find the leaf node associated with this path.
     *
     * @param input    input pattern
     * @param that     that pattern
     * @param topic    topic pattern
     * @return         leaf node or null if no matching node is found
     */
    public Nodemapper findNode(String input, String that, String topic) {
        Nodemapper result = findNode(root, Path.sentenceToPath(inputThatTopic(input, that, topic)));
        if (verbose) logger.debug("findNode "+inputThatTopic(input, that, topic)+" "+result);
        return result;
    }
    public static boolean verbose = false;

    /**
     * Recursively find a leaf node given a starting node and a path.
     *
     * @param node         string node
     * @param path         string path
     * @return             the leaf node or null if no leaf is found
     */
    Nodemapper findNode(Nodemapper node, Path path) {
        if (path == null && node != null) {
            if (verbose) logger.debug("findNode: path is null, returning node "+node.category.inputThatTopic());
            return node;
        }
    else if (Path.pathToSentence(path).trim().equals("<THAT> * <TOPIC> *") && node.shortCut && path.getWord().equals("<THAT>")) {
            if (verbose) logger.debug("findNode: shortcut, returning "+node.category.inputThatTopic());
            return node;
        }
    else if (NodemapperOperator.containsKey(node, path.getWord())) {
      if (verbose)
        logger.debug("findNode: node contains " + path.getWord());
      Nodemapper nextNode = NodemapperOperator.get(node, path.getWord().toUpperCase());
      return findNode(nextNode, path.getNext());
        }

        else {
            if (verbose) logger.debug("findNode: returning null");
            return null;
        }
    }

    /**
     * Find the matching leaf node given an input, that state and topic value
     *
     * @param input              client input
     * @param that               bot's last sentence
     * @param topic              current topic
     * @return                   matching leaf node or null if no match is found
     */
    public final Nodemapper match(String input, String that, String topic) {
        Nodemapper n = null;
        try {
         String inputThatTopic = inputThatTopic(input, that, topic);
         //logger.debug("Matching: "+inputThatTopic);
         Path p = Path.sentenceToPath(inputThatTopic);
         //p.print();
         n = match(p, inputThatTopic);
         if (MagicBooleans.trace_mode) {
             if (n != null) {
				 //MagicBooleans.trace("in graphmaster.match(), matched "+n.category.inputThatTopic()+" "+n.category.getFilename());
				 if (MagicBooleans.trace_mode) logger.debug ("Matched: "+n.category.inputThatTopic()+" "+n.category.getFilename());
             } else {
				 //MagicBooleans.trace("in graphmaster.match(), no match.");
				 if (MagicBooleans.trace_mode) logger.debug("No match.");
			 }
				 
         }
        } catch (Exception ex) {
            //logger.debug("Match: "+input);
            ex.printStackTrace();
            n = null;
        }
    if (MagicBooleans.trace_mode && Chat.getMatchTrace().length() < MagicNumbers.max_trace_length) {
            if (n != null) {
        Chat.setMatchTrace(Chat.getMatchTrace() + n.category.inputThatTopic() + "\n");
			}
        }
		//MagicBooleans.trace("in graphmaster.match(), returning: " + n);
        return n;
    }

    /**
     * Find the matching leaf node given a path of the form "{@code input <THAT> that <TOPIC> topic}"
     * @param path
     * @param inputThatTopic
     * @return  matching leaf node or null if no match is found
     */
    final Nodemapper match(Path path, String inputThatTopic) {
        try {
        String[] inputStars = new String[MagicNumbers.max_stars];
        String[] thatStars = new String[MagicNumbers.max_stars];
        String[] topicStars = new String[MagicNumbers.max_stars];
        String starState = "inputStar";
        String matchTrace = "";
        Nodemapper n = match(path, root, inputThatTopic, starState, 0, inputStars, thatStars, topicStars, matchTrace);
        if (n != null) {
            StarBindings sb = new StarBindings();
        for (int i = 0; inputStars[i] != null && i < MagicNumbers.max_stars; i++)
          sb.getInputStars().add(inputStars[i]);
        for (int i = 0; thatStars[i] != null && i < MagicNumbers.max_stars; i++)
          sb.getThatStars().add(thatStars[i]);
        for (int i = 0; topicStars[i] != null && i < MagicNumbers.max_stars; i++)
          sb.getTopicStars().add(topicStars[i]);
            n.starBindings = sb;
        }
        //if (!n.category.getPattern().contains("*")) logger.debug("adding match "+inputThatTopic);
        if (n != null) n.category.addMatch(inputThatTopic, bot);
        return n;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Depth-first search of the graph for a matching leaf node.
     * At each node, the order of search is
     * 1. $WORD  (high priority exact word match)
     * 2. # wildcard  (zero or more word match)
     * 3. _ wildcard (one or more words match)
     * 4. WORD (exact word match)
     * 5. {@code <set></set>} (AIML Set match)
     * 6. shortcut (graph shortcut when that pattern = * and topic pattern = *)
     * 7. ^ wildcard  (zero or more words match)
     * 8. * wildcard (one or more words match)
     *
     * @param path      remaining path to be matched
     * @param node      current search node
     * @param inputThatTopic  original input, that and topic string
     * @param starState       tells whether wildcards are in input pattern, that pattern or topic pattern
     * @param starIndex       index of wildcard
     * @param inputStars      array of input pattern wildcard matches
     * @param thatStars       array of that pattern wildcard matches
     * @param topicStars      array of topic pattern wildcard matches
     * @param matchTrace      trace of match path for debugging purposes
     * @return  matching leaf node or null if no match is found
     */
    final Nodemapper match(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode;
        //logger.debug("Match: Height="+node.height+" Length="+path.length+" Path="+Path.pathToSentence(path));
        matchCount++;
        if ((matchedNode = nullMatch(path, node, matchTrace)) != null) return matchedNode;
    else if (path.getLength() < node.height) {
           return null;}

        else if ((matchedNode = dollarMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) return matchedNode;
        else if ((matchedNode = sharpMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) return matchedNode;
        else if ((matchedNode = underMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) return matchedNode;
        else if ((matchedNode = wordMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) return matchedNode;
        else if ((matchedNode = setMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) return matchedNode;
        else if ((matchedNode = shortCutMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) return matchedNode;
        else if ((matchedNode = caretMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) return matchedNode;
        else if ((matchedNode = starMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) return matchedNode;
        else {
            return null;
        }
    }

    /**
     * print out match trace when search fails
     *
     * @param mode   Which mode of search
     * @param trace  Match trace info
     */
    void fail (String mode, String trace) {
       // logger.debug("Match failed ("+mode+") "+trace);
    }

    /**
     * a match is found if the end of the path is reached and the node is a leaf node
     *
     * @param path     remaining path
     * @param node     current search node
     * @param matchTrace   trace of match for debugging purposes
     * @return         matching leaf node or null if no match found
     */
    final Nodemapper nullMatch(Path path, Nodemapper node, String matchTrace) {
        if (path == null && node != null && NodemapperOperator.isLeaf(node) && node.category != null) return node;
        else {
            fail("null", matchTrace);
            return null;
        }
    }


    final Nodemapper shortCutMatch(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
    if (node != null && node.shortCut && path.getWord().equals("<THAT>") && node.category != null) {
            String tail = Path.pathToSentence(path).trim();
            //logger.debug("Shortcut tail = "+tail);
            String that = tail.substring(tail.indexOf("<THAT>")+"<THAT>".length(), tail.indexOf("<TOPIC>")).trim();
            String topic = tail.substring(tail.indexOf("<TOPIC>")+"<TOPIC>".length(), tail.length()).trim();
            //logger.debug("Shortcut that = "+that+" topic = "+topic);
            //logger.debug("Shortcut matched: "+node.category.inputThatTopic());
            thatStars[0] = that;
            topicStars[0] = topic;
            return node;
        }
        else {
            fail("shortCut", matchTrace);
            return null;
        }
    }
    final Nodemapper wordMatch(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode;
        try {
      String uword = path.getWord().toUpperCase();
            if (uword.equals("<THAT>")) {starIndex = 0; starState = "thatStar";}
            else if (uword.equals("<TOPIC>")) {starIndex = 0; starState = "topicStar";}
      // logger.debug("path.getNext()= "+path.getNext()+" node.get="+node.get(uword));
            matchTrace += "["+uword+","+uword+"]";
            if (path != null && NodemapperOperator.containsKey(node, uword) &&
          (matchedNode = match(path.getNext(), NodemapperOperator.get(node, uword), inputThatTopic, starState, starIndex, inputStars,
              thatStars, topicStars, matchTrace)) != null) {
                 return matchedNode;
            } else {
                fail("word", matchTrace);
                return null;
            }
        } catch (Exception ex) {
            logger.debug("wordMatch: "+Path.pathToSentence(path)+": "+ex);
            ex.printStackTrace();
            return null;
        }
    }
    final Nodemapper dollarMatch(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
    String uword = "$" + path.getWord().toUpperCase();
        Nodemapper matchedNode;
    if (path != null && NodemapperOperator.containsKey(node, uword)
        && (matchedNode = match(path.getNext(), NodemapperOperator.get(node, uword), inputThatTopic, starState, starIndex, inputStars,
            thatStars, topicStars, matchTrace)) != null) {
            return matchedNode;
        } else {
            fail("dollar", matchTrace);
            return null;
        }
    }
    final Nodemapper starMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        return wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "*", matchTrace);
    }
    final Nodemapper underMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        return wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "_", matchTrace);
    }
    final Nodemapper caretMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode;
        matchedNode = zeroMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "^", matchTrace);
        if (matchedNode != null) return matchedNode;
        else return wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "^", matchTrace);
    }
    final Nodemapper sharpMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
    // logger.debug("Entering sharpMatch with path.getWord()="+path.getWord()); NodemapperOperator.printKeys(node);
        Nodemapper matchedNode;
        matchedNode = zeroMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "#", matchTrace);
        if (matchedNode != null) return matchedNode;
        else
        return wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "#", matchTrace);
    }
    final Nodemapper zeroMatch(Path path, Nodemapper node, String input, String starState, int starIndex,
                               String[] inputStars, String[] thatStars, String[] topicStars, String wildcard, String matchTrace) {
    // logger.debug("Entering zeroMatch on "+path.getWord()+" "+NodemapperOperator.get(node, wildcard));
        matchTrace += "["+wildcard+",]";
        if (path != null && NodemapperOperator.containsKey(node, wildcard)) {
            //logger.debug("Zero match calling setStars Prop "+MagicStrings.null_star+" = "+bot.properties.get(MagicStrings.null_star));
      setStars(bot.getProperties().get(MagicStrings.null_star), starIndex, starState, inputStars, thatStars, topicStars);
            Nodemapper nextNode = NodemapperOperator.get(node, wildcard);
            return match(path, nextNode, input, starState, starIndex+1, inputStars, thatStars, topicStars, matchTrace);
        }
        else {
            fail("zero "+wildcard, matchTrace);
            return null;
        }

    }
    final Nodemapper wildMatch(Path path, Nodemapper node, String input, String starState, int starIndex,
                               String[] inputStars, String[] thatStars, String[] topicStars, String wildcard, String matchTrace) {
        Nodemapper matchedNode;
    if (path.getWord().equals("<THAT>") || path.getWord().equals("<TOPIC>")) {
            fail("wild1 "+wildcard, matchTrace);
            return null;
        }
        try {
            if (path != null && NodemapperOperator.containsKey(node, wildcard)) {
        matchTrace += "[" + wildcard + "," + path.getWord() + "]";
                String currentWord;
                String starWords;
                Path pathStart;
        currentWord = path.getWord();
                starWords = currentWord+" ";
        pathStart = path.getNext();
                Nodemapper nextNode = NodemapperOperator.get(node, wildcard);
                if (NodemapperOperator.isLeaf(nextNode) && !nextNode.shortCut) {
                    matchedNode = nextNode;
                    starWords = Path.pathToSentence(path);
                    //logger.debug(starIndex+". starwords="+starWords);
                    setStars(starWords, starIndex, starState, inputStars, thatStars, topicStars);
                    return matchedNode;
                }
                else {
          for (path = pathStart; path != null && !currentWord.equals("<THAT>") && !currentWord.equals("<TOPIC>"); path = path.getNext()) {
            matchTrace += "[" + wildcard + "," + path.getWord() + "]";
                        if ((matchedNode = match(path, nextNode, input, starState, starIndex + 1, inputStars, thatStars, topicStars, matchTrace)) != null) {
                            setStars(starWords, starIndex, starState, inputStars, thatStars, topicStars);
                            return matchedNode;
                        }
                        else {
              currentWord = path.getWord();
                            starWords += currentWord + " ";
                        }
                    }
                    fail("wild2 "+wildcard, matchTrace);
                    return null;
                }
            }
        } catch (Exception ex) {
            logger.debug("wildMatch: "+Path.pathToSentence(path)+": "+ex);
        }
        fail("wild3 "+wildcard, matchTrace);
        return null;
    }

      final Nodemapper setMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        if (DEBUG) logger.debug("Graphmaster.setMatch(path: " + path + ", node: " + node + ", input: " + input + ", starState: " + starState + ", starIndex: " + starIndex + ", inputStars, thatStars, topicStars, matchTrace: " + matchTrace + ", )");
    if (node.sets == null || path.getWord().equals("<THAT>") || path.getWord().equals("<TOPIC>"))
      return null;
        if (DEBUG) logger.debug("in Graphmaster.setMatch, setMatch sets ="+node.sets);
        for (String setName : node.sets) {
            if (DEBUG) logger.debug("in Graphmaster.setMatch, setMatch trying type "+setName);
            Nodemapper nextNode = NodemapperOperator.get(node, "<SET>"+setName.toUpperCase()+"</SET>");
      AIMLSet aimlSet = bot.getSetMap().get(setName);
            //logger.debug(aimlSet.setName + "="+ aimlSet);
            Nodemapper matchedNode;
            Nodemapper bestMatchedNode = null;
      String currentWord = path.getWord();
            String starWords = currentWord+" ";
            int length = 1;
      matchTrace += "[<set>" + setName + "</set>," + path.getWord() + "]";
            if (DEBUG) logger.debug("in Graphmaster.setMatch, setMatch starWords =\""+starWords+"\"");
      for (Path qath = path.getNext(); qath != null && !currentWord.equals("<THAT>") && !currentWord.equals("<TOPIC>")
          && length <= aimlSet.maxLength; qath = qath.getNext()) {
        if (DEBUG)
          logger.debug("in Graphmaster.setMatch, qath.word = " + qath.getWord());
        String phrase = bot.getPreProcessor().normalize(starWords.trim()).toUpperCase();
                if (DEBUG) logger.debug("in Graphmaster.setMatch, setMatch trying \""+phrase+"\" in "+setName);
                if (aimlSet.contains(phrase) && (matchedNode = match(qath, nextNode, input, starState, starIndex + 1, inputStars, thatStars, topicStars, matchTrace)) != null) {
                    setStars(starWords, starIndex, starState, inputStars, thatStars, topicStars);
                    if (DEBUG) logger.debug("in Graphmaster.setMatch, setMatch found "+phrase+" in "+ setName);
                    bestMatchedNode = matchedNode;
                }
                //    else if (qath.word.equals("<THAT>") || qath.word.equals("<TOPIC>")) return null;

                length = length + 1;
        currentWord = qath.getWord();
                starWords += currentWord + " ";

            }
            if (bestMatchedNode != null) return bestMatchedNode;
        }
        fail("set", matchTrace);
        return null;
    }

    /*
   * final Nodemapper setMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[]
   * thatStars, String[] topicStars, String matchTrace) { if (DEBUG) logger.debug("Graphmaster.setMatch(path: " + path + ", node: " + node +
   * ", input: " + input + ", starState: " + starState + ", starIndex: " + starIndex + ", inputStars, thatStars, topicStars, matchTrace: " +
   * matchTrace + ", )"); if (node.sets == null || path.getWord().equals("<THAT>") || path.getWord().equals("<TOPIC>")) return null; if
   * (DEBUG) logger.debug("in Graphmaster.setMatch, setMatch sets ="+node.sets); for (String setName : node.sets) { if (DEBUG) logger.debug(
   * "in Graphmaster.setMatch, setMatch trying type "+setName); Nodemapper nextNode = NodemapperOperator.get(node,
   * "<SET>"+setName.toUpperCase()+"</SET>"); AIMLSet aimlSet = bot.setMap.get(setName); //logger.debug(aimlSet.setName + "="+ aimlSet);
   * Nodemapper matchedNode; String currentWord = path.getWord(); String starWords = currentWord+" "; int length = 1; matchTrace +=
   * "[<set>"+setName+"</set>,"+path.getWord()+"]"; if (DEBUG) logger.debug("in Graphmaster.setMatch, setMatch starWords =\""
   * +starWords+"\""); for (Path qath = path.getNext(); qath != null && !currentWord.equals("<THAT>") && !currentWord.equals("<TOPIC>") &&
   * length <= aimlSet.maxLength; qath = qath.next) { if (DEBUG) logger.debug("in Graphmaster.setMatch, qath.word = "+qath.word); String
   * phrase = bot.preProcessor.normalize(starWords.trim()).toUpperCase(); if (DEBUG) logger.debug(
   * "in Graphmaster.setMatch, setMatch trying \""+phrase+"\" in "+setName); if (aimlSet.contains(phrase) && (matchedNode = match(qath,
   * nextNode, input, starState, starIndex + 1, inputStars, thatStars, topicStars, matchTrace)) != null) { setStars(starWords, starIndex,
   * starState, inputStars, thatStars, topicStars); if (DEBUG) logger.debug("in Graphmaster.setMatch, setMatch found "+phrase+" in "+
   * setName); return matchedNode; } // else if (qath.word.equals("<THAT>") || qath.word.equals("<TOPIC>")) return null; else { length =
   * length + 1; currentWord = qath.word; starWords += currentWord + " "; } } } fail("set", matchTrace); return null; }
   */

    public void setStars(String starWords, int starIndex, String starState, String[] inputStars, String[] thatStars, String[] topicStars) {
    if (starIndex < MagicNumbers.max_stars) {
        //logger.debug("starWords="+starWords);
        starWords = starWords.trim();
        if (starState.equals("inputStar")) inputStars[starIndex] = starWords;
        else if (starState.equals("thatStar")) thatStars[starIndex] = starWords;
        else if (starState.equals("topicStar")) topicStars[starIndex] = starWords;
        }
    }
    public void printgraph () {
        printgraph(root, "");
    }
    void printgraph(Nodemapper node, String partial) {
        if (node == null) logger.debug("Null graph");
        else {
            String template = "";
            if (NodemapperOperator.isLeaf(node) || node.shortCut) {
                template = Category.templateToLine(node.category.getTemplate());
                template = template.substring(0, Math.min(16, template.length()));
                if (node.shortCut) logger.debug(partial+"("+NodemapperOperator.size(node)+"["+node.height+"])--<THAT>-->X(1)--*-->X(1)--<TOPIC>-->X(1)--*-->"+template+"...");
                else logger.debug(partial+"("+NodemapperOperator.size(node)+"["+node.height+"]) "+template+"...");
            }
            for (String key : NodemapperOperator.keySet(node)) {
                //logger.debug(key);
                printgraph(NodemapperOperator.get(node, key), partial+"("+NodemapperOperator.size(node)+"["+node.height+"])--"+key+"-->");
            }
        }
    }
    public ArrayList<Category> getCategories() {
        ArrayList<Category> categories = new ArrayList<Category>();
        getCategories(root, categories);
        //for (Category c : categories) logger.debug("getCategories: "+c.inputThatTopic()+" "+c.getTemplate());
        return categories;
    }
    void getCategories(Nodemapper node, ArrayList<Category> categories) {
        if (node == null) return;

        else {
            //String template = "";
            if (NodemapperOperator.isLeaf(node) || node.shortCut) {
                if (node.category != null) categories.add(node.category);   // node.category == null when the category is deleted.
            }
            for (String key : NodemapperOperator.keySet(node)) {
                //logger.debug(key);
                getCategories(NodemapperOperator.get(node, key), categories);
            }
        }
    }

    int leafCnt;
    int nodeCnt;
    long nodeSize;
    int singletonCnt;
    int shortCutCnt;
    int naryCnt;
    public void nodeStats() {
        leafCnt = 0;
        nodeCnt = 0;
        nodeSize = 0;
        singletonCnt = 0;
        shortCutCnt = 0;
        naryCnt = 0;
        nodeStatsGraph(root);
    resultNote = bot.getName() + " (" + name + "): " + getCategories().size() + " categories " + nodeCnt + " nodes " + singletonCnt
        + " singletons " + leafCnt + " leaves " + shortCutCnt + " shortcuts " + naryCnt + " n-ary " + nodeSize + " branches "
        + (float) nodeSize / (float) nodeCnt + " average branching ";
        if (MagicBooleans.trace_mode) logger.debug(resultNote);
    }
    public void nodeStatsGraph(Nodemapper node) {
        if (node != null) {
            //logger.debug("Counting "+node.key+ " size="+NodemapperOperator.size(node));
            nodeCnt++;
            nodeSize += NodemapperOperator.size(node);
            if (NodemapperOperator.size(node) == 1) singletonCnt += 1;
            if (NodemapperOperator.isLeaf(node) && !node.shortCut) {
                leafCnt++;
            }
            if (NodemapperOperator.size(node) > 1) naryCnt += 1;
            if (node.shortCut) {shortCutCnt += 1;}
            for (String key : NodemapperOperator.keySet(node)) {
                    nodeStatsGraph(NodemapperOperator.get(node, key));
            }
        }
    }

    public HashSet<String> getVocabulary () {
        vocabulary = new HashSet<String>();
        getBrainVocabulary(root);
    for (String set : bot.getSetMap().keySet())
      vocabulary.addAll(bot.getSetMap().get(set));
        return vocabulary;
    }
    public void getBrainVocabulary(Nodemapper node) {
        if (node != null) {
            //logger.debug("Counting "+node.key+ " size="+NodemapperOperator.size(node));
            for (String key : NodemapperOperator.keySet(node)) {
                vocabulary.add(key);
                getBrainVocabulary(NodemapperOperator.get(node, key));
            }
        }
    }

  public int getUpgradeCnt() {
    return upgradeCnt;
  }

  public static void enableShortCuts(final boolean b) {
    enableShortCuts = b;
  }
}
