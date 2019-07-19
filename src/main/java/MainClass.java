import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ADBA
 * Date : 2/28/2019
 * Time : 3:06 PM
 */

public class MainClass {
    private static final String lineFeed = "\r\n";

    private static final String END_COMMA = "},";
    private static final String TOOK = "\\{\"took\":.*";
    private static final String SCORE = ",\"_score\":[0-9]+.[0-9]+,\"_source\":";
    private static final String HITS = "\"hits\":\\{.*\\[";
    private static final String END_BRACES = "}]}}";
    private static final String FIRST = "\\{\"took\":.*},\"hits\":\\{.*\\[";
    private static final String INDEX_TYPE = "\"_index\":\".*\",\"_type\":\".*\",\"_id\":.*}";
    private static Charset defCharset = Charset.defaultCharset();



    public static void main(String args[]) {
        String indexName = args[0];
        String typeName = args[1];


        File inputFile = new File(args[2]);
        File finalFile = new File(args[3]);
        File outputFile = new File(".\\" + System.currentTimeMillis() + ".json");


        try {
            outputFile.createNewFile();
            List<String> content = Files.readLines(inputFile, defCharset);
            for (String next : content) {

                if (next.contains(END_COMMA)) {
                    next = next.replace(END_COMMA, "\n");
                }

                if (next.contains(END_BRACES)) {
                    next = next.replace(END_BRACES, "\n");
                }

                Files.asCharSink(outputFile, defCharset, FileWriteMode.APPEND).write(next);
                Files.asCharSink(outputFile, defCharset, FileWriteMode.APPEND).write(lineFeed);
            }

            System.out.println(new Date(System.currentTimeMillis()) + "\tFormatting complete");

            finalFile.createNewFile();
            List<String> output = Files.readLines(outputFile, defCharset);
            for(String next : output) {


                next = next.replaceAll(HITS, "");
                next = next.replaceAll(SCORE, "}\n");

                if(matchRegex(next, TOOK)){
                    continue;
                }
                next = next.replaceAll(INDEX_TYPE, "\"index\":{\"_index\":\""+indexName+"\",\"_type\":\""+typeName+"\"}\t}");
                next = next.replaceAll("\\}\\}\n", "}");

                Files.asCharSink(finalFile, defCharset, FileWriteMode.APPEND).write(next);
                Files.asCharSink(finalFile, defCharset, FileWriteMode.APPEND).write(lineFeed);
            }

            outputFile.delete();
            System.out.println(new Date(System.currentTimeMillis()) + "\tFile created : " + finalFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static boolean matchRegex(String content, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }

        /*
    if(matchRegex(next, HITS)){
        System.out.println("Found the match for : " + HITS);
        next = next.replaceAll(HITS, "");
    }

    if(matchRegex(next, SCORE)){
        System.out.println("Found the match for : " + SCORE);
        next = next.replaceAll(SCORE, "");
    }*/
}
