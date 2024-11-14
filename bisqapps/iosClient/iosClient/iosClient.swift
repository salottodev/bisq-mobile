import SwiftUI
import presentation

@main
struct iosClient: App {
    init() {
        // TODO might need to get away the helper approach in favour of adding koin pods in
        DependenciesProviderHelper().doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
