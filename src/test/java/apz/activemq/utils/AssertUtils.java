package apz.activemq.utils;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import java.util.function.Supplier;

import static java.lang.Thread.sleep;
import static java.util.Objects.requireNonNull;

public class AssertUtils {

    public static void retry(final Runnable runnable) {

        requireNonNull(runnable, "runnable must not be null");

        for (int i = 0; i < 49; i++) {
            try {
                runnable.run();
                return;
            } catch (final Throwable throwable) {
                try {
                    sleep(100);
                } catch (final InterruptedException e) {
                    // do nothing
                }
            }
        }

        runnable.run();
    }

    public static <T> void assertThat(final String reason, final Supplier<T> actual, final Matcher<? super T> matcher) {
        retry(() -> MatcherAssert.assertThat(reason, actual.get(), matcher));
    }
}
