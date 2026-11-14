// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "com_posthog_posthog_kmp_0_10_0",
  platforms: [
    .iOS("15.3")
  ],
  products: [
    .library(
      name: "com_posthog_posthog_kmp_0_10_0",
      type: .none,
      targets: ["com_posthog_posthog_kmp_0_10_0"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/PostHog/posthog-ios.git",
      exact: "3.64.1"
    )
  ],
  targets: [
    .target(
      name: "com_posthog_posthog_kmp_0_10_0",
      dependencies: [
        .product(
          name: "PostHog",
          package: "posthog-ios"
        )
      ]
    )
  ]
)
