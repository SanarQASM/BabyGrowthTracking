import UIKit
import SwiftUI
import ComposeApp

/**
 * ContentView.swift — NO FUNCTIONAL CHANGES NEEDED FOR LANDSCAPE.
 *
 * ComposeUIViewController already fills 100 % of the UIViewController bounds
 * including safe-area handling via `.ignoresSafeArea()`.
 *
 * When iOS rotates the device:
 *  1. UIKit changes the view controller's bounds.
 *  2. SwiftUI propagates the new size to ComposeView.
 *  3. Compose's LocalWindowInfo.containerSize updates automatically.
 *  4. BabyGrowthTheme reads the new size → new ScreenInfo (isLandscape=true/false).
 *  5. LocalIsLandscape updates → HomeScreen switches between bottom-nav and rail.
 *
 * No Kotlin or Swift code beyond what already exists is needed here.
 */
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all) // let Compose manage safe-area insets itself
    }
}