package com.zdjizhi.common;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.tools.general.ConfigurationsUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Administrator
 */
public class FlowWriteConfig {
    private static final Log logger = LogFactory.get();

    private static StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    private static Properties propDefault = new Properties();
    private static Properties propService = new Properties();

    static {
        encryptor.setPassword("galaxy");
        try {
            propService.load(ConfigurationsUtils.class.getClassLoader().getResourceAsStream("service_flow_config.properties"));
            propDefault.load(ConfigurationsUtils.class.getClassLoader().getResourceAsStream("default_config.properties"));
        } catch (IOException | RuntimeException e) {
            logger.error("Loading the configuration file Failed!,The error message is:{}" + e);
        }
    }

    /**
     * 默认的文件系统标识
     */
    public static final String FILE_SYSTEM_TYPE = "hdfs";

    /**
     * 默认的切分符号
     */
    public static final String FORMAT_SPLITTER = ",";

    public static final String GTPC_FAMILY_NAME = "gtp";
    public static final String RADIUS_FAMILY_NAME = "radius";
    public static final String DEFAULT_RELATIONSHIP_MODULE = "vsys";


    /**
     * Nacos
     */
    public static final String NACOS_SERVER = ConfigurationsUtils.getStringProperty(propService, "nacos.server");
    public static final String NACOS_USERNAME = encryptor.decrypt(ConfigurationsUtils.getStringProperty(propDefault, "nacos.username"));
    public static final String NACOS_PIN = encryptor.decrypt(ConfigurationsUtils.getStringProperty(propDefault, "nacos.pin"));
    public static final Integer NACOS_CONNECTION_TIMEOUT = ConfigurationsUtils.getIntProperty(propDefault, "nacos.connection.timeout");

    public static final String NACOS_SCHEMA_NAMESPACE = ConfigurationsUtils.getStringProperty(propService, "nacos.schema.namespace");
    public static final String NACOS_SCHEMA_DATA_ID = ConfigurationsUtils.getStringProperty(propService, "nacos.schema.data.id");
    public static final String NACOS_SCHEMA_GROUP = ConfigurationsUtils.getStringProperty(propDefault, "nacos.schema.group");

    public static final String NACOS_PUBLIC_NAMESPACE = ConfigurationsUtils.getStringProperty(propDefault, "nacos.public.namespace");
    public static final String NACOS_PUBLIC_GROUP = ConfigurationsUtils.getStringProperty(propDefault, "nacos.public.group");
    public static final String NACOS_KNOWLEDGEBASE_DATA_ID = ConfigurationsUtils.getStringProperty(propDefault, "nacos.knowledgebase.data.id");

    /**
     * knowledgebase
     */
    public static final String KNOWLEDGEBASE_FILE_STORAGE_PATH = ConfigurationsUtils.getStringProperty(propService, "knowledgebase.file.storage.path");
    public static final String KNOWLEDGEBASE_FILE_STORAGE_TYPE = ConfigurationsUtils.getStringProperty(propService, "knowledgebase.file.storage.type");

    /**
     * System config
     */
    public static final Integer SOURCE_PARALLELISM = ConfigurationsUtils.getIntProperty(propService, "source.parallelism");
    public static final Integer SINK_PARALLELISM = ConfigurationsUtils.getIntProperty(propService, "sink.parallelism");
    public static final Integer TRANSFORM_PARALLELISM = ConfigurationsUtils.getIntProperty(propService, "transform.parallelism");
    public static final Integer DATA_CENTER_ID_NUM = ConfigurationsUtils.getIntProperty(propService, "data.center.id.num");
    public static final Integer LOG_NEED_COMPLETE = ConfigurationsUtils.getIntProperty(propService, "log.need.complete");
    public static final Integer LOG_TRANSFORM_TYPE = ConfigurationsUtils.getIntProperty(propDefault, "log.transform.type");
    public static final String DATA_RELATIONSHIP_MODEL = ConfigurationsUtils.getStringProperty(propDefault, "data.relationship.model");
    public static final Integer BUFFER_TIMEOUT = ConfigurationsUtils.getIntProperty(propDefault, "buffer.timeout");

