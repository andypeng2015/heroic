package com.spotify.heroic.cluster;

import java.util.List;

import com.spotify.heroic.aggregation.Aggregation;
import com.spotify.heroic.common.DateRange;
import com.spotify.heroic.common.RangeFilter;
import com.spotify.heroic.common.Series;
import com.spotify.heroic.filter.Filter;
import com.spotify.heroic.metadata.CountSeries;
import com.spotify.heroic.metadata.DeleteSeries;
import com.spotify.heroic.metadata.FindKeys;
import com.spotify.heroic.metadata.FindSeries;
import com.spotify.heroic.metadata.FindTags;
import com.spotify.heroic.metric.MetricType;
import com.spotify.heroic.metric.QueryOptions;
import com.spotify.heroic.metric.QueryTrace;
import com.spotify.heroic.metric.ResultGroups;
import com.spotify.heroic.metric.WriteMetric;
import com.spotify.heroic.metric.WriteResult;
import com.spotify.heroic.suggest.KeySuggest;
import com.spotify.heroic.suggest.MatchOptions;
import com.spotify.heroic.suggest.TagKeyCount;
import com.spotify.heroic.suggest.TagSuggest;
import com.spotify.heroic.suggest.TagValueSuggest;
import com.spotify.heroic.suggest.TagValuesSuggest;

import eu.toolchain.async.AsyncFuture;

public class TracingClusterNodeGroup implements ClusterNode.Group {
    private final ClusterNode.Group delegate;
    private final QueryTrace.Identifier query;

    public TracingClusterNodeGroup(final Class<?> where, final ClusterNode.Group delegate) {
        this(where.getCanonicalName(), delegate);
    }

    public TracingClusterNodeGroup(final String identifier, final ClusterNode.Group delegate) {
        this.delegate = delegate;
        this.query = QueryTrace.identifier(identifier + "#query");
    }

    @Override
    public ClusterNode node() {
        return delegate.node();
    }

    @Override
    public AsyncFuture<ResultGroups> query(MetricType source, Filter filter, DateRange range, Aggregation aggregation,
            QueryOptions options) {
        return delegate.query(source, filter, range, aggregation, options).directTransform(ResultGroups.trace(query));
    }

    @Override
    public AsyncFuture<FindTags> findTags(RangeFilter filter) {
        return delegate.findTags(filter);
    }

    @Override
    public AsyncFuture<FindKeys> findKeys(RangeFilter filter) {
        return delegate.findKeys(filter);
    }

    @Override
    public AsyncFuture<FindSeries> findSeries(RangeFilter filter) {
        return delegate.findSeries(filter);
    }

    @Override
    public AsyncFuture<DeleteSeries> deleteSeries(RangeFilter filter) {
        return delegate.deleteSeries(filter);
    }

    @Override
    public AsyncFuture<CountSeries> countSeries(RangeFilter filter) {
        return delegate.countSeries(filter);
    }

    @Override
    public AsyncFuture<TagKeyCount> tagKeyCount(RangeFilter filter) {
        return delegate.tagKeyCount(filter);
    }

    @Override
    public AsyncFuture<TagSuggest> tagSuggest(RangeFilter filter, MatchOptions options, String key, String value) {
        return delegate.tagSuggest(filter, options, key, value);
    }

    @Override
    public AsyncFuture<KeySuggest> keySuggest(RangeFilter filter, MatchOptions options, String key) {
        return delegate.keySuggest(filter, options, key);
    }

    @Override
    public AsyncFuture<TagValuesSuggest> tagValuesSuggest(RangeFilter filter, List<String> exclude, int groupLimit) {
        return delegate.tagValuesSuggest(filter, exclude, groupLimit);
    }

    @Override
    public AsyncFuture<TagValueSuggest> tagValueSuggest(RangeFilter filter, String key) {
        return delegate.tagValueSuggest(filter, key);
    }

    @Override
    public AsyncFuture<WriteResult> writeSeries(DateRange range, Series series) {
        return delegate.writeSeries(range, series);
    }

    @Override
    public AsyncFuture<WriteResult> writeMetric(WriteMetric write) {
        return delegate.writeMetric(write);
    }
}