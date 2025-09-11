import com.iktwo.kodices.KodicesParser

fun main() {
    KodicesParser.logger.info("--Kodices--")
    KodicesParser.debug = true
    KodicesParser().parseJSONToContent("{\"elements\":[{\"type\":\"sample\"}]}")
}
