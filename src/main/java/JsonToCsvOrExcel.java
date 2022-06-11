import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/***
 * Convert dynamic/nested JSONArray/JSONObject to CSV/Excel file, it doesn't depend on Schema.
 * CSV/Excel headers/Column headers will be added as Json hierarchy Key
 * and the Values of dynamic/nested JSONArray/JSONObject will be added as row in the CSV/Excel
 * and will be mapped to the right CSV/Excel header.
 */
public class JsonToCsvOrExcel {
    private static final Logger logger = LoggerFactory.getLogger(JsonToCsvOrExcel.class);

    /* CSV Header */
    private static List<String> jsonKeyAsHeader;

    /* JSON Key-Value, Keys added as header in CSV, Values added as row in CSV
     *  Using LinkedHashMap to maintain order of insertion  */
    private static Map<String, String> jsonKeyValue;

    /* List of JSON Key-Value, Keys added as header in CSV, Values added as row in CSV */
    private static List<Map<String, String>> jsonKeyValueList;

    private static List<List<Map<String, String>>> jsonKeyValueListOfLists;

    private static final int CSV_OR_EXCEL_COLUMNS_SIZE = 16384;

    public static void main(String[] args) throws IOException {

        addDifferentJsonsToCsv();
        mergeDifferentJsonsAndAddInCsv();
    }

    /**
     * Read JSONObject.json; store the key - value in Hashmap @param jsonKeyValue,
     * add this multiple Hashmap @param jsonKeyValue to ArrayList @param jsonKeyValueList.
     *
     * Read JSONArray.json; store the key - value in Hashmap @param jsonKeyValue,
     * add this multiple Hashmap @param jsonKeyValue to ArrayList @param jsonKeyValueList.
     *
     * Trigger convertJsontoCsv by passing ArrayList @param jsonKeyValueList.
     *
     * @throws IOException
     */
    private static void addDifferentJsonsToCsv() throws IOException {
        jsonKeyValueList = new ArrayList<>();
        jsonKeyValueListOfLists = new ArrayList<>();

        String jsonObjectFile = "src/main/resources/json/JSONObject.json";
        String jsonObjectStr = readFileAsString(jsonObjectFile);
        /* Append JSONObject Keys*/
        //String appendJsonObjectKey = "jsonObject_";
        String appendJsonObjectKey = "";
        /* Passing JSONObject.json JSONObject*/
        JSONObject jsonObject = new JSONObject(jsonObjectStr);

        jsonKeyAsHeader = new ArrayList<>();
        jsonKeyValue = new LinkedHashMap<>();
        processMap(jsonObject.toMap(),appendJsonObjectKey);
        for (int i =0 ; i < 25 ; i++)
            jsonKeyValueList.add(jsonKeyValue);

        String jsonArrayFile = "src/main/resources/json/JSONArray.json";
        String jsonArrayStr = readFileAsString(jsonArrayFile);
        /*Append JSONArray Keys*/
        //String appendJsonArrayKey = "jsonArray_";
        String appendJsonArrayKey = "";
        /*Passing JSONArray.json JSONArray*/
        JSONArray jsonArray = new JSONArray(jsonArrayStr);

        jsonKeyAsHeader = new ArrayList<>();
        jsonKeyValue = new LinkedHashMap<>();
        processListOfMaps(jsonArray.toList(),appendJsonArrayKey);
        for (int i =0 ; i < 25 ; i++)
            jsonKeyValueList.add(jsonKeyValue);

        jsonKeyValueListOfLists.add(jsonKeyValueList);

        jsonToCsvOrExcel(jsonKeyValueListOfLists,"src/main/resources/csvHeaders/ListHeaders.txt");
    }

