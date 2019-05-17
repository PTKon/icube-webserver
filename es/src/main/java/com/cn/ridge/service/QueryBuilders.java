package com.cn.ridge.service;

/**
 * Author: create by wang.gf
 * Date: create at 2019/4/25
 */
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.indices.TermsLookup;
import org.elasticsearch.script.Script;

public final class QueryBuilders {
    private QueryBuilders() {
    }

    public static MatchAllQueryBuilder matchAllQuery() {
        return new MatchAllQueryBuilder();
    }

    public static MatchQueryBuilder matchQuery(String name, Object text) {
        return new MatchQueryBuilder(name, text);
    }

    public static CommonTermsQueryBuilder commonTermsQuery(String fieldName, Object text) {
        return new CommonTermsQueryBuilder(fieldName, text);
    }

    public static MultiMatchQueryBuilder multiMatchQuery(Object text, String... fieldNames) {
        return new MultiMatchQueryBuilder(text, fieldNames);
    }

    public static MatchPhraseQueryBuilder matchPhraseQuery(String name, Object text) {
        return new MatchPhraseQueryBuilder(name, text);
    }

    public static MatchPhrasePrefixQueryBuilder matchPhrasePrefixQuery(String name, Object text) {
        return new MatchPhrasePrefixQueryBuilder(name, text);
    }

    public static DisMaxQueryBuilder disMaxQuery() {
        return new DisMaxQueryBuilder();
    }

    public static IdsQueryBuilder idsQuery() {
        return new IdsQueryBuilder();
    }

    public static IdsQueryBuilder idsQuery(String... types) {
        return (new IdsQueryBuilder()).types(types);
    }

    public static TermQueryBuilder termQuery(String name, String value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, int value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, long value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, float value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, double value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, boolean value) {
        return new TermQueryBuilder(name, value);
    }

    public static TermQueryBuilder termQuery(String name, Object value) {
        return new TermQueryBuilder(name, value);
    }

    public static FuzzyQueryBuilder fuzzyQuery(String name, String value) {
        return new FuzzyQueryBuilder(name, value);
    }

    public static FuzzyQueryBuilder fuzzyQuery(String name, Object value) {
        return new FuzzyQueryBuilder(name, value);
    }

    public static PrefixQueryBuilder prefixQuery(String name, String prefix) {
        return new PrefixQueryBuilder(name, prefix);
    }

    public static RangeQueryBuilder rangeQuery(String name) {
        return new RangeQueryBuilder(name);
    }

    public static WildcardQueryBuilder wildcardQuery(String name, String query) {
        return new WildcardQueryBuilder(name, query);
    }

    public static RegexpQueryBuilder regexpQuery(String name, String regexp) {
        return new RegexpQueryBuilder(name, regexp);
    }

    public static QueryStringQueryBuilder queryStringQuery(String queryString) {
        return new QueryStringQueryBuilder(queryString);
    }

    public static SimpleQueryStringBuilder simpleQueryStringQuery(String queryString) {
        return new SimpleQueryStringBuilder(queryString);
    }

    public static BoostingQueryBuilder boostingQuery(QueryBuilder positiveQuery, QueryBuilder negativeQuery) {
        return new BoostingQueryBuilder(positiveQuery, negativeQuery);
    }

    public static BoolQueryBuilder boolQuery() {
        return new BoolQueryBuilder();
    }

    public static SpanTermQueryBuilder spanTermQuery(String name, String value) {
        return new SpanTermQueryBuilder(name, value);
    }

    public static SpanTermQueryBuilder spanTermQuery(String name, int value) {
        return new SpanTermQueryBuilder(name, value);
    }

    public static SpanTermQueryBuilder spanTermQuery(String name, long value) {
        return new SpanTermQueryBuilder(name, value);
    }

    public static SpanTermQueryBuilder spanTermQuery(String name, float value) {
        return new SpanTermQueryBuilder(name, value);
    }

    public static SpanTermQueryBuilder spanTermQuery(String name, double value) {
        return new SpanTermQueryBuilder(name, value);
    }

