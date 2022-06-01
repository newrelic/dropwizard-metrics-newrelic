package com.codahale.metrics.newrelic.transformer.customizer;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.newrelic.telemetry.Attributes;

/**
 * {@code MetricAttributesCustomizer} is a {@link FunctionalInterface} intended to give a hook into
 * metric transformers to provide custom per-metric attributes that are based on data available at
 * runtime.
 *
 * <p>An example use may be an end user providing an implementation that extracts encoded data from
 * a Metric name and returns that encoded data as the contents of an {@code Attributes} object.
 */
@FunctionalInterface
public interface MetricAttributesCustomizer {
  MetricAttributesCustomizer DEFAULT = (name, metric, attributes) -> attributes;

  /**
   * Customizes metric attributes using any data available at runtime within the scope of the method
   * call. The default implementation returns the {@code baseAttributes} parameter unchanged.
   *
   * @param name the name that a metric is registered under in the effective {@link MetricRegistry}
   * @param metric incoming metric. This may be used to fetch data for attributes.
   * @param attributes incoming attributes. These may be returned, dropped, or merged with computed
   *     attributes.
   * @return an Attributes object to report to NewRelic with the transformed {@link Metric}
   */
  Attributes customizeMetricAttributes(String name, Metric metric, Attributes attributes);
}