    /**
     * Read JSONObject.json; store the key - value in Hashmap @param jsonKeyValue,
     * add this multiple Hashmap @param jsonKeyValue to ArrayList @param jsonKeyValueList.
     *
     * Read JSONArray.json; store the key - value in Hashmap @param jsonKeyValue,
     * add this multiple Hashmap @param jsonKeyValue to ArrayList @param jsonKeyValueList_2.
     *
     * Trigger mergeJsontoCsv by passing ArrayList @param jsonKeyValueList and ArrayList @param jsonKeyValueList_2.
     *
     * @throws IOException
     */
    private static void mergeDifferentJsonsAndAddInCsv() throws IOException {

        jsonKeyValueListOfLists = new ArrayList<>();

        String jsonObjectFile = "src/main/resources/json/JSONObject.json";
        String jsonObjectStr = readFileAsString(jsonObjectFile);
        /* Append JSONObject Keys*/
        String appendJsonObjectKey = "jsonObject_";
        jsonKeyValueList = new ArrayList<>();
        for (int i = 1001 ; i <= 1010 ; i++){
            /* Passing JSONObject.json JsonObject*/
            JSONObject jsonObject = new JSONObject(jsonObjectStr);
            jsonObject.put("uniqueKey",i);
            jsonKeyAsHeader = new ArrayList<>();
            jsonKeyValue = new LinkedHashMap<>();
            processMap(jsonObject.toMap(),appendJsonObjectKey);
            jsonKeyValueList.add(jsonKeyValue);
        }
        jsonKeyValueListOfLists.add(jsonKeyValueList);

        String jsonArrayFile = "src/main/resources/json/JSONArray.json";
        String jsonArrayStr = readFileAsString(jsonArrayFile);
        /* Append JSONArray Keys*/
        String appendJsonArrayKey = "jsonArray_";
        jsonKeyValueList = new ArrayList<>();
        for (int i = 1001 ; i <= 1010 ; i++) {
            /* Passing JSONArray.json JSONArray*/
            JSONArray jsonArray = new JSONArray(jsonArrayStr);
            jsonArray.getJSONObject(0).put("uniqueKey",i);
            jsonKeyAsHeader = new ArrayList<>();
            jsonKeyValue = new LinkedHashMap<>();
            processListOfMaps(jsonArray.toList(),appendJsonArrayKey);
            jsonKeyValueList.add(jsonKeyValue);
        }
        jsonKeyValueListOfLists.add(jsonKeyValueList);

        jsonToCsvOrExcel(jsonKeyValueListOfLists,"src/main/resources/csvHeaders/MergeListHeaders.txt");
    }

    /**
     * @param listOfMaps Process each Map in the List
     * @param json if different json has same key name, use this param which will append string to differentiate them.
     * @return return's true if json is processed
     *         return's false if JSONArray has empty values like [], [{}], [{},{}], [{},{"a":"b"},{},{"c":"d"}] ...
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
                StringBuffer headerKey = new StringBuffer();
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
                //logger.debug(headerKey+"::-->:::");
                //logger.debugln(map.get(s));
                jsonKeyValue.put(String.valueOf(headerKey), String.valueOf(map.get(s)));
                jsonKeyAsHeader.remove(jsonKeyAsHeader.size() - 1);
            }
        }
    }

    /**
     * If JSONArray/JSONObject has same key name, add new key with a counter appended.
     * @param count
     * @param serviceName Append serviceName to CSV/Excel Header/Key
     * @return headerKey
     */
    private static StringBuffer appendHeaderKey(int count, String serviceName) {

        StringBuffer headerKey = new StringBuffer();
        headerKey.append(serviceName);
        for (String str : JsonToCsvOrExcel.jsonKeyAsHeader) {
            headerKey.append(str);
        }
        //logger.debug("Before :" + headerKey);

        headerKey.append(count);
        if(JsonToCsvOrExcel.jsonKeyValue.containsKey(Objects.toString(headerKey)))
        {
            headerKey = appendHeaderKey(++count, serviceName);
        }
        //logger.debug("After :"+headerKey);

        return headerKey;
    }