    /**
     * HBase
     */
    public static final Integer HBASE_TICK_TUPLE_FREQ_SECS = ConfigurationsUtils.getIntProperty(propService, "hbase.tick.tuple.freq.secs");
    public static final Integer HBASE_GTPC_SCAN_MAX_ROWS = ConfigurationsUtils.getIntProperty(propDefault, "hbase.gtpc.scan.max.rows");
    public static final Integer HBASE_RADIUS_SCAN_MAX_ROWS = ConfigurationsUtils.getIntProperty(propDefault, "hbase.radius.scan.max.rows");
    public static final String HBASE_RADIUS_TABLE_NAME = ConfigurationsUtils.getStringProperty(propDefault, "hbase.radius.table.name");
    public static final String HBASE_GTPC_TABLE_NAME = ConfigurationsUtils.getStringProperty(propDefault, "hbase.gtpc.table.name");
    public static final String HBASE_RPC_TIMEOUT = ConfigurationsUtils.getStringProperty(propDefault, "hbase.rpc.timeout");

    /**
     * HDFS
     */
    public static final String HDFS_SERVERS = ConfigurationsUtils.getStringProperty(propService, "hdfs.servers");

    /**
     * HTTP
     */
    public static final Integer HTTP_POOL_MAX_CONNECTION = ConfigurationsUtils.getIntProperty(propDefault, "http.pool.max.connection");
    public static final Integer HTTP_POOL_MAX_PER_ROUTE = ConfigurationsUtils.getIntProperty(propDefault, "http.pool.max.per.route");
    public static final Integer HTTP_POOL_REQUEST_TIMEOUT = ConfigurationsUtils.getIntProperty(propDefault, "http.pool.request.timeout");
    public static final Integer HTTP_POOL_CONNECT_TIMEOUT = ConfigurationsUtils.getIntProperty(propDefault, "http.pool.connect.timeout");
    public static final Integer HTTP_SOCKET_TIMEOUT = ConfigurationsUtils.getIntProperty(propDefault, "http.socket.timeout");

    /**
     * kafka common
     */
    public static final String KAFKA_SASL_JAAS_USER = encryptor.decrypt(ConfigurationsUtils.getStringProperty(propDefault, "kafka.user"));
    public static final String KAFKA_SASL_JAAS_PIN = encryptor.decrypt(ConfigurationsUtils.getStringProperty(propDefault, "kafka.pin"));

    /**
     * kafka source config
     */
    public static final String SOURCE_KAFKA_TOPIC = ConfigurationsUtils.getStringProperty(propService, "source.kafka.topic");
    public static final String GROUP_ID = ConfigurationsUtils.getStringProperty(propService, "group.id");
    public static final String SESSION_TIMEOUT_MS = ConfigurationsUtils.getStringProperty(propDefault, "session.timeout.ms");
    public static final String MAX_POLL_RECORDS = ConfigurationsUtils.getStringProperty(propDefault, "max.poll.records");
    public static final String MAX_PARTITION_FETCH_BYTES = ConfigurationsUtils.getStringProperty(propDefault, "max.partition.fetch.bytes");

    /**
     * kafka sink config
     */
    public static final String SINK_KAFKA_TOPIC = ConfigurationsUtils.getStringProperty(propService, "sink.kafka.topic");
    public static final String PRODUCER_ACK = ConfigurationsUtils.getStringProperty(propDefault, "producer.ack");
    public static final String PRODUCER_KAFKA_COMPRESSION_TYPE = ConfigurationsUtils.getStringProperty(propService, "producer.kafka.compression.type");

    /**
     * connection kafka
     */
    public static final String RETRIES = ConfigurationsUtils.getStringProperty(propDefault, "retries");
    public static final String LINGER_MS = ConfigurationsUtils.getStringProperty(propDefault, "linger.ms");
    public static final Integer REQUEST_TIMEOUT_MS = ConfigurationsUtils.getIntProperty(propDefault, "request.timeout.ms");
    public static final Integer BATCH_SIZE = ConfigurationsUtils.getIntProperty(propDefault, "batch.size");
    public static final Integer BUFFER_MEMORY = ConfigurationsUtils.getIntProperty(propDefault, "buffer.memory");
    public static final Integer MAX_REQUEST_SIZE = ConfigurationsUtils.getIntProperty(propDefault, "max.request.size");

    /**
     * common config
     */
    public static final String SOURCE_KAFKA_SERVERS = ConfigurationsUtils.getStringProperty(propService, "source.kafka.servers");
    public static final String SINK_KAFKA_SERVERS = ConfigurationsUtils.getStringProperty(propService, "sink.kafka.servers");
    public static final String ZOOKEEPER_SERVERS = ConfigurationsUtils.getStringProperty(propService, "zookeeper.servers");
    public static final String TOOLS_LIBRARY = ConfigurationsUtils.getStringProperty(propService, "tools.library");

}