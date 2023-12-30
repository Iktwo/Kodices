package com.iktwo.kodices

import com.iktwo.kodices.dataprocessors.JSONDrillerProcessor
import com.iktwo.kodices.dataprocessors.StringProcessor
import com.iktwo.kodices.dataprocessors.StylerProcessor
import com.iktwo.kodices.utils.Constants
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

val sampleArrayData = buildJsonArray {
    addJsonObject {
        put("scientificName", "Calypte anna")
        putJsonArray("commonNames") {
            add("Anna's Hummingbird")
        }
    }

    addJsonObject {
        put("scientificName", "Melanerpes formicivorus")
        putJsonArray("commonNames") {
            add("Acorn woodpecker")
        }
    }

    addJsonObject {
        put("scientificName", "Todus mexicanus")
        putJsonArray("commonNames") {
            add("San Pedrito")
            add("Puerto Rican Tody")
        }
    }
}

val sampleProcessedElementSource = buildJsonObject {
    put(Constants.TYPE, "row")
}

val sampleInterimElementSource = buildJsonObject {
    put(Constants.TYPE, "row")

    putJsonObject(Constants.PROCESSORS) {
        putJsonObject(Constants.TEXT_KEY) {
            put(Constants.TYPE, JSONDrillerProcessor.TYPE)

            putJsonArray(Constants.ELEMENTS) {
                add(1)
                add("scientificName")
            }
        }
    }
}

val sampleInterimElementSourceWithAction = buildJsonObject {
    put(Constants.TYPE, "row")

    putJsonObject(Constants.PROCESSORS) {
        putJsonObject(Constants.TEXT_KEY) {
            put(Constants.TYPE, JSONDrillerProcessor.TYPE)

            putJsonArray(Constants.ELEMENTS) {
                add(1)
                add("scientificName")
            }
        }
    }

    putJsonObject(Constants.ACTION) {
        put(Constants.TYPE, "sample")
    }
}

val sampleContent = buildJsonObject {
    putJsonArray(Constants.ELEMENTS) {
        addJsonObject {
            put(Constants.TYPE, "row")

            putJsonObject(Constants.PROCESSORS) {
                putJsonObject(Constants.TEXT_KEY) {
                    put(Constants.TYPE, JSONDrillerProcessor.TYPE)
                    put(Constants.ELEMENT, "scientificName")
                }

                putJsonArray(Constants.TEXT_SECONDARY_KEY) {
                    addJsonObject {
                        put(Constants.TYPE, JSONDrillerProcessor.TYPE)
                        put(Constants.ELEMENT, "commonNames")
                    }

                    addJsonObject {
                        put(Constants.TYPE, StylerProcessor.TYPE)
                        put(Constants.ELEMENT, "uppercase")
                    }

                    addJsonObject {
                        put(Constants.TYPE, StringProcessor.TYPE)
                        put(Constants.ELEMENT, "Common names: %")
                    }
                }
            }

            putJsonObject(Constants.EXPAND_WITH_PROCESSOR) {
                put(Constants.TYPE, JSONDrillerProcessor.TYPE)
            }
        }
    }
}

val sampleContentThatNeedsNoData = buildJsonObject {
    putJsonArray(Constants.ELEMENTS) {
        addJsonObject {
            put(Constants.TYPE, "row")

            putJsonObject(Constants.CONSTANTS) {
                put(Constants.TEXT_KEY, "Test row")
            }
        }
    }
}

val sampleRowElement = buildJsonObject {
    put(Constants.TYPE, "row")

    put(Constants.TEXT_KEY, "Test row")
    put(Constants.TEXT_SECONDARY_KEY, "Test row subtitle")
}

val sampleProcessedElementWithNestedElements = buildJsonObject {
    put(Constants.TYPE, "row")

    put(Constants.TEXT_KEY, "Top row")

    putJsonArray(Constants.NESTED_ELEMENTS) {
        add(
            buildJsonObject {
                put(Constants.TYPE, "row")

                put(Constants.TEXT_KEY, "Test row")

                putJsonArray(Constants.NESTED_ELEMENTS) {
                    add(
                        buildJsonObject {
                            put(Constants.TYPE, "row")
                            put(Constants.TEXT_KEY, "Two level nested row")
                            put("another_key", "some_value")
                        },
                    )
                }
            },
        )
    }
}

val sampleInterimElement = buildJsonObject {
    put(Constants.TYPE, "row")

    putJsonObject(Constants.PROCESSORS) {
        putJsonObject(Constants.TEXT_KEY) {
            put(Constants.TYPE, JSONDrillerProcessor.TYPE)
            put(Constants.ELEMENT, "scientificName")
        }

        putJsonArray(Constants.TEXT_SECONDARY_KEY) {
            addJsonObject {
                put(Constants.TYPE, JSONDrillerProcessor.TYPE)
                put(Constants.ELEMENT, "commonNames")
            }

            addJsonObject {
                put(Constants.TYPE, StylerProcessor.TYPE)
                put(Constants.ELEMENT, "uppercase")
            }

            addJsonObject {
                put(Constants.TYPE, StringProcessor.TYPE)
                put(Constants.ELEMENT, "Common names: %")
            }
        }
    }
}

val sampleInvalidDataProcessorInElement = buildJsonObject {
    put(Constants.TYPE, "row")

    putJsonObject(Constants.PROCESSORS) {
        putJsonArray(Constants.TEXT_KEY) {
            add(2)
        }
    }
}

val sampleUnknownDataProcessorInElement = buildJsonObject {
    put(Constants.TYPE, "row")

    putJsonObject(Constants.PROCESSORS) {
        putJsonArray(Constants.TEXT_KEY) {
            addJsonObject {
                put(Constants.TYPE, "invalid")
            }
        }
    }
}