    /**
     * Merge Different Jsons and add in CSV/Excel as a single row based on common value.
     * @param jsonKeyValueListOfLists List of List of Maps(key-values); keys are added as header in CSV/Excel, values will be added as rows in CSV/Excel.
     * @param keysText Text file that contains Keys which are added as Header in CSV/Excel
     * @throws IOException
     */
    private static void jsonToCsvOrExcel(List<List<Map<String, String>>> jsonKeyValueListOfLists, String keysText) throws IOException {

        Set<String> allServiceHeaderKeySet = new LinkedHashSet<>();
        Set<String> headerKeySet = new LinkedHashSet<>();
        List<Set<String>> listOfHeaderKeySet = new ArrayList<>();
        Map<String, String> mergeJsonKeyValue = new LinkedHashMap<>();
        List<Map<String,String>> mergeJsonKeyValueList = new ArrayList<>();

        /* Read Keys if file is not empty*/
        Scanner sc = new Scanner(new File(keysText));
        while(sc.hasNext()){
            String headerKey = sc.nextLine();
            headerKeySet = buildCSVOrExcelColumns(allServiceHeaderKeySet, headerKeySet, listOfHeaderKeySet, headerKey);
        }
        sc.close();

        /* Add Set @param headerKeySet to List @param listOfHeaderKeySet, if it's not equal to zero and not equal to @param CSV_COLUMNS_SIZE*/
        if (headerKeySet.size() > 1)
        {
            listOfHeaderKeySet.add(headerKeySet);
        }

        if(headerKeySet.isEmpty())
        {
            PrintWriter headerWriter = new PrintWriter(keysText, "UTF-8");
            /*
               Read all HashMap Keys from ArrayList @param jsonKeyValueListOfLists, store in a Set @param headerKeySet and add as a header in CSV/Excel.
            */

            for(int i = 0; i < jsonKeyValueListOfLists.size(); i++) {
                for (int j = 0; j < jsonKeyValueListOfLists.get(i).size(); j++) {
                    for (String headerKey : jsonKeyValueListOfLists.get(i).get(j).keySet()) {

                        headerKeySet = buildCSVOrExcelColumns(allServiceHeaderKeySet, headerKeySet, listOfHeaderKeySet, headerKey);

                    }
                }
            }

            /* Add Set @param headerKeySet to List @param listOfHeaderKeySet, if it's not equal to zero and not equal to @param CSV_COLUMNS_SIZE*/
            if (headerKeySet.size() > 1)
            {
                listOfHeaderKeySet.add(headerKeySet);
            }

            /* Write the keys to the .txt files in csvHeaders folder */
            for (Set<String> headerKeySubSet: listOfHeaderKeySet) {
                for (String headerStr : headerKeySubSet) {
                    headerWriter.println(headerStr);
                }
            }

            headerWriter.close();
        }


        int forLoopCount=0;
        PrintWriter forLoopWriter = new PrintWriter("src/main/resources/text/forLoopCount.txt", "UTF-8");

        /* Recursive call
         *
         * i -> service1
         * j -> service2
         *
         * Compare 2 services
         * i=0 j=1
         *
         * Compare 3 services
         * i=0 j=1
         * i=0 j=2
         * i=1 j=2
         *
         * Compare 5 services
         *  i=0 j=1
         *  i=0 j=2
         *  i=0 j=3
         *  i=0 j=4
         *  i=1 j=2
         *  i=1 j=3
         *  i=1 j=4
         *  i=2 j=3
         *  i=2 j=4
         *  i=3 j=4
         * */
        for(int i = 0; i < jsonKeyValueListOfLists.size(); i++)
        {
            for (int j = i + 1; j < jsonKeyValueListOfLists.size(); j++)
            {
                for (int k = 0; k < jsonKeyValueListOfLists.get(i).size(); k++)
                {
                    for (int l = 0; l < jsonKeyValueListOfLists.get(j).size(); l++)
                    {
                        forLoopWriter.println("i -> "+i +" j -> "+j+ " k -> "+k+" l -> "+l);

                        /*logger.debug("i -> "+i +" j -> "+j+ " k -> "+k+" l -> "+l);
                        logger.debug(Objects.toString(jsonKeyValueListOfLists.get(i).get(k)));
                        logger.debug(Objects.toString(jsonKeyValueListOfLists.get(j).get(l)));*/

                        forLoopCount++;

                        if(StringUtils.equalsIgnoreCase(jsonKeyValueListOfLists.get(i).get(k).get("jsonObject_uniqueKey:"),jsonKeyValueListOfLists.get(j).get(l).get("jsonArray_uniqueKey:")))
                        {
                            /*logger.debug("i -> "+i +" j -> "+j+ " k -> "+k+" l -> "+l);
                            logger.debug("jsonObject_uniqueKey:" + jsonKeyValueListOfLists.get(i).get(k).get("jsonObject_uniqueKey:") + " " + "jsonArray_uniqueKey:" + jsonKeyValueListOfLists.get(j).get(l).get("jsonArray_uniqueKey:"));*/
                            boolean itsNewJsonKeyValue = true;

                            for (Map<String,String> jsonKeyValue : mergeJsonKeyValueList) {
                                if(jsonKeyValue.containsValue(jsonKeyValueListOfLists.get(i).get(k).get("jsonObject_uniqueKey:"))) {
                                    mergeJsonKeyValue = jsonKeyValue;
                                    itsNewJsonKeyValue = false;
                                    break;
                                }
                            }

                            if(itsNewJsonKeyValue)
                                mergeJsonKeyValue = new LinkedHashMap<>();

                            for(String list1Key : jsonKeyValueListOfLists.get(i).get(k).keySet()){
                                mergeJsonKeyValue.put(list1Key,jsonKeyValueListOfLists.get(i).get(k).get(list1Key));
                            }

                            for(String list2Key : jsonKeyValueListOfLists.get(j).get(l).keySet()){
                                mergeJsonKeyValue.put(list2Key,jsonKeyValueListOfLists.get(j).get(l).get(list2Key));
                            }

                            if(itsNewJsonKeyValue)
                                mergeJsonKeyValueList.add(mergeJsonKeyValue);

                        }
                    }

                }
            }
        }

        forLoopWriter.close();

        if(jsonKeyValueListOfLists.size() == 1)
        {
            mergeJsonKeyValueList = jsonKeyValueListOfLists.get(0);
        }

        logger.debug("<----------------------------------------->");

        if(jsonKeyValueListOfLists.size() > 1)
        {
            logger.debug("Total forLoopCount : " + forLoopCount);
        }

        logger.debug("Total number of rows in CSV/Excel : " + mergeJsonKeyValueList.size());
        logger.debug("Total number of Keys : " + allServiceHeaderKeySet.size());

        writeToCsv(listOfHeaderKeySet, mergeJsonKeyValueList);
        writeToExcel(listOfHeaderKeySet, mergeJsonKeyValueList);

    }

