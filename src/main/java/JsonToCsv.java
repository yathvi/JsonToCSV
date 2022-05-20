import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/***
 * Convert dynamic/nested JsonArray/JsonObject to CSV file, it doesn't depend on Schema.
 * CSV headers/Column headers will be added as Json hierarchy Key
 * and the Values of dynamic/nested JsonArray/JsonObject will be added as row in the CSV
 * and will be mapped to the right CSV header.
 */
public class JsonToCsv {
    /* CSV Header */
    private static List<String> jsonKeyAsHeader;

    /* JSON Key-Value, Keys added as header in CSV, Values added as row in CSV
     *  Using LinkedHashMap to maintain order of insertion  */
    private static Map<String, String> jsonKeyValue;

    /* List of JSON Key-Value, Keys added as header in CSV, Values added as row in CSV */
    private static List<Map<String, String>> jsonKeyValueList;
    private static List<Map<String, String>> jsonKeyValueList_2;

    private static List<List<Map<String, String>>> jsonKeyValueListOfLists;



    public static void main(String[] args) throws IOException {

        addDifferentJsonsToCsv();
        mergeDifferentJsonsAndAddInCsv();
    }

    /**
     * Read JsonObject.json; store the key - value in Hashmap @param jsonKeyValue,
     * add this multiple Hashmap @param jsonKeyValue to ArrayList @param jsonKeyValueList.
     *
     * Read JsonArray.json; store the key - value in Hashmap @param jsonKeyValue,
     * add this multiple Hashmap @param jsonKeyValue to ArrayList @param jsonKeyValueList.
     *
     * Trigger convertJsontoCsv by passing ArrayList @param jsonKeyValueList.
     *
     * @throws IOException
     */
    private static void addDifferentJsonsToCsv() throws IOException {
        jsonKeyValueList = new ArrayList<>();
        jsonKeyValueListOfLists = new ArrayList<>();

        String jsonObjectFile = "src/main/resources/json/JsonObject.json";
        String jsonObjectStr = readFileAsString(jsonObjectFile);
        /* Append JsonObject Keys*/
        //String appendJsonObjectKey = "jsonObject_";
        String appendJsonObjectKey = "";
        /* Passing JsonObject.json JsonObject*/
        JSONObject jsonObject = new JSONObject(jsonObjectStr);

        jsonKeyAsHeader = new ArrayList<>();
        jsonKeyValue = new LinkedHashMap<>();
        processMap(jsonObject.toMap(),appendJsonObjectKey);
        for (int i =0 ; i < 25 ; i++)
            jsonKeyValueList.add(jsonKeyValue);

        String jsonArrayFile = "src/main/resources/json/JsonArray.json";
        String jsonArrayStr = readFileAsString(jsonArrayFile);
        /*Append JsonArray Keys*/
        //String appendJsonArrayKey = "jsonArray_";
        String appendJsonArrayKey = "";
        /*Passing JsonArray.json JsonArray*/
        JSONArray jsonArray = new JSONArray(jsonArrayStr);

        jsonKeyAsHeader = new ArrayList<>();
        jsonKeyValue = new LinkedHashMap<>();
        processListOfMaps(jsonArray.toList(),appendJsonArrayKey);
        for (int i =0 ; i < 25 ; i++)
            jsonKeyValueList.add(jsonKeyValue);

        jsonKeyValueListOfLists.add(jsonKeyValueList);

        jsonToCsv(jsonKeyValueListOfLists,"src/main/resources/csvHeaders/CsvListHeaders.txt");
    }

