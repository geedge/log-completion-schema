package com.zdjizhi.common;


import com.zdjizhi.utils.system.FlowWriteConfigurations;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 * @author Administrator
 */
public class FlowWriteConfig {

    private static StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

    static {
        encryptor.setPassword("galaxy");
    }

    public static final int IF_PARAM_LENGTH = 3;
    /**
     * 有此标识的字段为失效字段，不计入最终日志字段
     */
    public static final String VISIBILITY = "disabled";
    /**
     * 默认的切分符号
     */
    public static final String FORMAT_SPLITTER = ",";
    /**
     * 标识字段为日志字段还是schema指定字段
     */
    public static final String IS_JSON_KEY_TAG = "$.";
    /**
     * if函数连接分隔符
     */
    public static final String IF_CONDITION_SPLITTER = "=";
    /**
     * 默认的字符串解析编码
     */
    public static final String ENCODING = "UTF8";

    public static final String GTPC_FAMILY_NAME = "gtp";
    public static final String RADIUS_FAMILY_NAME = "radius";


    /**
     * Nacos
     */
    public static final String NACOS_SERVER = FlowWriteConfigurations.getStringProperty(0, "nacos.server");
    public static final String NACOS_SCHEMA_NAMESPACE = FlowWriteConfigurations.getStringProperty(0, "nacos.schema.namespace");
    public static final String NACOS_DATA_ID = FlowWriteConfigurations.getStringProperty(0, "nacos.data.id");
    public static final String NACOS_GROUP = FlowWriteConfigurations.getStringProperty(1, "nacos.group");
    public static final String NACOS_USERNAME = encryptor.decrypt(FlowWriteConfigurations.getStringProperty(1, "nacos.username"));
    public static final String NACOS_PIN = encryptor.decrypt(FlowWriteConfigurations.getStringProperty(1, "nacos.pin"));

    /**
     * System config
     */
    public static final Integer SOURCE_PARALLELISM = FlowWriteConfigurations.getIntProperty(0, "source.parallelism");
    public static final Integer SINK_PARALLELISM = FlowWriteConfigurations.getIntProperty(0, "sink.parallelism");
    public static final Integer TRANSFORM_PARALLELISM = FlowWriteConfigurations.getIntProperty(0, "transform.parallelism");
    public static final Integer DATA_CENTER_ID_NUM = FlowWriteConfigurations.getIntProperty(0, "data.center.id.num");
    public static final Integer LOG_NEED_COMPLETE = FlowWriteConfigurations.getIntProperty(0, "log.need.complete");
    public static final String MAIL_DEFAULT_CHARSET = FlowWriteConfigurations.getStringProperty(1, "mail.default.charset");
    public static final Integer LOG_TRANSFORM_TYPE = FlowWriteConfigurations.getIntProperty(1, "log.transform.type");
    public static final Integer BUFFER_TIMEOUT = FlowWriteConfigurations.getIntProperty(1, "buffer.timeout");

    /**
     * HBase
     */
    public static final Integer HBASE_TICK_TUPLE_FREQ_SECS = FlowWriteConfigurations.getIntProperty(0, "hbase.tick.tuple.freq.secs");
    public static final Integer HBASE_GTPC_SCAN_MAX_ROWS = FlowWriteConfigurations.getIntProperty(1, "hbase.gtpc.scan.max.rows");
    public static final Integer HBASE_RADIUS_SCAN_MAX_ROWS = FlowWriteConfigurations.getIntProperty(1, "hbase.radius.scan.max.rows");
    public static final String HBASE_RADIUS_TABLE_NAME = FlowWriteConfigurations.getStringProperty(1, "hbase.radius.table.name");
    public static final String HBASE_GTPC_TABLE_NAME = FlowWriteConfigurations.getStringProperty(1, "hbase.gtpc.table.name");

    /**
     * kafka common
     */
    public static final String KAFKA_SASL_JAAS_USER = encryptor.decrypt(FlowWriteConfigurations.getStringProperty(1, "kafka.user"));
    public static final String KAFKA_SASL_JAAS_PIN = encryptor.decrypt(FlowWriteConfigurations.getStringProperty(1, "kafka.pin"));

    /**
     * kafka source config
     */
    public static final String SOURCE_KAFKA_TOPIC = FlowWriteConfigurations.getStringProperty(0, "source.kafka.topic");
    public static final String GROUP_ID = FlowWriteConfigurations.getStringProperty(0, "group.id");
    public static final String SESSION_TIMEOUT_MS = FlowWriteConfigurations.getStringProperty(1, "session.timeout.ms");
    public static final String MAX_POLL_RECORDS = FlowWriteConfigurations.getStringProperty(1, "max.poll.records");
    public static final String MAX_PARTITION_FETCH_BYTES = FlowWriteConfigurations.getStringProperty(1, "max.partition.fetch.bytes");

    /**
     * kafka sink config
     */
    public static final String SINK_KAFKA_TOPIC = FlowWriteConfigurations.getStringProperty(0, "sink.kafka.topic");
    public static final String PRODUCER_ACK = FlowWriteConfigurations.getStringProperty(1, "producer.ack");
    public static final String PRODUCER_KAFKA_COMPRESSION_TYPE = FlowWriteConfigurations.getStringProperty(0, "producer.kafka.compression.type");

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
     * common config
     */
    public static final String SOURCE_KAFKA_SERVERS = FlowWriteConfigurations.getStringProperty(0, "source.kafka.servers");
    public static final String SINK_KAFKA_SERVERS = FlowWriteConfigurations.getStringProperty(0, "sink.kafka.servers");
    public static final String ZOOKEEPER_SERVERS = FlowWriteConfigurations.getStringProperty(0, "zookeeper.servers");
    public static final String TOOLS_LIBRARY = FlowWriteConfigurations.getStringProperty(0, "tools.library");
    public static final String HBASE_ZOOKEEPER_SERVERS = FlowWriteConfigurations.getStringProperty(0, "hbase.zookeeper.servers");

}