    /**
     * @param allServiceHeaderKeySet Set of all CSV/Excel Column Header Keys
     * @param headerKeySet SubSet of CSV/Excel Column Header Keys
     * @param listOfHeaderKeySet List of CSV/Excel Column Header Key Set
     * @param headerKey CSV/Excel Column Header Key
     * @return
     */
    private static Set<String> buildCSVOrExcelColumns(Set<String> allServiceHeaderKeySet, Set<String> headerKeySet, List<Set<String>> listOfHeaderKeySet, String headerKey) {
        if (!allServiceHeaderKeySet.contains(headerKey))
            headerKeySet.add(headerKey);

        allServiceHeaderKeySet.add(headerKey);

        /* Create a new @param headerKeySet object if current @param headerKeySet.size() is equal to @param CSV_COLUMNS_SIZE */
        if (headerKeySet.size() == CSV_OR_EXCEL_COLUMNS_SIZE) {
            listOfHeaderKeySet.add(headerKeySet);
            headerKeySet = new LinkedHashSet<>();
        }
        return headerKeySet;
    }

    /**
     * @param listOfHeaderKeySet Add as Column Header in CSV/Excel.
     * @param mergeJsonKeyValueList Add as Rows in CSV/Excel.
     */
    private static void writeToCsv(List<Set<String>> listOfHeaderKeySet, List<Map<String,String>> mergeJsonKeyValueList ) throws IOException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSSZ");
        String csvName = "src/main/resources/csv/csv_" + sdf.format(timestamp) + ".csv";
        File csvFile = new File(csvName);
        FileWriter csvFileWriter = new FileWriter(csvFile, true);
        CSVWriter csvWriter = new CSVWriter(csvFileWriter);