    public static SpanFirstQueryBuilder spanFirstQuery(SpanQueryBuilder match, int end) {
        return new SpanFirstQueryBuilder(match, end);
    }

    public static SpanNearQueryBuilder spanNearQuery(SpanQueryBuilder initialClause, int slop) {
        return new SpanNearQueryBuilder(initialClause, slop);
    }

    public static SpanNotQueryBuilder spanNotQuery(SpanQueryBuilder include, SpanQueryBuilder exclude) {
        return new SpanNotQueryBuilder(include, exclude);
    }

    public static SpanOrQueryBuilder spanOrQuery(SpanQueryBuilder initialClause) {
        return new SpanOrQueryBuilder(initialClause);
    }

    public static SpanWithinQueryBuilder spanWithinQuery(SpanQueryBuilder big, SpanQueryBuilder little) {
        return new SpanWithinQueryBuilder(big, little);
    }

    public static SpanContainingQueryBuilder spanContainingQuery(SpanQueryBuilder big, SpanQueryBuilder little) {
        return new SpanContainingQueryBuilder(big, little);
    }

    public static SpanMultiTermQueryBuilder spanMultiTermQueryBuilder(MultiTermQueryBuilder multiTermQueryBuilder) {
        return new SpanMultiTermQueryBuilder(multiTermQueryBuilder);
    }

    public static FieldMaskingSpanQueryBuilder fieldMaskingSpanQuery(SpanQueryBuilder query, String field) {
        return new FieldMaskingSpanQueryBuilder(query, field);
    }

    public static ConstantScoreQueryBuilder constantScoreQuery(QueryBuilder queryBuilder) {
        return new ConstantScoreQueryBuilder(queryBuilder);
    }

    public static FunctionScoreQueryBuilder functionScoreQuery(QueryBuilder queryBuilder) {
        return new FunctionScoreQueryBuilder(queryBuilder);
    }

    public static FunctionScoreQueryBuilder functionScoreQuery(QueryBuilder queryBuilder, FilterFunctionBuilder[] filterFunctionBuilders) {
        return new FunctionScoreQueryBuilder(queryBuilder, filterFunctionBuilders);
    }

    public static FunctionScoreQueryBuilder functionScoreQuery(FilterFunctionBuilder[] filterFunctionBuilders) {
        return new FunctionScoreQueryBuilder(filterFunctionBuilders);
    }

    public static FunctionScoreQueryBuilder functionScoreQuery(ScoreFunctionBuilder function) {
        return new FunctionScoreQueryBuilder(function);
    }

    public static FunctionScoreQueryBuilder functionScoreQuery(QueryBuilder queryBuilder, ScoreFunctionBuilder function) {
        return new FunctionScoreQueryBuilder(queryBuilder, function);
    }

    public static MoreLikeThisQueryBuilder moreLikeThisQuery(String[] fields, String[] likeTexts, Item[] likeItems) {
        return new MoreLikeThisQueryBuilder(fields, likeTexts, likeItems);
    }

    public static MoreLikeThisQueryBuilder moreLikeThisQuery(String[] likeTexts, Item[] likeItems) {
        return moreLikeThisQuery((String[])null, likeTexts, likeItems);
    }

    public static MoreLikeThisQueryBuilder moreLikeThisQuery(String[] likeTexts) {
        return moreLikeThisQuery((String[])null, likeTexts, (Item[])null);
    }

    public static MoreLikeThisQueryBuilder moreLikeThisQuery(Item[] likeItems) {
        return moreLikeThisQuery((String[])null, (String[])null, likeItems);
    }

    public static NestedQueryBuilder nestedQuery(String path, QueryBuilder query, ScoreMode scoreMode) {
        return new NestedQueryBuilder(path, query, scoreMode);
    }

    public static TermsQueryBuilder termsQuery(String name, String... values) {
        return new TermsQueryBuilder(name, values);
    }

    public static TermsQueryBuilder termsQuery(String name, int... values) {
        return new TermsQueryBuilder(name, values);
    }

