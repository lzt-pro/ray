package io.ray.streaming.api.context;

import com.google.common.base.Preconditions;
import io.ray.streaming.api.stream.StreamSink;
import io.ray.streaming.jobgraph.JobGraph;
import io.ray.streaming.jobgraph.JobGraphBuilder;
import io.ray.streaming.schedule.JobScheduler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encapsulate the context information of a streaming Job.
 */
public class StreamingContext implements Serializable {

  private transient AtomicInteger idGenerator;

  /**
   * The sinks of this streaming job.
   */
  private List<StreamSink> streamSinks;

  /**
   * The user custom streaming job configuration.
   */
  private Map<String, String> jobConfig;

  /**
   * The logic plan.
   */
  private JobGraph jobGraph;

  private StreamingContext() {
    this.idGenerator = new AtomicInteger(0);
    this.streamSinks = new ArrayList<>();
    this.jobConfig = new HashMap<>();
  }

  public static StreamingContext buildContext() {
    return new StreamingContext();
  }

  /**
   * Construct job DAG, and execute the job.
   */
  public void execute(String jobName) {
    JobGraphBuilder jobGraphBuilder = new JobGraphBuilder(this.streamSinks, jobName);
    this.jobGraph = jobGraphBuilder.build();
    jobGraph.printJobGraph();

    ServiceLoader<JobScheduler> serviceLoader = ServiceLoader.load(JobScheduler.class);
    Iterator<JobScheduler> iterator = serviceLoader.iterator();
    Preconditions.checkArgument(iterator.hasNext(),
        "No JobScheduler implementation has been provided.");
    JobScheduler jobSchedule = iterator.next();
    jobSchedule.schedule(jobGraph, jobConfig);
  }

  public int generateId() {
    return this.idGenerator.incrementAndGet();
  }

  public void addSink(StreamSink streamSink) {
    streamSinks.add(streamSink);
  }

  public List<StreamSink> getStreamSinks() {
    return streamSinks;
  }

  public void withConfig(Map<String, String> jobConfig) {
    this.jobConfig = jobConfig;
  }
}
