package com.zdjizhi.common;


import com.zdjizhi.utils.system.FlowWriteConfigurations;

/**
 * @author Administrator
 */
public class FlowWriteConfig {

    public static final int IF_PARAM_LENGTH = 3;
    public static final String VISIBILITY = "disabled";
    public static final String FORMAT_SPLITTER = ",";
    public static final String IS_JSON_KEY_TAG = "$.";
    public static final String IF_CONDITION_SPLITTER = "=";
    public static final String MODEL = "remote";
    public static final String PROTOCOL_SPLITTER = "\\.";

    /**
     * System config
     */
    public static final Integer SOURCE_PARALLELISM = FlowWriteConfigurations.getIntProperty(0, "source.parallelism");
    public static final Integer SINK_PARALLELISM = FlowWriteConfigurations.getIntProperty(0, "sink.parallelism");
    public static final Integer TRANSFORM_PARALLELISM = FlowWriteConfigurations.getIntProperty(0, "transform.parallelism");
    public static final Integer HBASE_TICK_TUPLE_FREQ_SECS = FlowWriteConfigurations.getIntProperty(0, "hbase.tick.tuple.freq.secs");
    public static final Integer APP_TICK_TUPLE_FREQ_SECS = FlowWriteConfigurations.getIntProperty(0, "app.tick.tuple.freq.secs");
    public static final Integer DATA_CENTER_ID_NUM = FlowWriteConfigurations.getIntProperty(0, "data.center.id.num");
    public static final Integer LOG_NEED_COMPLETE = FlowWriteConfigurations.getIntProperty(0, "log.need.complete");
    public static final String MAIL_DEFAULT_CHARSET = FlowWriteConfigurations.getStringProperty(0, "mail.default.charset");
    public static final String HBASE_TABLE_NAME = FlowWriteConfigurations.getStringProperty(1, "hbase.table.name");
    public static final Integer LOG_TRANSFORM_TYPE = FlowWriteConfigurations.getIntProperty(1, "log.transform.type");

    /**
     * kafka source config
     */
    public static final String SESSION_TIMEOUT_MS = FlowWriteConfigurations.getStringProperty(1, "session.timeout.ms");
    public static final String MAX_POLL_RECORDS = FlowWriteConfigurations.getStringProperty(1, "max.poll.records");
    public static final String MAX_PARTITION_FETCH_BYTES = FlowWriteConfigurations.getStringProperty(1, "max.partition.fetch.bytes");


    /**
     * kafka sink config
     */
    public static final String SOURCE_KAFKA_SERVERS = FlowWriteConfigurations.getStringProperty(0, "source.kafka.servers");
    public static final String SINK_KAFKA_SERVERS = FlowWriteConfigurations.getStringProperty(0, "sink.kafka.servers");
    public static final String ZOOKEEPER_SERVERS = FlowWriteConfigurations.getStringProperty(0, "zookeeper.servers");
    public static final String HBASE_ZOOKEEPER_SERVERS = FlowWriteConfigurations.getStringProperty(0, "hbase.zookeeper.servers");
    public static final String GROUP_ID = FlowWriteConfigurations.getStringProperty(0, "group.id");
    public static final String SINK_KAFKA_TOPIC = FlowWriteConfigurations.getStringProperty(0, "sink.kafka.topic");
    public static final String SOURCE_KAFKA_TOPIC = FlowWriteConfigurations.getStringProperty(0, "source.kafka.topic");
    public static final String PRODUCER_ACK = FlowWriteConfigurations.getStringProperty(0, "producer.ack");
    public static final String TOOLS_LIBRARY = FlowWriteConfigurations.getStringProperty(0, "tools.library");
    public static final String PRODUCER_KAFKA_COMPRESSION_TYPE = FlowWriteConfigurations.getStringProperty(0, "producer.kafka.compression.type");
    public static final String KAFKA_SOURCE_PROTOCOL = FlowWriteConfigurations.getStringProperty(1, "kafka.source.protocol");
    public static final String KAFKA_SINK_PROTOCOL = FlowWriteConfigurations.getStringProperty(1, "kafka.sink.protocol");
    public static final String KAFKA_USER = FlowWriteConfigurations.getStringProperty(1, "kafka.user");
    public static final String KAFKA_PIN = FlowWriteConfigurations.getStringProperty(1, "kafka.pin");

    /**
     * connection kafka
     */
    public static final String RETRIES = FlowWriteConfigurations.getStringProperty(1, "retries");
    public static final String LINGER_MS = FlowWriteConfigurations.getStringProperty(1, "linger.ms");
    public static final Integer REQUEST_TIMEOUT_MS = FlowWriteConfigurations.getIntProperty(1, "request.timeout.ms");
    public static final Integer BATCH_SIZE = FlowWriteConfigurations.getIntProperty(1, "batch.size");
    public static final Integer BUFFER_MEMORY = FlowWriteConfigurations.getIntProperty(1, "buffer.memory");
    public static final Integer MAX_REQUEST_SIZE = FlowWriteConfigurations.getIntProperty(1, "max.request.size");

    /**
     * http
     */
    public static final String SCHEMA_HTTP = FlowWriteConfigurations.getStringProperty(0, "schema.http");
    public static final String APP_ID_HTTP = FlowWriteConfigurations.getStringProperty(0, "app.id.http");


}