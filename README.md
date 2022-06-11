# JsonToCsvOrExcel
Convert dynamic/nested JSONArray/JSONObject to CSV/Excel file, it doesn't depend on Schema.

CSV/Excel headers/Column headers will be added as Json hierarchy Key and the Values of dynamic/nested JSONArray/JSONObject will be added as row in the CSV/Excel and will be mapped to the right CSV/Excel header.

https://github.com/yathvi/JsonToCsvOrExcel/blob/main/src/main/resources/csv/CsvList.csv
https://github.com/yathvi/JsonToCsvOrExcel/blob/main/src/main/resources/xlsx/XlsxList.xlsx

Merge two different JSONArray/JSONObject to CSV/Excel file based on common value.
If different JSONArray/JSONObject has same Key name, Append custom string to differentiate them.

https://github.com/yathvi/JsonToCsvOrExcel/blob/main/src/main/resources/csv/MergeCsvList.csv
https://github.com/yathvi/JsonToCsvOrExcel/blob/main/src/main/resources/xlsx/MergeXlsxList.xlsx

The JSONArray/JSONObject should have one of this valid value (JSON String, Number, Array, Object or token 'null', 'true' or 'false')