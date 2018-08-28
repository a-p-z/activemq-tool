package apz.activemq.utils;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Assume;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

public class AssertUtils {

    public static void retry(final @Nonnull Runnable runnable) {

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

    public static <T> void assumeThat(final String reason, final Supplier<T> actual, final Matcher<? super T> matcher) {
        retry(() -> Assume.assumeThat(reason, actual.get(), matcher));
    }

    public static <T> void assertThat(final String reason, final Supplier<T> actual, final Matcher<? super T> matcher) {
        retry(() -> Assert.assertThat(reason, actual.get(), matcher));
    }
}