    public static TermsQueryBuilder termsQuery(String name, long... values) {
        return new TermsQueryBuilder(name, values);
    }

    public static TermsQueryBuilder termsQuery(String name, float... values) {
        return new TermsQueryBuilder(name, values);
    }

    public static TermsQueryBuilder termsQuery(String name, double... values) {
        return new TermsQueryBuilder(name, values);
    }

    public static TermsQueryBuilder termsQuery(String name, Collection<?> values) {
        return new TermsQueryBuilder(name, values);
    }

    public static WrapperQueryBuilder wrapperQuery(String source) {
        return new WrapperQueryBuilder(source);
    }

    public static WrapperQueryBuilder wrapperQuery(BytesReference source) {
        return new WrapperQueryBuilder(source);
    }

    public static WrapperQueryBuilder wrapperQuery(byte[] source) {
        return new WrapperQueryBuilder(source);
    }

    public static TypeQueryBuilder typeQuery(String type) {
        return new TypeQueryBuilder(type);
    }

    public static TermsQueryBuilder termsLookupQuery(String name, TermsLookup termsLookup) {
        return new TermsQueryBuilder(name, termsLookup);
    }

    public static ScriptQueryBuilder scriptQuery(Script script) {
        return new ScriptQueryBuilder(script);
    }

    public static GeoDistanceQueryBuilder geoDistanceQuery(String name) {
        return new GeoDistanceQueryBuilder(name);
    }

    public static GeoBoundingBoxQueryBuilder geoBoundingBoxQuery(String name) {
        return new GeoBoundingBoxQueryBuilder(name);
    }

    public static GeoPolygonQueryBuilder geoPolygonQuery(String name, List<GeoPoint> points) {
        return new GeoPolygonQueryBuilder(name, points);
    }

    public static GeoShapeQueryBuilder geoShapeQuery(String name, ShapeBuilder shape) throws IOException {
        return new GeoShapeQueryBuilder(name, shape);
    }

    public static GeoShapeQueryBuilder geoShapeQuery(String name, String indexedShapeId, String indexedShapeType) {
        return new GeoShapeQueryBuilder(name, indexedShapeId, indexedShapeType);
    }

    public static GeoShapeQueryBuilder geoIntersectionQuery(String name, ShapeBuilder shape) throws IOException {
        GeoShapeQueryBuilder builder = geoShapeQuery(name, shape);
        builder.relation(ShapeRelation.INTERSECTS);
        return builder;
    }

    public static GeoShapeQueryBuilder geoIntersectionQuery(String name, String indexedShapeId, String indexedShapeType) {
        GeoShapeQueryBuilder builder = geoShapeQuery(name, indexedShapeId, indexedShapeType);
        builder.relation(ShapeRelation.INTERSECTS);
        return builder;
    }

    public static GeoShapeQueryBuilder geoWithinQuery(String name, ShapeBuilder shape) throws IOException {
        GeoShapeQueryBuilder builder = geoShapeQuery(name, shape);
        builder.relation(ShapeRelation.WITHIN);
        return builder;
    }

    public static GeoShapeQueryBuilder geoWithinQuery(String name, String indexedShapeId, String indexedShapeType) {
        GeoShapeQueryBuilder builder = geoShapeQuery(name, indexedShapeId, indexedShapeType);
        builder.relation(ShapeRelation.WITHIN);
        return builder;
    }

    public static GeoShapeQueryBuilder geoDisjointQuery(String name, ShapeBuilder shape) throws IOException {
        GeoShapeQueryBuilder builder = geoShapeQuery(name, shape);
        builder.relation(ShapeRelation.DISJOINT);
        return builder;
    }

    public static GeoShapeQueryBuilder geoDisjointQuery(String name, String indexedShapeId, String indexedShapeType) {
        GeoShapeQueryBuilder builder = geoShapeQuery(name, indexedShapeId, indexedShapeType);
        builder.relation(ShapeRelation.DISJOINT);
        return builder;
    }

    public static ExistsQueryBuilder existsQuery(String name) {
        return new ExistsQueryBuilder(name);
    }
}
