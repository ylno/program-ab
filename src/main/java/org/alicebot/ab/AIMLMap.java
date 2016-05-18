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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.seibertmedia.chatbot.CommandLineInteraction;
import net.seibertmedia.chatbot.UserInteraction;

   /**
    * implements AIML Map
    *
    * A map is a function from one string set to another.
    * Elements of the domain are called keys and elements of the range are called values.
    *
*/
public class AIMLMap extends HashMap<String, String> {

       private static final Logger logger = LoggerFactory.getLogger(AIMLMap.class);

  private UserInteraction userinteraction;

    public String  mapName;
    String host; // for external maps
    String botid; // for external maps
    boolean isExternal = false;
    Inflector inflector = new Inflector();
    Bot bot;

       /**
        * constructor to create a new AIML Map
        *
        * @param name      the name of the map
        */
    public AIMLMap (String name, Bot bot) {
        super();
        this.bot = bot;
        this.mapName = name;
        userinteraction = new CommandLineInteraction();
    }

       /**
        * return a map value given a key
        *
        * @param key          the domain element
        * @return             the range element or a string indicating the key was not found
        */
    public String get(String key) {
        String value;
        if (mapName.equals(MagicStrings.map_successor)) {
            try {
                int number = Integer.parseInt(key);
                return String.valueOf(number+1);
            } catch (Exception ex) {
                return MagicStrings.default_map;
            }
        }
        else if (mapName.equals(MagicStrings.map_predecessor)) {
            try {
                int number = Integer.parseInt(key);
                return String.valueOf(number-1);
            } catch (Exception ex) {
                return MagicStrings.default_map;
            }
        }
        else if (mapName.equals("singular")) {
            return inflector.singularize(key).toLowerCase();
        }
        else if (mapName.equals("plural")) {
            return inflector.pluralize(key).toLowerCase();
        }
        else if (isExternal && MagicBooleans.enable_external_sets) {
            //String[] split = key.split(" ");
            String query = mapName.toUpperCase()+" "+key;
            String response = Sraix.sraix(null, query, MagicStrings.default_map, null, host, botid, null, "0");
            userinteraction.outputForUserWithNewline("External "+mapName+"("+key+")="+response);
            value = response;
        }
        else value = super.get(key);
        if (value == null) value = MagicStrings.default_map;

    logger.debug("AIMLMap map {} get " + key + "=" + value, mapName);
        return value;
    }

       /**
        * put a new key, value pair into the map.
        *
        * @param key    the domain element
        * @param value  the range element
        * @return       the value
        */
    public String put(String key, String value) {
        //logger.debug("AIMLMap put "+key+"="+value);
        return super.put(key, value);
    }


       public  void writeAIMLMap () {
           logger.debug("Writing AIML Map "+mapName);
           try{
               // Create file
      FileWriter fstream = new FileWriter(bot.getMaps_path() + "/" + mapName + ".txt");
               BufferedWriter out = new BufferedWriter(fstream);
               for (String p : this.keySet()) {
                   p = p.trim();
                   //logger.debug(p+"-->"+this.get(p));
                   out.write(p+":"+this.get(p).trim());
                   out.newLine();
               }
               //Close the output stream
               out.close();
           }catch (Exception e){//Catch exception if any
               System.err.println("Error: " + e.getMessage());
           }
       }
    public int readAIMLMapFromInputStream(InputStream in, Bot bot)  {
        int cnt=0;
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        //Read File Line By Line
        try {
            while ((strLine = br.readLine()) != null  && strLine.length() > 0)   {
                String[] splitLine = strLine.split(":");
                //logger.debug("AIMLMap line="+strLine);
                if (splitLine.length >= 2) {
                    cnt++;
                    if (strLine.startsWith(MagicStrings.remote_map_key)) {
                        if (splitLine.length >= 3) {
                            host = splitLine[1];
                            botid = splitLine[2];
                            isExternal = true;
                            userinteraction.outputForUserWithNewline("Created external map at "+host+" "+botid);
                        }
                    }
                    else {
                      String key = splitLine[0].toUpperCase();
                      String value = splitLine[1];
                      // assume domain element is already normalized for speedier load
                      //key = bot.preProcessor.normalize(key).trim();
                      put(key, value);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cnt;
    }

       /**
        * read an AIML map for a bot
        *
        * @param bot          the bot associated with this map.
        */
    public int readAIMLMap (Bot bot) {
        int cnt = 0;
    if (MagicBooleans.trace_mode)
      logger.debug("Reading AIML Map " + bot.getMaps_path() + "/" + mapName + ".txt");
        try{
            // Open the file that is the first
            // command line parameter
      File file = new File(bot.getMaps_path() + "/" + mapName + ".txt");
            if (file.exists()) {
        FileInputStream fstream = new FileInputStream(bot.getMaps_path() + "/" + mapName + ".txt");
                // Get the object
                cnt = readAIMLMapFromInputStream(fstream, bot);
                fstream.close();
            }
      else
        logger.debug(bot.getMaps_path() + "/" + mapName + ".txt not found");
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return cnt;

    }

  public int readAIMLMapJson(Bot bot) {
    int cnt = 0;
    if (MagicBooleans.trace_mode)
      logger.debug("Reading AIML Map " + bot.getMaps_path() + "/" + mapName + ".map");
    try {
      // Open the file that is the first
      // command line parameter
      File file = new File(bot.getMaps_path() + "/" + mapName + ".map");
      if (file.exists()) {
        FileInputStream fstream = new FileInputStream(bot.getMaps_path() + "/" + mapName + ".map");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        StringBuilder stringBuilder = new StringBuilder();
        String strLine;
        while ((strLine = br.readLine()) != null && strLine.length() > 0) {
          stringBuilder.append(strLine);
        }
        fstream.close();

        // Parse json
        JSONArray json = new JSONArray(stringBuilder.toString());
        logger.debug("json-array " + json);
        int i = 0;
        for (i = 0; i <= json.length(); i++) {
          logger.debug("add {}", json.getString(i));
          JSONArray jsonArray = json.getJSONArray(i);
          String key = jsonArray.getString(0);
          String value = jsonArray.getString(1);
          this.put(key, value);
          // this.add(json.getString(i));
          // String key = splitLine[0].toUpperCase();
          // String value = splitLine[1];
        }

        // Get the object
        cnt = readAIMLMapFromInputStream(fstream, bot);
        fstream.close();
      }

      // assume domain element is already normalized for speedier load
      // key = bot.preProcessor.normalize(key).trim();
      // put(key, value);

      else
        logger.debug(bot.getMaps_path() + "/" + mapName + ".map not found");
    } catch (Exception e) {// Catch exception if any
      System.err.println("Error: " + e.getMessage());
    }
    return cnt;

  }

}
