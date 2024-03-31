/*
 * Apus - A social wall for conferences with additional features.
 * Copyright (C) Marcus Fihlon and the individual contributors to Apus.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package swiss.fihlon.apus.service;

import jakarta.annotation.PreDestroy;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import swiss.fihlon.apus.configuration.Configuration;
import swiss.fihlon.apus.social.Message;
import swiss.fihlon.apus.social.mastodon.MastodonAPI;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;

@Service
public final class SocialService {

    private static final Duration UPDATE_FREQUENCY = Duration.ofMinutes(1);
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    private final ScheduledFuture<?> updateScheduler;
    private final MastodonAPI mastodonAPI;
    private final String hashtag;
    private final boolean filterReplies;
    private final boolean filterSensitive;
    private final List<String> filterWords;
    private List<Message> messages = List.of();

    public SocialService(@NotNull final TaskScheduler taskScheduler,
                         @NotNull final Configuration configuration) {
        mastodonAPI = new MastodonAPI(configuration.getMastodon().instance());
        hashtag = configuration.getMastodon().hashtag();
        filterReplies = configuration.getFilter().replies();
        filterSensitive = configuration.getFilter().sensitive();
        filterWords = configuration.getFilter().words().stream()
                .map(filterWord -> filterWord.toLowerCase(DEFAULT_LOCALE).trim())
                .toList();
        updateMessages();
        updateScheduler = taskScheduler.scheduleAtFixedRate(this::updateMessages, UPDATE_FREQUENCY);
    }

    @PreDestroy
    public void stopUpdateScheduler() {
        updateScheduler.cancel(true);
    }

    private void updateMessages() {
        final var newMessages = mastodonAPI.getMessages(hashtag).stream()
                .filter(message -> !filterSensitive || !message.isSensitive())
                .filter(message -> !filterReplies || !message.isReply())
                .filter(this::checkWordFilter)
                .toList();
        synchronized (this) {
            messages = newMessages;
        }
    }

    private boolean checkWordFilter(@NotNull final Message message) {
        final String messageText = Jsoup.parse(message.html()).text().toLowerCase(DEFAULT_LOCALE);
        for (final String filterWord : filterWords) {
            if (messageText.contains(filterWord)) {
                return false;
            }
        }
        return true;
    }

    public List<Message> getMessages(final int limit) {
        synchronized (this) {
            if (limit <= 0 || messages.isEmpty()) {
                return Collections.unmodifiableList(messages);
            }
            final int toIndex = limit < messages.size() ? limit : messages.size() - 1;
            return Collections.unmodifiableList(messages.subList(0, toIndex));
        }
    }

}
