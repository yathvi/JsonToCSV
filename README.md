# JsonToCsv
Convert dynamic/nested JsonArray/JsonObject to CSV file, it doesn't depend on Schema.

CSV headers/Column headers will be added as Json hierarchy Key and the Values of dynamic/nested JsonArray/JsonObject will be added as row in the CSV and will be mapped to the right CSV header.

Merge two different JsonArray/JsonObject to CSV file based on common value.
If different JsonArray/JsonObject has same Key name, Append custom string to differentiate them.

The JsonArray/JsonObject should have one of this valid value (JSON String, Number, Array, Object or token 'null', 'true' or 'false')