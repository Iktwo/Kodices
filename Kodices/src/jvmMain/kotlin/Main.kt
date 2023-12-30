import com.iktwo.kodices.Kodices

fun main(args: Array<String>) {
    Kodices.logger.info("--Kodices--")
    Kodices.debug = true
    Kodices().parseJSONToContent("{\"elements\":[{\"type\":\"sample\"}]}")
}
