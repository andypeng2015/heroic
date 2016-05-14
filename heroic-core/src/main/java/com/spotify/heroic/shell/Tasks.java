/*
 * Copyright (c) 2015 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.heroic.shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.spotify.heroic.HeroicCoreInstance;
import com.spotify.heroic.common.DateRange;
import com.spotify.heroic.common.OptionalLimit;
import com.spotify.heroic.common.RangeFilter;
import com.spotify.heroic.common.Series;
import com.spotify.heroic.dagger.CoreComponent;
import com.spotify.heroic.filter.Filter;
import com.spotify.heroic.filter.FilterFactory;
import com.spotify.heroic.grammar.QueryParser;
import com.spotify.heroic.metric.BackendKeyFilter;
import com.spotify.heroic.shell.task.AnalyticsDumpFetchSeries;
import com.spotify.heroic.shell.task.AnalyticsReportFetchSeries;
import com.spotify.heroic.shell.task.BackendKeyArgument;
import com.spotify.heroic.shell.task.Configure;
import com.spotify.heroic.shell.task.CountData;
import com.spotify.heroic.shell.task.DataMigrate;
import com.spotify.heroic.shell.task.DeleteKeys;
import com.spotify.heroic.shell.task.DeserializeKey;
import com.spotify.heroic.shell.task.Fetch;
import com.spotify.heroic.shell.task.IngestionFilter;
import com.spotify.heroic.shell.task.Keys;
import com.spotify.heroic.shell.task.ListBackends;
import com.spotify.heroic.shell.task.LoadGenerated;
import com.spotify.heroic.shell.task.MetadataCount;
import com.spotify.heroic.shell.task.MetadataDelete;
import com.spotify.heroic.shell.task.MetadataEntries;
import com.spotify.heroic.shell.task.MetadataFetch;
import com.spotify.heroic.shell.task.MetadataFindSeries;
import com.spotify.heroic.shell.task.MetadataLoad;
import com.spotify.heroic.shell.task.MetadataMigrate;
import com.spotify.heroic.shell.task.MetadataTags;
import com.spotify.heroic.shell.task.ParseQuery;
import com.spotify.heroic.shell.task.Pause;
import com.spotify.heroic.shell.task.Query;
import com.spotify.heroic.shell.task.ReadWriteTest;
import com.spotify.heroic.shell.task.Refresh;
import com.spotify.heroic.shell.task.Resume;
import com.spotify.heroic.shell.task.SerializeKey;
import com.spotify.heroic.shell.task.Statistics;
import com.spotify.heroic.shell.task.SuggestKey;
import com.spotify.heroic.shell.task.SuggestPerformance;
import com.spotify.heroic.shell.task.SuggestTag;
import com.spotify.heroic.shell.task.SuggestTagKeyCount;
import com.spotify.heroic.shell.task.SuggestTagValue;
import com.spotify.heroic.shell.task.SuggestTagValues;
import com.spotify.heroic.shell.task.Write;
import com.spotify.heroic.shell.task.WritePerformance;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.DateTimeParserBucket;
import org.kohsuke.args4j.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Tasks {
    static final List<ShellTaskDefinition> available = new ArrayList<>();
    static final Map<Class<?>, ShellTaskDefinition> availableMap = new HashMap<>();

    static {
        shellTask(Refresh::setup, Refresh.class);
        shellTask(Configure::setup, Configure.class);
        shellTask(Statistics::setup, Statistics.class);
        shellTask(Keys::setup, Keys.class);
        shellTask(DeleteKeys::setup, DeleteKeys.class);
        shellTask(CountData::setup, CountData.class);
        shellTask(SerializeKey::setup, SerializeKey.class);
        shellTask(DeserializeKey::setup, DeserializeKey.class);
        shellTask(ListBackends::setup, ListBackends.class);
        shellTask(Fetch::setup, Fetch.class);
        shellTask(Write::setup, Write.class);
        shellTask(WritePerformance::setup, WritePerformance.class);
        shellTask(MetadataDelete::setup, MetadataDelete.class);
        shellTask(MetadataFetch::setup, MetadataFetch.class);
        shellTask(MetadataTags::setup, MetadataTags.class);
        shellTask(MetadataCount::setup, MetadataCount.class);
        shellTask(MetadataEntries::setup, MetadataEntries.class);
        shellTask(MetadataFindSeries::setup, MetadataFindSeries.class);
        shellTask(MetadataMigrate::setup, MetadataMigrate.class);
        shellTask(MetadataLoad::setup, MetadataLoad.class);
        shellTask(SuggestTag::setup, SuggestTag.class);
        shellTask(SuggestKey::setup, SuggestKey.class);
        shellTask(SuggestTagValue::setup, SuggestTagValue.class);
        shellTask(SuggestTagValues::setup, SuggestTagValues.class);
        shellTask(SuggestTagKeyCount::setup, SuggestTagKeyCount.class);
        shellTask(SuggestPerformance::setup, SuggestPerformance.class);
        shellTask(Query::setup, Query.class);
        shellTask(ReadWriteTest::setup, ReadWriteTest.class);
        shellTask(Pause::setup, Pause.class);
        shellTask(Resume::setup, Resume.class);
        shellTask(IngestionFilter::setup, IngestionFilter.class);
        shellTask(DataMigrate::setup, DataMigrate.class);
        shellTask(ParseQuery::setup, ParseQuery.class);
        shellTask(AnalyticsReportFetchSeries::setup, AnalyticsReportFetchSeries.class);
        shellTask(AnalyticsDumpFetchSeries::setup, AnalyticsDumpFetchSeries.class);
        shellTask(LoadGenerated::setup, LoadGenerated.class);
    }

    public static List<ShellTaskDefinition> available() {
        return available;
    }

    public static Map<Class<?>, ShellTaskDefinition> availableMap() {
        return availableMap;
    }

    static <T extends ShellTask> ShellTaskDefinition shellTask(
        final Function<CoreComponent, T> task, Class<T> type
    ) {
        final String usage = taskUsage(type);

        final String name = name(type);
        final List<String> names = allNames(type);
        final List<String> aliases = aliases(type);

        final ShellTaskDefinition d = new ShellTaskDefinition() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public List<String> names() {
                return names;
            }

            @Override
            public List<String> aliases() {
                return aliases;
            }

            @Override
            public String usage() {
                return usage;
            }

            @Override
            public ShellTask setup(final HeroicCoreInstance core) throws Exception {
                return core.inject(task);
            }
        };

        available.add(d);
        availableMap.put(type, d);
        return d;
    }

    public static String taskUsage(final Class<? extends ShellTask> task) {
        final TaskUsage u = task.getAnnotation(TaskUsage.class);

        if (u != null) {
            return u.value();
        }

        return String.format("<no @ShellTaskUsage annotation for %s>", task.getCanonicalName());
    }

    public static String name(final Class<? extends ShellTask> task) {
        final TaskName n = task.getAnnotation(TaskName.class);

        if (n != null) {
            return n.value();
        }

        throw new IllegalStateException(
            String.format("No name configured with @TaskName on %s", task.getCanonicalName()));
    }

    public static List<String> allNames(final Class<? extends ShellTask> task) {
        final TaskName n = task.getAnnotation(TaskName.class);
        final List<String> names = new ArrayList<>();

        if (n != null) {
            names.add(n.value());

            for (final String alias : n.aliases()) {
                names.add(alias);
            }
        }

        if (names.isEmpty()) {
            throw new IllegalStateException(
                String.format("No name configured with @TaskName on %s", task.getCanonicalName()));
        }

        return names;
    }

    public static List<String> aliases(final Class<? extends ShellTask> task) {
        final TaskName n = task.getAnnotation(TaskName.class);
        final List<String> names = new ArrayList<>();

        if (n != null) {
            for (final String alias : n.aliases()) {
                names.add(alias);
            }
        }

        return names;
    }

    public static Filter setupFilter(
        FilterFactory filters, QueryParser parser, TaskQueryParameters params
    ) {
        final List<String> query = params.getQuery();

        if (query.isEmpty()) {
            return filters.t();
        }

        return parser.parseFilter(StringUtils.join(query, " "));
    }

    public static BackendKeyFilter setupKeyFilter(KeyspaceBase params, ObjectMapper mapper)
        throws Exception {
        BackendKeyFilter filter = BackendKeyFilter.of();

        if (params.start != null) {
            filter = filter.withStart(BackendKeyFilter.gte(
                mapper.readValue(params.start, BackendKeyArgument.class).toBackendKey()));
        }

        if (params.startPercentage >= 0) {
            filter = filter.withStart(
                BackendKeyFilter.gtePercentage((float) params.startPercentage / 100f));
        }

        if (params.startToken != null) {
            filter = filter.withStart(BackendKeyFilter.gteToken(params.startToken));
        }

        if (params.end != null) {
            filter = filter.withEnd(BackendKeyFilter.lt(
                mapper.readValue(params.end, BackendKeyArgument.class).toBackendKey()));
        }

        if (params.endPercentage >= 0) {
            filter =
                filter.withEnd(BackendKeyFilter.ltPercentage((float) params.endPercentage / 100f));
        }

        if (params.endToken != null) {
            filter = filter.withEnd(BackendKeyFilter.ltToken(params.endToken));
        }

        filter = filter.withLimit(params.limit);
        return filter;
    }

    public static Series parseSeries(final ObjectMapper mapper, final Optional<String> series) {
        return series.<Series>map(s -> {
            try {
                return mapper.readValue(s, Series.class);
            } catch (IOException e) {
                throw new RuntimeException("Bad series: " + s, e);
            }
        }).orElseGet(Series::empty);
    }

    public static <T> Stream<T> parseJsonLines(
        final ObjectMapper mapper, final Optional<Path> file, final ShellIO io, final Class<T> type
    ) {
        return file.<Stream<T>>map(f -> {
            try {
                final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(io.newInputStream(f), Charsets.UTF_8));

                return reader.lines().onClose(() -> {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to close file", e);
                    }
                }).map(line -> {
                    try {
                        return mapper.readValue(line.trim(), type);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to decode line (" + line.trim() + ")",
                            e);
                    }
                });
            } catch (final IOException e) {
                throw new RuntimeException("Failed to open file: " + f, e);
            }
        }).orElseGet(Stream::empty);
    }

    public abstract static class QueryParamsBase extends AbstractShellTaskParams
        implements TaskQueryParameters {
        private final DateRange defaultDateRange;

        public QueryParamsBase() {
            final long now = System.currentTimeMillis();
            final long start = now - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
            this.defaultDateRange = new DateRange(start, now);
        }

        @Override
        public DateRange getRange() {
            return defaultDateRange;
        }
    }

    public abstract static class KeyspaceBase extends QueryParamsBase {
        @Option(name = "--start", usage = "First key to operate on", metaVar = "<json>")
        protected String start;

        @Option(name = "--end", usage = "Last key to operate on (exclusive)", metaVar = "<json>")
        protected String end;

        @Option(name = "--start-percentage", usage = "First key to operate on in percentage",
            metaVar = "<int>")
        protected int startPercentage = -1;

        @Option(name = "--end-percentage",
            usage = "Last key to operate on (exclusive) in percentage", metaVar = "<int>")
        protected int endPercentage = -1;

        @Option(name = "--start-token", usage = "First token to operate on", metaVar = "<long>")
        protected Long startToken = null;

        @Option(name = "--end-token", usage = "Last token to operate on (exclusive)",
            metaVar = "<int>")
        protected Long endToken = null;

        @Option(name = "--limit", usage = "Limit the number keys to operate on", metaVar = "<int>")
        @Getter
        protected OptionalLimit limit = OptionalLimit.empty();
    }

    public static RangeFilter setupRangeFilter(
        FilterFactory filters, QueryParser parser, TaskQueryParameters params
    ) {
        final Filter filter = setupFilter(filters, parser, params);
        return new RangeFilter(filter, params.getRange(), params.getLimit());
    }

    private static final List<DateTimeParser> today = new ArrayList<>();
    private static final List<DateTimeParser> full = new ArrayList<>();

    static {
        today.add(DateTimeFormat.forPattern("HH:mm").getParser());
        today.add(DateTimeFormat.forPattern("HH:mm:ss").getParser());
        today.add(DateTimeFormat.forPattern("HH:mm:ss.SSS").getParser());
        full.add(DateTimeFormat.forPattern("yyyy-MM-dd/HH:mm").getParser());
        full.add(DateTimeFormat.forPattern("yyyy-MM-dd/HH:mm:ss").getParser());
        full.add(DateTimeFormat.forPattern("yyyy-MM-dd/HH:mm:ss.SSS").getParser());
    }

    public static long parseInstant(String input, long now) {
        if (input.charAt(0) == '+') {
            return now + Long.parseLong(input.substring(1));
        }

        if (input.charAt(0) == '-') {
            return now - Long.parseLong(input.substring(1));
        }

        // try to parse just milliseconds
        try {
            return Long.parseLong(input);
        } catch (IllegalArgumentException e) {
            // pass-through
        }

        final Chronology chrono = ISOChronology.getInstanceUTC();

        if (input.indexOf('/') >= 0) {
            return parseFullInstant(input, chrono);
        }

        return parseTodayInstant(input, chrono, now);
    }

    private static long parseTodayInstant(String input, final Chronology chrono, long now) {
        final DateTime n = new DateTime(now, chrono);

        for (final DateTimeParser p : today) {
            final DateTimeParserBucket bucket =
                new DateTimeParserBucket(0, chrono, null, null, 2000);

            bucket.saveField(chrono.year(), n.getYear());
            bucket.saveField(chrono.monthOfYear(), n.getMonthOfYear());
            bucket.saveField(chrono.dayOfYear(), n.getDayOfYear());

            try {
                p.parseInto(bucket, input, 0);
            } catch (IllegalArgumentException e) {
                // pass-through
                continue;
            }

            return bucket.computeMillis();
        }

        throw new IllegalArgumentException(input + " is not a valid instant");
    }

    private static long parseFullInstant(String input, final Chronology chrono) {
        for (final DateTimeParser p : full) {
            final DateTimeParserBucket bucket =
                new DateTimeParserBucket(0, chrono, null, null, 2000);

            try {
                p.parseInto(bucket, input, 0);
            } catch (IllegalArgumentException e) {
                // pass-through
                continue;
            }

            return bucket.computeMillis();
        }

        throw new IllegalArgumentException(input + " is not a valid instant");
    }

    public static String formatTimeNanos(long diff) {
        if (diff < 1000) {
            return String.format("%d ns", diff);
        }

        if (diff < 1000000) {
            final double v = ((double) diff) / 1000;
            return String.format("%.3f us", v);
        }

        if (diff < 1000000000) {
            final double v = ((double) diff) / 1000000;
            return String.format("%.3f ms", v);
        }

        final double v = ((double) diff) / 1000000000;
        return String.format("%.3f s", v);
    }
}
