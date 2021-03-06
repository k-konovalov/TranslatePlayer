/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package ru.konovalovk.subtitle_parser.subs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * This class represents the .SRT subtitle format
 * <br><br>
 * Copyright (c) 2012 J. David Requejo <br>
 * j[dot]david[dot]requejo[at] Gmail
 * <br><br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * <br><br>
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 * <br><br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @author J. David Requejo
 */
public class FormatSRT extends TimedTextFileFormat {


    public TimedTextObject parseFile(String fileName, String[] inputString) throws IOException {

        TimedTextObject tto = new TimedTextObject();
        Caption caption = new Caption();
        int captionNumber = 1;
        boolean ignoreUntilNextCaption;
        boolean subtitleHasIncreasingNumber;

        //the file name is saved
        tto.fileName = fileName;

        int lineCounter = 0;
        int stringIndex = 0;
        String line;
        String nextLine;
        try {
            line = getLine(inputString, stringIndex++);
            while (line != null && stringIndex < inputString.length) {
                line = line.trim();
                lineCounter++;
                //if its a blank line, ignore it, otherwise...
                if (!line.isEmpty()) {
                    //check if this caption has increasing number
                    try {
                        int num = Integer.parseInt(line);
                        subtitleHasIncreasingNumber = true;

                    } catch (Exception e) {
                        subtitleHasIncreasingNumber = false;
                    }
                        //we go to next line, here the begin and end time should be found
                        try {
                            ignoreUntilNextCaption = false;
                            lineCounter++;
//                            if a caption didn't have increasing number
//                            so we allready read the line about time so don't read it again
//                            like below example
                            /*
                            1
                            00:00:07,591 --> 00:00:10,427
                            - sync and corrections by Mr. C -
                            - www.addic7ed.com -
                            2
                            00:00:35,118 --> 00:00:36,662
                                    <i>and the people who got hurt.</i>

                            <--- below caption has no increasing number ---->
                            00:00:30,614 --> 00:00:32,199
                            <i>and the damage left behind...</i>
                            */
                            if (subtitleHasIncreasingNumber)
                                line = getLine(inputString, stringIndex++).trim();
                            String start = line.substring(0, 12);
                            String end = line.substring(line.length() - 12, line.length());
                            Time time = new Time("hh:mm:ss,ms", start);
                            caption.start = time;
                            time = new Time("hh:mm:ss,ms", end);
                            caption.end = time;
                        } catch (Exception e) {
                            tto.warnings += "incorrect time format at line " + lineCounter;
                            ignoreUntilNextCaption = true;
                        }
                    if (!ignoreUntilNextCaption) {
                        //we go to next line where the caption text starts
                        lineCounter++;
                        line = getLine(inputString, stringIndex++).trim();
                        String text = "";
                        while (!line.isEmpty() && stringIndex < inputString.length) {
                            nextLine = getLine(inputString, stringIndex++).trim();
                            lineCounter++;
                            text += line;

                            if(!nextLine.isEmpty())
                                text += "<br />";
                            line = nextLine;
                        }
                        caption.content = text;
                        int key = caption.start.mseconds;
                        //in case the key is already there, we increase it by a millisecond, since no duplicates are allowed
                        while (tto.captions.containsKey(key)) key++;
                        if (key != caption.start.mseconds)
                            tto.warnings += "caption with same start time found...\n\n";
                        //we add the caption.
                        tto.captions.put(key, caption);
                    }
                    //we go to next blank
                    while (!line.isEmpty() && stringIndex < inputString.length) {
                        line = getLine(inputString, stringIndex++).trim();
                        lineCounter++;
                    }
                    caption = new Caption();
                }
                if (stringIndex < inputString.length) {
                    line = getLine(inputString, stringIndex++);
                }
            }

        } catch (NullPointerException e) {
            tto.warnings += "unexpected end of file, maybe last caption is not complete.\n\n";
        }

        tto.built = true;
        return tto;
    }


    public String[] toFile(TimedTextObject tto) {

        //first we check if the TimedTextObject had been built, otherwise...
        if (!tto.built)
            return null;

        //we will write the lines in an ArrayList,
        int index = 0;
        //the minimum size of the file is 4*number of captions, so we'll take some extra space.
        ArrayList<String> file = new ArrayList<>(5 * tto.captions.size());
        //we iterate over our captions collection, they are ordered since they come from a TreeMap
        Collection<Caption> c = tto.captions.values();
        Iterator<Caption> itr = c.iterator();
        int captionNumber = 1;

        while (itr.hasNext()) {
            //new caption
            Caption current = itr.next();
            //number is written
            file.add(index++, "" + captionNumber++);
            //we check for offset value:
            if (tto.offset != 0) {
                current.start.mseconds += tto.offset;
                current.end.mseconds += tto.offset;
            }
            //time is written
            file.add(index++, current.start.getTime("hh:mm:ss,ms") + " --> " + current.end.getTime("hh:mm:ss,ms"));
            //offset is undone
            if (tto.offset != 0) {
                current.start.mseconds -= tto.offset;
                current.end.mseconds -= tto.offset;
            }
            //text is added
            String[] lines = cleanTextForSRT(current);
            int i = 0;
            while (i < lines.length)
                file.add(index++, "" + lines[i++]);
            //we add the next blank line
            file.add(index++, "");
        }

        return file.toArray(new String[file.size()]);
    }


	/* PRIVATE METHODS */

    /**
     * This method cleans caption.content of XML and parses line breaks.
     */
    private String[] cleanTextForSRT(Caption current) {
        String[] lines;
        String text = current.content;
        //add line breaks
        lines = text.split("<br />");
        //clean XML
        for (int i = 0; i < lines.length; i++) {
            //this will destroy all remaining XML tags
            lines[i] = lines[i].replaceAll("<.*?>", "");
        }
        return lines;
    }

}
