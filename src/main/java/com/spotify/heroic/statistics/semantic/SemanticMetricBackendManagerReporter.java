package com.spotify.heroic.statistics.semantic;

import com.spotify.heroic.statistics.CallbackReporter;
import com.spotify.heroic.statistics.CallbackReporter.Context;
import com.spotify.heroic.statistics.MetricBackendManagerReporter;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;

public class SemanticMetricBackendManagerReporter implements
        MetricBackendManagerReporter {
    private static final String COMPONENT = "metric-backend-manager";

    private final CallbackReporter getAllRows;
    private final CallbackReporter queryMetrics;
    private final CallbackReporter streamMetrics;
    private final CallbackReporter streamMetricsChunk;
    private final CallbackReporter findRowGroups;
    private final CallbackReporter write;

    public SemanticMetricBackendManagerReporter(SemanticMetricRegistry registry) {
        final MetricId id = MetricId.build().tagged("component", COMPONENT);

        this.getAllRows = new SemanticCallbackReporter(registry, id.tagged(
                "what", "get-all-rows", "unit", Units.READS));
        this.queryMetrics = new SemanticCallbackReporter(registry, id.tagged(
                "what", "query-metrics", "unit", Units.READS));
        this.streamMetrics = new SemanticCallbackReporter(registry, id.tagged(
                "what", "stream-metrics", "unit", Units.READS));
        this.streamMetricsChunk = new SemanticCallbackReporter(registry,
                id.tagged("operation", "stream-metrics-chunk", "unit",
                        Units.READS));
        this.findRowGroups = new SemanticCallbackReporter(registry, id.tagged(
                "what", "find-row-groups", "unit", Units.LOOKUPS));
        this.write = new SemanticCallbackReporter(registry, id.tagged("what",
                "write", "unit", Units.WRITES));
    }

    @Override
    public CallbackReporter.Context reportGetAllRows() {
        return getAllRows.setup();
    }

    @Override
    public CallbackReporter.Context reportQueryMetrics() {
        return queryMetrics.setup();
    }

    @Override
    public CallbackReporter.Context reportStreamMetrics() {
        return streamMetrics.setup();
    }

    @Override
    public CallbackReporter.Context reportStreamMetricsChunk() {
        return streamMetricsChunk.setup();
    }

    @Override
    public Context reportFindTimeSeries() {
        return findRowGroups.setup();
    }

    @Override
    public Context reportWrite() {
        return write.setup();
    }
}