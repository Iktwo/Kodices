<img src="../resources/images/kodices.svg" alt="Auto Dismiss" style="width:50%; height:auto;">

# Kodices

Kotlin multiplatform library to parse JSON models that describe user interfaces.

## Usage

### Consuming in a Gradle

Add this to your dependencies:

    implementation("com.iktwo:kodices:kodices_version")

### Consuming through Swift Package Manager:

You can point to this repository to consume this as a Swift Package:

    https://github.com/Iktwo/Kodices-iOS

## Core Concepts

### Element

Used to describe what is usually mapped to some UI element.

### Action

An operation that may be triggered from an element.

Built-in actions include:

* MessageAction: Used to show a message.

New actions can be added through the ActionsRegistry. 

### DataProcessor

Operations that define how to transform data.

#### JSONDrillerProcessor

A processor that can retrieve data.

You can reference a JSON key as a string or use a number for the index of an item in an array. 

Sample JSON:

    {
        "name": "Kodices",
        "metadata": {
            "language": "Kotlin",
            "supportedPlatforms": ["Android", "iOS", "Linux", "MacOS", "Windows"]
        }
    }

Sample 1:

    JSONDrillerProcessor(
        listOf(
            StringRoute("name")
        )    
    )

Result:

    "Kodices"

Sample 2:

    JSONDrillerProcessor(
        listOf(
            StringRoute("metadata"),
            StringRoute("language")
        )    
    )

Result: "Kotlin"

Sample 3:

    JSONDrillerProcessor(
        listOf(
            StringRoute("metadata"),
            StringRoute("supportedPlatforms"),
            NumberRoute(0)
        )    
    )

Result: 

    "Android"

##### Creating from JSON

You can create a JSONDrillerProcessor with this JSON:

    {
        "type" : "path",
        "elements" : [..]
    }

#### StringProcessor

A processor to inject data in a string. Typically used to show data alongside some text.

Sample JSON:

    {
        "name": "Kodices",
        "metadata": {
            "language": "Kotlin",
            "supportedPlatforms": ["Android", "iOS", "Linux", "MacOS", "Windows"]
        }
    }

Sample 1:

    StringProcessor(
        "Project name: %"    
    )

Processing that with "Kodices" (in combination with a **JSONDrillerProcessor**) will result in:

    "Project name: Kodices"

##### Creating from JSON

You can create a StringProcessor with this JSON:

    {
        "type" : "string",
        "elements" : ".."
    }

#### StylerProcessor

A processor to style values.

This is generally used to transform how data looks like. For example to display a string as UPPERCASE or lowercase.

Sample JSON:

    {
        "name": "Kodices",
        "metadata": {
            "language": "Kotlin",
            "supportedPlatforms": ["Android", "iOS", "Linux", "MacOS", "Windows"]
        }
    }

Sample 1:

    StylerProcessor(
        "UPPERCASE"    
    )

Processing that with "Kodices" (in combination with a **JSONDrillerProcessor**) will result in:

    "KODICES"