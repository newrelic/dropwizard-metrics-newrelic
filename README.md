# New Relic Dropwizard Reporter
A [Dropwizard metrics](https://metrics.dropwizard.io/4.0.0/) reporter for sending dimensional metrics to New Relic using the New Relic Java Telemetry SDK.

# how to use

## gradle

`build.gradle:`
```
compile("com.newrelic.telemetry:dropwizard-metrics-newrelic:0.1.0-SNAPSHOT")
```

or if you're using kotlin build gradle...

`build.gradle.kts:`
```
implementation("com.newrelic.telemetry:dropwizard-metrics-newrelic:0.1.0-SNAPSHOT")
```

## start the reporter

Early in the lifecycle of your application, you will want to create and
start a `NewRelicReporter`, similar to this:

```
MetricRegistry metricRegistry = new MetricRegistry(); // If you're already using dropwizard-metrics you may already have one of these.
...
String apiKey = "<YOUR_SECRET_API_KEY>";
MetricBatchSender metricBatchSender = SimpleMetricBatchSender.builder(apiKey).build();

Attributes commonAttributes = new Attributes()
            .put("host", InetAddress.getLocalHost().getHostName())
            .put("appName", "Your Application Name Here")
            .put("other", "any other common attributes you wish");
            
NewRelicReporter reporter = NewRelicReporter.build(metricRegistry, metricBatchSender)
        .commonAttributes(commonAttributes)
        .build();
        
reporter.start(15, TimeUnit.SECONDS);
```