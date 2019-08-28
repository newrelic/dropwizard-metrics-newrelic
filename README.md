# New Relic Dropwizard Reporter
A [Dropwizard metrics](https://metrics.dropwizard.io/4.0.0/) reporter for sending dimensional metrics to New Relic using the New Relic Java Telemetry SDK.

# how to use

## gradle

`build.gradle:`
```
compile("com.newrelic.telemetry:dropwizard-metrics-newrelic:0.1.1")
compile("com.newrelic.telemetry:telemetry-components:0.2.1")
```

or if you're using kotlin build gradle...

`build.gradle.kts:`
```
implementation("com.newrelic.telemetry:dropwizard-metrics-newrelic:0.1.1")
implementation("com.newrelic.telemetry:telemetry-components:0.2.1")
```

Note: to use the sample code below, you will need the `telemetry-components` library mentioned above. It provides
implementations for generating JSON and communicating via HTTP using the gson and okhttp libraries, respectively.
If you do not want to depend on those libraries, you can elide the dependency on `telemetry-components`, 
but you will need to construct a `MetricBatchSender` instance with your
own implementations of the `com.newrelic.telemetry.http.HttpPoster` and `com.newrelic.telemetry.MetricJsonGenerator` interfaces.

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