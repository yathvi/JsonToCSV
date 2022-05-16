import com.opencsv.CSVWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/***
 * Convert dynamic/nested JsonArray/JsonObject to CSV file, it doesn't depend on Schema.
 * CSV headers/Column headers will be added as Json hierarchy Key
 * and the Values of dynamic/nested JsonArray/JsonObject will be added as row in the CSV
 * and will be mapped to the right CSV header.
 */
public class JsonToCsv {
    /* CSV Header */
    private static ArrayList<String> jsonKeyAsHeader;

    /* JSON Key-Value, Keys added as header in CSV, Values added as row in CSV
     *  Using LinkedHashMap to maintain order of insertion  */
    private static HashMap<String, String> jsonKeyValue;

    /* List of JSON Key-Value, Keys added as header in CSV, Values added as row in CSV */
    private static ArrayList<HashMap<String, String>> jsonKeyValueList;
    private static ArrayList<HashMap<String, String>> jsonKeyValueList_2;


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

        convertJsontoCsv(jsonKeyValueList,"src/main/resources/csvHeaders/CsvListHeaders.txt");
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

        mergeJsontoCsv(jsonKeyValueList,jsonKeyValueList_2,"src/main/resources/csvHeaders/MergeCsvListHeaders.txt");
    }

    /**
     *
     * @param listOfMaps Process each Map in the array
     * @param json if different json has same key name, use this param which will append string to differentiate them.
     */
    private static void processListOfMaps(List listOfMaps, String json){
        listOfMaps.forEach(map-> {
            if (map instanceof Map) {
                processMap((Map) map,json);
            }
        });
    }

    /**
     *
     * @param map keys are added as header in CSV, values are added as rows in CSV
     * @param json if different json has same key name, use this param which will append string to differentiate them.
     */
    private static void processMap(Map map ,String json){

        for (Object s : map.keySet()) {
            jsonKeyAsHeader.add(s + ":");
            if (map.get(s) instanceof Map)
            {
                processMap((Map) map.get(s),json);
                jsonKeyAsHeader.remove(jsonKeyAsHeader.size() - 1);
            }
            else if (map.get(s) instanceof List)
            {
                processListOfMaps((List) map.get(s),json);
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
     * Add each Dynamic/nested Json as a row in CSV.
     * @param jsonKeyValueList ArrayList of HashMaps(key-values),
     *                              keys are added as header in CSV,
     *                              values will be added as rows in CSV.
     * @throws IOException
     */
    private static void convertJsontoCsv(ArrayList<HashMap<String, String>> jsonKeyValueList, String keysText) throws IOException {

        Path fileToDeletePath = Paths.get("src/main/resources/csv/CsvList.csv");
        Files.deleteIfExists(fileToDeletePath);
        File listFile = new File("src/main/resources/csv/CsvList.csv");
        FileWriter listFileWriter = new FileWriter(listFile, true);
        CSVWriter csvWriter = new CSVWriter(listFileWriter);
        Set<String> headerStrSet = new LinkedHashSet<>();

        /* Read Keys if file is not empty*/
        Scanner sc = new Scanner(new File(keysText));
        while(sc.hasNext()){
            String line = sc.nextLine();
            headerStrSet.add(line);
        }

        /*
         Read all HashMap Keys from ArrayList, store in a Set @headerStrSet and add as a header in CSV.
         */

        /*jsonKeyValueList.forEach(map-> headerStrSet.addAll(map.keySet()));
          csvWriter.writeNext(headerStrSet.stream().toArray(String[]::new));*/

        if(headerStrSet.isEmpty()) {
            PrintWriter headerWriter = new PrintWriter(keysText, "UTF-8");
            for (int i = 0; i < jsonKeyValueList.size(); i++) {
                for (String headerStr : jsonKeyValueList.get(i).keySet()) {
                    headerStrSet.add(headerStr);
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


        /* Add each HashMap values as a row in CSV. */

           /* jsonKeyValueList.forEach(map->{
                ArrayList<String> stringValueArray = new ArrayList<>();
                headerStrSet.forEach(set->stringValueArray.add(map.get(set)));
                csvWriter.writeNext(stringValueArray.stream().toArray(String[]::new));
            });*/

        for (int i = 0; i < jsonKeyValueList.size(); i++) {
            String[] mapValueArray = new String[headerStrSet.size()];
            int j = 0;
            for (String key : headerStrSet) {
                mapValueArray[j++] = jsonKeyValueList.get(i).get(key);
            }
            csvWriter.writeNext(mapValueArray);
        }
        csvWriter.close();
    }

    /**
     * Merge Different Jsons and add in Csv as a single row based on common value. In this case key10.
     * @param jsonKeyValueList ArrayList of HashMaps(key-values); first Json, keys are added as header in CSV, values will be added as rows in CSV.
     * @param jsonKeyValueList_2 ArrayList of HashMaps(key-values); second Json, keys are added as header in CSV, values will be added as rows in CSV.
     * @throws IOException
     */
    private static void mergeJsontoCsv(ArrayList<HashMap<String, String>> jsonKeyValueList, ArrayList<HashMap<String, String>> jsonKeyValueList_2, String keysText) throws IOException {

        Path fileToDeletePath = Paths.get("src/main/resources/csv/MergeCsvList.csv");
        Files.deleteIfExists(fileToDeletePath);
        File listFile = new File("src/main/resources/csv/MergeCsvList.csv");
        FileWriter listFileWriter = new FileWriter(listFile, true);
        CSVWriter csvWriter = new CSVWriter(listFileWriter);
        Set<String> headerStrSet = new LinkedHashSet<>();

        /* Read Keys if file is not empty*/
        Scanner sc = new Scanner(new File(keysText));
        while(sc.hasNext()){
            String line = sc.nextLine();
            headerStrSet.add(line);
        }

        if(headerStrSet.isEmpty()) {
            PrintWriter headerWriter = new PrintWriter(keysText, "UTF-8");
            /*
                Read all HashMap Keys from ArrayList @param jsonKeyValueList, store in a Set @param headerStrSet and add as a header in CSV.
            */
            for (int i = 0; i < jsonKeyValueList.size(); i++) {
                for (String headerStr : jsonKeyValueList.get(i).keySet()) {
                    headerStrSet.add(headerStr);
                }
            }

            /*
                Read all HashMap Keys from ArrayList @param jsonKeyValueList_2, store in a Set @param headerStrSet and add as a header in CSV.
            */
            for (int i = 0; i < jsonKeyValueList_2.size(); i++) {
                for (String headerStr : jsonKeyValueList_2.get(i).keySet()) {
                    headerStrSet.add(headerStr);
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


        /* If key10 value of JsonObject and JsonArray match, then merge JsonObject and JsonArray as a row in CSV. */

        for (int i = 0; i < jsonKeyValueList.size(); i++) {
            for (int j = 0; j < jsonKeyValueList_2.size(); j++) {
                if(jsonKeyValueList.get(i).containsKey("jsonObject_key2:key3:key6:key10:")
                        &&
                        jsonKeyValueList_2.get(j).containsKey("jsonArray_key5:key6:key10:")
                        &&
                        jsonKeyValueList.get(i).get("jsonObject_key2:key3:key6:key10:")
                                .equals(jsonKeyValueList_2.get(j).get("jsonArray_key5:key6:key10:"))) {

                    String[] mapValueArray = new String[headerStrSet.size()];
                    int k = 0;
                    for (String key : headerStrSet) {
                        if (jsonKeyValueList.get(i).containsKey(key))
                            mapValueArray[k++] = jsonKeyValueList.get(i).get(key);
                        else if (jsonKeyValueList_2.get(j).containsKey(key))
                            mapValueArray[k++] = jsonKeyValueList_2.get(j).get(key);
                        else k++;
                    }
                    csvWriter.writeNext(mapValueArray);
                }
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
