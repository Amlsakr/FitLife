$ErrorActionPreference = "Stop"

$out = "_bmad-output/review-scratch/ob004-full-review.diff"
if (Test-Path -LiteralPath $out) {
    Remove-Item -LiteralPath $out
}
New-Item -ItemType File -Path $out -Force | Out-Null

$gitArgs = @("-c", "safe.directory=D:/LinkDevProject/FitLife")
$trackedPaths = @(
    "_bmad-output/implementation-artifacts/ob-004-onboarding-completion-flag-navigation-graph.md",
    "_bmad-output/implementation-artifacts/deferred-work.md",
    "_bmad-output/implementation-artifacts/sprint-status.yaml",
    "app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt",
    "app/src/main/java/com/aml_sakr/fitlife/StartupBindingsModule.kt",
    "app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt",
    "feature/auth/auth-domain/src/main/java/com/aml_sakr/fitlife/feature/auth/domain/startup/DetermineStartupDestinationUseCase.kt",
    "feature/auth/auth-domain/src/test/java/com/aml_sakr/fitlife/feature/auth/domain/startup/DetermineStartupDestinationUseCaseTest.kt",
    "feature/auth/auth-ui/src/main/java/com/aml_sakr/fitlife/feature/auth/auth_ui/viewmodel/AuthViewModel.kt",
    "feature/auth/auth-ui/src/test/java/com/aml_sakr/fitlife/feature/auth/auth_ui/auth/viewmodel/AuthViewModelTest.kt",
    "feature/onboarding/onboarding-domain",
    "feature/onboarding/onboarding-data",
    "feature/onboarding/onboarding-ui"
)

& git @gitArgs diff HEAD -- $trackedPaths | Add-Content -LiteralPath $out

$untracked = & git @gitArgs ls-files --others --exclude-standard -- `
    "_bmad-output/implementation-artifacts/ob-004-onboarding-completion-flag-navigation-graph.md" `
    "feature/onboarding/onboarding-domain" `
    "feature/onboarding/onboarding-data" `
    "feature/onboarding/onboarding-ui"

foreach ($file in $untracked) {
    if ($file -match "/bin/") {
        continue
    }
    & git @gitArgs diff --no-index -- NUL $file | Add-Content -LiteralPath $out
}

Get-Item -LiteralPath $out | Select-Object FullName,Length
(Get-Content -LiteralPath $out | Measure-Object -Line).Lines
