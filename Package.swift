// swift-tools-version: 5.9
import PackageDescription

// The binary target points at a local build output, so this package resolves only after the
// XCFramework has been assembled:
//
//     ./gradlew :Kodices:assembleKodicesReleaseXCFramework
//
// The Publish workflow attaches `Kodices.xcframework.zip` to every GitHub release and prints its
// checksum in the job summary. To consume a released build instead of a local one, replace the
// target below with:
//
//     .binaryTarget(
//         name: "Kodices",
//         url: "https://github.com/Iktwo/Kodices/releases/download/<tag>/Kodices.xcframework.zip",
//         checksum: "<checksum from the release job summary>"
//     )

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
