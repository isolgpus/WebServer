package io.kiw.luxis.web.test;

import io.kiw.luxis.web.internal.TimeoutScheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StubTimeoutScheduler implements TimeoutScheduler {

    private final List<ScheduledEntry> entries = new ArrayList<>();

    @Override
    public Cancellable schedule(final long delayMillis, final Runnable action) {
        final ScheduledEntry entry = new ScheduledEntry(delayMillis, action);
        entries.add(entry);
        return () -> entries.remove(entry);
    }

    public void advanceBy(final long millis) {
        final List<ScheduledEntry> snapshot = new ArrayList<>(entries);
        for (final ScheduledEntry entry : snapshot) {
            entry.remainingMillis -= millis;
        }
        final Iterator<ScheduledEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            final ScheduledEntry entry = iterator.next();
            if (entry.remainingMillis <= 0) {
                iterator.remove();
                entry.action.run();
            }
        }
    }

    private static class ScheduledEntry {
        long remainingMillis;
        final Runnable action;

        ScheduledEntry(final long remainingMillis, final Runnable action) {
            this.remainingMillis = remainingMillis;
            this.action = action;
        }
    }
}