    /**
     * Read JsonObject.json; store the key - value in Hashmap @param jsonKeyValue,
     * add this multiple Hashmap @param jsonKeyValue to ArrayList @param jsonKeyValueList.
     *
     * Read JsonArray.json; store the key - value in Hashmap @param jsonKeyValue,
     * add this multiple Hashmap @param jsonKeyValue to ArrayList @param jsonKeyValueList_2.
     *
     * Trigger mergeJsontoCsv by passing ArrayList @param jsonKeyValueList and ArrayList @param jsonKeyValueList_2.
     *
     * @throws IOException
     */
    private static void mergeDifferentJsonsAndAddInCsv() throws IOException {
        jsonKeyValueList = new ArrayList<>();
        jsonKeyValueList_2 = new ArrayList<>();
        jsonKeyValueListOfLists = new ArrayList<>();


        String jsonObjectFile = "src/main/resources/json/JsonObject.json";
        String jsonObjectStr = readFileAsString(jsonObjectFile);
        /* Append JsonObject Keys*/
        String appendJsonObjectKey = "jsonObject_";
        /* Passing JsonObject.json JsonObject*/
        JSONObject jsonObject = new JSONObject(jsonObjectStr);

        jsonKeyAsHeader = new ArrayList<>();
        jsonKeyValue = new LinkedHashMap<>();
        processMap(jsonObject.toMap(),appendJsonObjectKey);
        for (int i =0 ; i < 10 ; i++)
            jsonKeyValueList.add(jsonKeyValue);

        jsonKeyValueListOfLists.add(jsonKeyValueList);

        String jsonArrayFile = "src/main/resources/json/JsonArray.json";
        String jsonArrayStr = readFileAsString(jsonArrayFile);
        /* Append JsonArray Keys*/
        String appendJsonArrayKey = "jsonArray_";
        /* Passing JsonArray.json JsonArray*/
        JSONArray jsonArray = new JSONArray(jsonArrayStr);

        jsonKeyAsHeader = new ArrayList<>();
        jsonKeyValue = new LinkedHashMap<>();
        processListOfMaps(jsonArray.toList(),appendJsonArrayKey);
        for (int i =0 ; i < 10 ; i++)
            jsonKeyValueList_2.add(jsonKeyValue);

        jsonKeyValueListOfLists.add(jsonKeyValueList_2);

        jsonToCsv(jsonKeyValueListOfLists,"src/main/resources/csvHeaders/MergeCsvListHeaders.txt");
    }

    /**
     *
     * @param listOfMaps Process each Map in the List
     * @param json if different json has same key name, use this param which will append string to differentiate them.
     * @return return's true if json is processed
     *         return's false if JsonArray has empty values like [], [{}], [{},{}], [{},{"a":"b"},{},{"c":"d"}] ...
     */
    private static boolean processListOfMaps(List listOfMaps, String json){

        boolean emptyValuesNotPresent = true;

        for (int i = 0 ; i < listOfMaps.size() ; i++)
        {
            /* size()!=0 Check for empty values like {}, [], [{}], [{},{}], [{},{"a":"b"},{},{"c":"d"}] ... */
            if(listOfMaps.get(i) instanceof Map && ((Map<?, ?>) listOfMaps.get(i)).size()!=0) {
                processMap((Map) listOfMaps.get(i),json);
            }
            else {
                emptyValuesNotPresent = false;
            }
        }

        return emptyValuesNotPresent;
    }

    /**
     *
     * @param map keys are added as header in CSV, values are added as rows in CSV
     * @param json if different json has same key name, use this param which will append string to differentiate them.
     */
    private static void processMap(Map map ,String json){

        for (Object s : map.keySet()) {
            jsonKeyAsHeader.add(s + ":");
            /* size()!=0 Check for empty values like {} */
            if (map.get(s) instanceof Map && ((Map<?, ?>) map.get(s)).size()!=0)
            {
                processMap((Map) map.get(s),json);
                jsonKeyAsHeader.remove(jsonKeyAsHeader.size() - 1);
            }
            /* size()!=0 Check for empty values like [], [{}], [{},{}], [{},{"a":"b"},{},{"c":"d"}]... */
            else if (map.get(s) instanceof List && ((List<?>) map.get(s)).size()!=0
                        &&
                            processListOfMaps((List) map.get(s),json))
            {

                jsonKeyAsHeader.remove(jsonKeyAsHeader.size() - 1);
            }
            else {
                StringBuilder headerKey = new StringBuilder();
                headerKey.append(json);
                for (String str : jsonKeyAsHeader) {
                    headerKey.append(str);
                }
                /* If key is there in HashMap, add new key with a counter appended. */
                if(jsonKeyValue.containsKey(String.valueOf(headerKey)))
                {
                    int count = 1;
                    headerKey = appendHeaderKey(count,json);
                }
                //System.out.print(headerKey+"::-->:::");
                //System.out.println(map.get(s));
                jsonKeyValue.put(String.valueOf(headerKey), String.valueOf(map.get(s)));
                jsonKeyAsHeader.remove(jsonKeyAsHeader.size() - 1);
            }
        }
    }

    /**
     * If JsonArray/JsonObject has same key name, add new key with a counter appended.
     * @param count
     * @return
     */

    private static StringBuilder appendHeaderKey(int count, String json) {
        StringBuilder headerKey = new StringBuilder();
        headerKey.append(json);
        for (String str : jsonKeyAsHeader) {
            headerKey.append(str);
        }
        //System.out.println("Before :" + headerKey);
        
        headerKey.append(count);
        if(jsonKeyValue.containsKey(String.valueOf(headerKey)))
        {
            headerKey = appendHeaderKey(++count,json);
        }
        //System.out.println("After :"+headerKey);
        return headerKey;
    }

