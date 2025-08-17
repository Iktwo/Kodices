import UIKit
import SwiftUI
import KodicesSampleApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        TabView {
                    ComposeView()
                        .tabItem {
                            Label("Compose", systemImage: "square.and.pencil")
                        }
                        .ignoresSafeArea(.keyboard)

                    KodicesSwiftUIContentView()
                        .tabItem {
                            Label("SwiftUI", systemImage: "swift")
                        }
                }
    }
}
