import UIKit
import SwiftUI
import ui

struct ComposeView: UIViewControllerRepresentable {
    private let presenter = MainPresenter(greetingRepository: GreetingRepository()) // Initialize the presenter for iOS

    func makeUIViewController(context: Context) -> UIViewController {
        return LifecycleAwareComposeViewController(presenter: presenter)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}
