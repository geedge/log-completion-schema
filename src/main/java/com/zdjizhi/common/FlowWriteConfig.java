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
     * System
     */
    public static final Integer CONSUMER_PARALLELISM = FlowWriteConfigurations.getIntProperty(0, "consumer.parallelism");
    public static final Integer MAP_PARALLELISM = FlowWriteConfigurations.getIntProperty(0, "map.parallelism");
    public static final Integer PRODUCER_PARALLELISM = FlowWriteConfigurations.getIntProperty(0, "producer.parallelism");
    public static final Integer HBASE_TICK_TUPLE_FREQ_SECS = FlowWriteConfigurations.getIntProperty(0, "hbase.tick.tuple.freq.secs");
    public static final Integer APP_TICK_TUPLE_FREQ_SECS = FlowWriteConfigurations.getIntProperty(0, "app.tick.tuple.freq.secs");
    public static final Integer DATA_CENTER_ID_NUM = FlowWriteConfigurations.getIntProperty(0, "data.center.id.num");
    public static final Integer LOG_NEED_COMPLETE = FlowWriteConfigurations.getIntProperty(0, "log.need.complete");
    public static final String MAIL_DEFAULT_CHARSET = FlowWriteConfigurations.getStringProperty(0, "mail.default.charset");



    /**
     * kafka
     */
    public static final String INPUT_KAFKA_SERVERS = FlowWriteConfigurations.getStringProperty(0, "input.kafka.servers");
    public static final String OUTPUT_KAFKA_SERVERS = FlowWriteConfigurations.getStringProperty(0, "output.kafka.servers");
    public static final String ZOOKEEPER_SERVERS = FlowWriteConfigurations.getStringProperty(0, "zookeeper.servers");
    public static final String HBASE_ZOOKEEPER_SERVERS = FlowWriteConfigurations.getStringProperty(0, "hbase.zookeeper.servers");
    public static final String GROUP_ID = FlowWriteConfigurations.getStringProperty(0, "group.id");
    public static final String OUTPUT_KAFKA_TOPIC = FlowWriteConfigurations.getStringProperty(0, "output.kafka.topic");
    public static final String INPUT_KAFKA_TOPIC = FlowWriteConfigurations.getStringProperty(0, "input.kafka.topic");
    public static final String PRODUCER_ACK = FlowWriteConfigurations.getStringProperty(0, "producer.ack");
    public static final String IP_LIBRARY = FlowWriteConfigurations.getStringProperty(0, "ip.library");

    /**
     * kafka限流配置-20201117
     */
    public static final String PRODUCER_KAFKA_COMPRESSION_TYPE = FlowWriteConfigurations.getStringProperty(0, "producer.kafka.compression.type");
    public static final String CONSUMER_CLIENT_ID = FlowWriteConfigurations.getStringProperty(0, "consumer.client.id");
    public static final String PRODUCER_CLIENT_ID = FlowWriteConfigurations.getStringProperty(0, "producer.client.id");

    /**
     * http
     */
    public static final String SCHEMA_HTTP = FlowWriteConfigurations.getStringProperty(0, "schema.http");
    public static final String APP_ID_HTTP = FlowWriteConfigurations.getStringProperty(0, "app.id.http");


}