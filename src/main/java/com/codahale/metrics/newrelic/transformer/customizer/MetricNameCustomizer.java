package com.codahale.metrics.newrelic.transformer.customizer;

import com.codahale.metrics.MetricRegistry;

/**
 * {@code MetricNameCustomizer} is a {@link FunctionalInterface} intended to give a hook into metric
 * transformers to provide metric names different than those registered with a {@link
 * MetricRegistry}.
 *
 * <p>Example usage, removing encoded data from metric names before sending them to NewRelic:
 *
 * <pre>{@code
 * new MetricNameCustomizer(){
 *     String customizeMetricName(String name){
 *         char delimiter='[';
 *         return name.substring(0, name.indexOf(delimiter));
 *     }
 * }
 * }</pre>
 *
 * or as a lambda:
 *
 * <pre>{@code
 * MetricNameCustomizer stripEncodedDataCustomizer =
 *       name -> name.substring(0, name.indexOf('['));
 * }</pre>
 */
@FunctionalInterface
public interface MetricNameCustomizer {

  MetricNameCustomizer DEFAULT = n -> n;

  /**
   * Customizes metric name using any data available at runtime within the scope of the method call.
   * The default implementation returns the {@code name} parameter unchanged.
   *
   * @param name the name that a metric is registered under in the effective {@link MetricRegistry}
   * @return a customized name.
   */
  String customizeMetricName(String name);
}
