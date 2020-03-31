package com.codahale.metrics.newrelic;

import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import com.newrelic.telemetry.metrics.MetricBatchSenderBuilder;
import io.dropwizard.metrics.BaseReporterFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.EnumSet;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonTypeName("newrelic")
public class NewRelicReporterFactory extends BaseReporterFactory {
  @JsonProperty private String apiKey = null;

  @JsonProperty private String overrideUri = null;

  @JsonProperty private Map<String, Object> commonAttributes = null;

  @Valid @NotNull @JsonProperty
  private EnumSet<MetricAttribute> disabledMetricAttributes = EnumSet.noneOf(MetricAttribute.class);

  @Override
  @NotNull
  public ScheduledReporter build(final MetricRegistry registry) {
    final MetricBatchSenderBuilder metricBatchSender = SimpleMetricBatchSender.builder(apiKey);
    handleUriOverride(metricBatchSender, overrideUri);
    Attributes attributes = buildAttributes();
    return NewRelicReporterBuilder.forRegistry(registry, metricBatchSender.build())
        .durationUnit(getDurationUnit())
        .rateUnit(getRateUnit())
        .filter(getFilter())
        .commonAttributes(attributes)
        .disabledMetricAttributes(disabledMetricAttributes)
        .build();
  }

  private Attributes buildAttributes() {
    final Attributes attributes = new Attributes();
    if (commonAttributes != null) {
      for (Map.Entry<String, Object> entry : commonAttributes.entrySet()) {
        if (entry.getValue() instanceof String)
          attributes.put(entry.getKey(), (String) entry.getValue());
        else if (entry.getValue() instanceof Number)
          attributes.put(entry.getKey(), (Number) entry.getValue());
        else if (entry.getValue() instanceof Boolean)
          attributes.put(entry.getKey(), (Boolean) entry.getValue());
      }
    }
    return attributes;
  }

  private void handleUriOverride(MetricBatchSenderBuilder metricBatchSender, String overrideUri) {
    if (overrideUri != null) {
      URI uri = URI.create(overrideUri);
      try {
        metricBatchSender.uriOverride(uri);
      } catch (MalformedURLException t) {
        throw new IllegalArgumentException(t.getMessage(), t);
      }
    }
  }
}