    /**
     * Merge Different Jsons and add in Csv as a single row based on common value.
     * @param jsonKeyValueListOfLists List of List of Maps(key-values); keys are added as header in CSV, values will be added as rows in CSV.
     * @param keysText Text file that contains Keys which are added as Header in CSV
     * @throws IOException
     */

    private static void jsonToCsv(List<List<Map<String, String>>> jsonKeyValueListOfLists, String keysText) throws IOException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSSZ");
        File listFile = new File("src/main/resources/csv/csv_"+sdf.format(timestamp)+".csv");
        FileWriter listFileWriter = new FileWriter(listFile, true);
        CSVWriter csvWriter = new CSVWriter(listFileWriter);
        Set<String> headerStrSet = new LinkedHashSet<>();

        /* Read Keys if file is not empty*/
        Scanner sc = new Scanner(new File(keysText));
        while(sc.hasNext()){
            String line = sc.nextLine();
            headerStrSet.add(line);
        }
        sc.close();

        if(headerStrSet.isEmpty())
        {
            PrintWriter headerWriter = new PrintWriter(keysText, "UTF-8");
            /*
               Read all HashMap Keys from ArrayList @param jsonKeyValueListOfLists, store in a Set @param headerStrSet and add as a header in CSV.
            */

            for(int i = 0; i < jsonKeyValueListOfLists.size(); i++) {
                for (int j = 0; j < jsonKeyValueListOfLists.get(i).size(); j++) {
                    for (String headerStr : jsonKeyValueListOfLists.get(i).get(j).keySet()) {
                        headerStrSet.add(headerStr);
                    }
                }
            }

            for (String headerStr : headerStrSet) {
                headerWriter.println(headerStr);
            }
            headerWriter.close();

        }

        /* Convert Set @headerStrSet to String array @headerStrArray  */
        String[] headerStrArray = new String[headerStrSet.size()];
        int headerCount = 0;
        for (String headerStr : headerStrSet) {
            headerStrArray[headerCount++] = headerStr;
        }
        csvWriter.writeNext(headerStrArray);

        /* List<List<Map<String, String>>> jsonKeyValueListOfLists */

        for(int i = 0; i < jsonKeyValueListOfLists.size(); i++) {
            for (int j = 0; j < jsonKeyValueListOfLists.get(i).size(); j++) {
                if (i + 1 < jsonKeyValueListOfLists.size()){
                    for (int k = 0; k < jsonKeyValueListOfLists.get(i + 1).size(); k++) {
                        if(StringUtils.equals(jsonKeyValueListOfLists.get(i).get(j).get("jsonObject_key2:key3:key6:key10:"),
                                jsonKeyValueListOfLists.get(i + 1).get(k).get("jsonArray_key5:key6:key10:"))) {
                            String[] mapValueArray = new String[headerStrSet.size()];
                            int l = 0;
                            for (String key : headerStrSet) {
                                if (jsonKeyValueListOfLists.get(i).get(j).containsKey(key))
                                    mapValueArray[l++] = jsonKeyValueListOfLists.get(i).get(j).get(key);
                                else if (jsonKeyValueListOfLists.get(i + 1).get(k).containsKey(key))
                                    mapValueArray[l++] = jsonKeyValueListOfLists.get(i + 1).get(k).get(key);
                                else l++;
                            }
                            csvWriter.writeNext(mapValueArray);
                        }
                    }
                }
            }
        }

        if(jsonKeyValueListOfLists.size() == 1){

            /* Add each HashMap values as a row in CSV. */

            /*jsonKeyValueListOfLists.get(0).forEach(map->{
                ArrayList<String> stringValueArray = new ArrayList<>();
                headerStrSet.forEach(set->stringValueArray.add(map.get(set)));
                csvWriter.writeNext(stringValueArray.stream().toArray(String[]::new));
            });*/

            for (int i = 0; i < jsonKeyValueListOfLists.get(0).size(); i++) {
                String[] mapValueArray = new String[headerStrSet.size()];
                int j = 0;
                for (String key: headerStrSet) {
                    mapValueArray[j++] = jsonKeyValueListOfLists.get(0).get(i).get(key);
                }
                csvWriter.writeNext(mapValueArray);
            }
        }
        csvWriter.close();
    }



    /**
     *
     * @param file Read File as a String
     * @return
     */
    private static String readFileAsString(String file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "readFileAsString failed";
    }
}