        LocalDateTime startTime = LocalDateTime.now();
        for(int numberOfCsv = 0 ; numberOfCsv < listOfHeaderKeySet.size() ; numberOfCsv++)
        {
            /* Skip for first CSV */
            if(numberOfCsv != 0) {
                csvName = "src/main/resources/csv/csv_" + sdf.format(timestamp) + "_" + numberOfCsv + ".csv";
                csvFile = new File(csvName);
                csvFileWriter = new FileWriter(csvFile, true);
                csvWriter = new CSVWriter(csvFileWriter);
            }

            String[] headerStrArray = new String[listOfHeaderKeySet.get(numberOfCsv).size()];
            int headerCount = 0;
            for (String headerStr : listOfHeaderKeySet.get(numberOfCsv)) {
                headerStrArray[headerCount++] = headerStr;
            }
            csvWriter.writeNext(headerStrArray);

            /* Read List of Maps @param mergeJsonKeyValueList and Add as rows in CSV. */
            for (Map<String,String> jsonKeyValue : mergeJsonKeyValueList)
            {
                String[] csvRowArray = new String[listOfHeaderKeySet.get(numberOfCsv).size()];
                int i = 0;
                for (String key : listOfHeaderKeySet.get(numberOfCsv)) {
                    if(jsonKeyValue.containsKey(key)) {
                        csvRowArray[i++] = jsonKeyValue.get(key);
                    } else i++;
                }
                csvWriter.writeNext(csvRowArray);
            }
            csvWriter.close();
        }
        logger.debug("Generated CSV, Total time took: " + startTime.until(LocalDateTime.now(), ChronoUnit.MILLIS));

    }

    /**
     * @param listOfHeaderKeySet Add as Column Header in CSV/Excel.
     * @param mergeJsonKeyValueList Add as Rows in CSV/Excel.
     * @throws IOException
     */
    private static void writeToExcel(List<Set<String>> listOfHeaderKeySet, List<Map<String,String>> mergeJsonKeyValueList ) throws IOException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSSZ");
        Workbook workbook = new SXSSFWorkbook();

        LocalDateTime startTime = LocalDateTime.now();

        for(int sheet = 0 ; sheet < listOfHeaderKeySet.size() ; sheet++)
        {
            int rowCounter = 0;
            Sheet workbookSheet = workbook.createSheet("Sheet" + Objects.toString(sheet+1));
            Row headerRow = workbookSheet.createRow(rowCounter++);
            int rowCount = 0;
            for (String headerKey : listOfHeaderKeySet.get(sheet)) {
                Cell cell = headerRow.createCell(rowCount++);
                cell.setCellValue(headerKey);
            }

            for (Map<String, String> jsonKeyValue : mergeJsonKeyValueList) {
                Row row = workbookSheet.createRow(rowCounter++);
                rowCount = 0;
                for (String key : listOfHeaderKeySet.get(sheet)) {
                    if (jsonKeyValue.containsKey(key)) {
                        Cell cell = row.createCell(rowCount++);
                        cell.setCellValue(jsonKeyValue.get(key));
                    } else {
                        row.createCell(rowCount++);
                    }
                }
            }
        }

        FileOutputStream fileOut = new FileOutputStream("src/main/resources/xlsx/xls_" + sdf.format(timestamp) + ".xlsx");
        workbook.write(fileOut);
        fileOut.flush();
        fileOut.close();
        workbook.close();

        logger.debug("Generated Excel, Total time took: " + startTime.until(LocalDateTime.now(), ChronoUnit.MILLIS));
        logger.debug("<----------------------------------------->");

    }

    /**
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