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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.tests.health.HealthTest;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class FunctionalTestRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalTestRunner.class);
    public static void main(String[] args) {
        loadRequiredSystemEnvVars(args);
        boolean criticalTestPassed = runHealthTest();
        if (!criticalTestPassed) {
            LOGGER.error("❌ Health actuator test failed! Application may not be running. Exiting test execution.");
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

        LOGGER.info("🚀 STARTING TEST EXECUTION");
        LOGGER.info("=" .repeat(60));

        long startTime = System.currentTimeMillis();
        launcher.execute(request);
        long endTime = System.currentTimeMillis();

        LOGGER.info("=" .repeat(60));
        LOGGER.info("📋 TEST EXECUTION COMPLETED");

        printVerboseSummary(summaryListener.getSummary(), endTime - startTime);
    }

    private static boolean runHealthTest() {
        LOGGER.info("\n=== RUNNING CRITICAL TEST ===");
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
                    LOGGER.info("▶️  RUNNING: {}", testIdentifier.getDisplayName());
                }
            }

            @Override
            public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
                if (testIdentifier.isTest()) {
                    String statusIcon = getStatusIcon(result.getStatus());
                    LOGGER.info("{} FINISHED: {} -> {}",
                            statusIcon, testIdentifier.getDisplayName(), result.getStatus());

                    // Print failure details immediately
                    result.getThrowable().ifPresent(throwable -> {
                        LOGGER.error("   💥 FAILURE: {}", throwable.getMessage());
                        if (throwable.getCause() != null) {
                            LOGGER.error("   🔗 CAUSE: {}", throwable.getCause().getMessage());
                        }
                    });
                    LOGGER.info("-" .repeat(60));
                }
            }

            @Override
            public void executionSkipped(TestIdentifier testIdentifier, String reason) {
                LOGGER.info("⏭️  SKIPPED: {}", testIdentifier.getDisplayName());
                LOGGER.info("   📝 Reason: {}", reason);
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
        LOGGER.info("\n📊 DETAILED TEST SUMMARY");
        LOGGER.info("═".repeat(50));

        // Basic counts
        LOGGER.info("Total tests found:    {}", summary.getTestsFoundCount());
        LOGGER.info("Tests started:        {}", summary.getTestsStartedCount());
        LOGGER.info("Tests succeeded:      {}", summary.getTestsSucceededCount());
        LOGGER.info("Tests failed:         {}", summary.getTestsFailedCount());
        LOGGER.info("Tests aborted:        {}", summary.getTestsAbortedCount());
        LOGGER.info("Tests skipped:        {}", summary.getTestsSkippedCount());

        // Success rate
        double successRate = summary.getTestsStartedCount() > 0 ?
                (double) summary.getTestsSucceededCount() / summary.getTestsStartedCount() * 100 : 0;
        LOGGER.info("Success rate:         {}", successRate);

        // Timing
        long minutes = totalDuration / 60000;
        long seconds = (totalDuration % 60000) / 1000;
        long millis = totalDuration % 1000;
        LOGGER.info("Total duration: {}m {}s {}ms", minutes, seconds, millis);

        // Failure details
        if (summary.getTestsFailedCount() > 0) {
            LOGGER.error("\n❌ FAILURE DETAILS");
            LOGGER.error("-".repeat(30));

            summary.getFailures().forEach(failure -> {
                TestIdentifier test = failure.getTestIdentifier();
                Throwable exception = failure.getException();

                LOGGER.error("Test: {}", test.getDisplayName());
                LOGGER.error("Exception: {}", exception.getClass().getSimpleName());
                LOGGER.error("Message: {}", exception.getMessage());

                if (exception.getCause() != null) {
                    LOGGER.error("Root cause: {}", exception.getCause().getMessage());
                }

                // Print first few lines of stack trace
                LOGGER.info("Stack trace (first 3 lines):");
                StackTraceElement[] stackTrace = exception.getStackTrace();
                for (int i = 0; i < Math.min(3, stackTrace.length); i++) {
                    LOGGER.info("  at {}", stackTrace[i]);
                }
                LOGGER.info("\n");
            });
        }

        // Summary verdict
        LOGGER.info("═".repeat(50));
        if (summary.getTestsFailedCount() == 0) {
            LOGGER.info("🎉 ALL TESTS PASSED!");
        } else {
            LOGGER.info("💥 {} TEST(S) FAILED", summary.getTestsFailedCount());
        }
        LOGGER.info("═".repeat(50));
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