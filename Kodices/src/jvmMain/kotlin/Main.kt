import com.iktwo.kodices.Kodices

fun main() {
    Kodices.logger.info("--Kodices--")
    Kodices.debug = true
    Kodices().parseJSONToContent("{\"elements\":[{\"type\":\"sample\"}]}")
}
