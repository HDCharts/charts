import SwiftUI
import app

@main
struct iOSApp: App {
    init() {
        KoinAppKt.doInitKoin()
    }
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
