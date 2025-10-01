package org.voyager.tests;

import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.voyager.tests.health.HealthTest;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class FunctionalTestRunner {
    public static void main(String[] args) {
        loadRequiredSystemEnvVars(args);
        boolean criticalTestPassed = runHealthTest();
        if (!criticalTestPassed) {
            System.out.println("❌ Health actuator test failed! Application may not be running. Exiting test execution.");
            System.exit(1);
        }
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectPackage(FunctionalTestRunner.class.getPackage().getName())
                )
                .build();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();

        TestExecutionListener verboseListener = createVerboseConsoleListener();

        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(summaryListener, verboseListener);

        System.out.println("🚀 STARTING TEST EXECUTION");
        System.out.println("=" .repeat(60));

        long startTime = System.currentTimeMillis();
        launcher.execute(request);
        long endTime = System.currentTimeMillis();

        System.out.println("=" .repeat(60));
        System.out.println("📋 TEST EXECUTION COMPLETED");

        // Print detailed summary
        printVerboseSummary(summaryListener.getSummary(), endTime - startTime);
    }

    private static boolean runHealthTest() {System.out.println("\n=== RUNNING CRITICAL TEST ===");
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(HealthTest.class))
                .build();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        return summary.getTestsFailedCount() == 0;
    }

    private static TestExecutionListener createVerboseConsoleListener() {
        return new TestExecutionListener() {
            @Override
            public void executionStarted(TestIdentifier testIdentifier) {
                if (testIdentifier.isTest()) {
                    System.out.printf("▶️  RUNNING: %s%n", testIdentifier.getDisplayName());
                }
            }

            @Override
            public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
                if (testIdentifier.isTest()) {
                    String statusIcon = getStatusIcon(result.getStatus());
                    System.out.printf("%s FINISHED: %s -> %s%n",
                            statusIcon, testIdentifier.getDisplayName(), result.getStatus());

                    // Print failure details immediately
                    result.getThrowable().ifPresent(throwable -> {
                        System.out.printf("   💥 FAILURE: %s%n", throwable.getMessage());
                        if (throwable.getCause() != null) {
                            System.out.printf("   🔗 CAUSE: %s%n", throwable.getCause().getMessage());
                        }
                    });
                    System.out.println("-" .repeat(60));
                }
            }

            @Override
            public void executionSkipped(TestIdentifier testIdentifier, String reason) {
                System.out.printf("⏭️  SKIPPED: %s%n", testIdentifier.getDisplayName());
                System.out.printf("   📝 Reason: %s%n", reason);
            }

            private String getStatusIcon(TestExecutionResult.Status status) {
                return switch (status) {
                    case SUCCESSFUL -> "✅";
                    case FAILED -> "❌";
                    case ABORTED -> "⚠️ ";
                };
            }
        };
    }

    private static void printVerboseSummary(TestExecutionSummary summary, long totalDuration) {
        System.out.println("\n📊 DETAILED TEST SUMMARY");
        System.out.println("═".repeat(50));

        // Basic counts
        System.out.printf("Total tests found:    %d%n", summary.getTestsFoundCount());
        System.out.printf("Tests started:        %d%n", summary.getTestsStartedCount());
        System.out.printf("Tests succeeded:      %d%n", summary.getTestsSucceededCount());
        System.out.printf("Tests failed:         %d%n", summary.getTestsFailedCount());
        System.out.printf("Tests aborted:        %d%n", summary.getTestsAbortedCount());
        System.out.printf("Tests skipped:        %d%n", summary.getTestsSkippedCount());

        // Success rate
        double successRate = summary.getTestsStartedCount() > 0 ?
                (double) summary.getTestsSucceededCount() / summary.getTestsStartedCount() * 100 : 0;
        System.out.printf("Success rate:         %.1f%%%n", successRate);

        // Timing
        System.out.printf("Total duration:       %d ms%n", totalDuration);

        // Failure details
        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n❌ FAILURE DETAILS");
            System.out.println("-".repeat(30));

            summary.getFailures().forEach(failure -> {
                TestIdentifier test = failure.getTestIdentifier();
                Throwable exception = failure.getException();

                System.out.printf("Test: %s%n", test.getDisplayName());
                System.out.printf("Exception: %s%n", exception.getClass().getSimpleName());
                System.out.printf("Message: %s%n", exception.getMessage());

                if (exception.getCause() != null) {
                    System.out.printf("Root cause: %s%n", exception.getCause().getMessage());
                }

                // Print first few lines of stack trace
                System.out.println("Stack trace (first 3 lines):");
                StackTraceElement[] stackTrace = exception.getStackTrace();
                for (int i = 0; i < Math.min(3, stackTrace.length); i++) {
                    System.out.printf("  at %s%n", stackTrace[i]);
                }
                System.out.println();
            });
        }

        // Summary verdict
        System.out.println("═".repeat(50));
        if (summary.getTestsFailedCount() == 0) {
            System.out.println("🎉 ALL TESTS PASSED!");
        } else {
            System.out.printf("💥 %d TEST(S) FAILED%n", summary.getTestsFailedCount());
        }
        System.out.println("═".repeat(50));
    }

    private static void loadRequiredSystemEnvVars(String[] args) {
        String baseUrl = args.length > 0 ? args[0] : System.getenv("VOYAGER_URL");
        if (StringUtils.isBlank(baseUrl)) {
            throw new RuntimeException(String.format("Required system environment variable 'VOYAGER_URL' has invalid value: '%s'",baseUrl));
        }
        System.setProperty("VOYAGER_URL",baseUrl);
        String authToken = args.length > 1 ? args[1] : System.getenv("VOYAGER_AUTH_TOKEN");
        if (StringUtils.isBlank(authToken)) {
            throw new RuntimeException(String.format("Required system environment variable 'VOYAGER_AUTH_TOKEN' has invalid value: '%s'",authToken));
        }
        System.setProperty("VOYAGER_AUTH_TOKEN",authToken);
    }
}