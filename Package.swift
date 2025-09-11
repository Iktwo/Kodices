// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "Kodices",
    products: [
        .library(
            name: "Kodices",
            targets: ["Kodices"]
        )
    ],
    targets: [
        .binaryTarget(
            name: "Kodices",
            path: "Kodices/build/XCFrameworks/release/Kodices.xcframework"
        ),
    ]
)
