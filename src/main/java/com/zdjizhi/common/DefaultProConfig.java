package com.zdjizhi.common;


import com.zdjizhi.utils.system.FlowWriteConfigurations;

/**
 * @author Administrator
 */
public class DefaultProConfig {


    public static final String RETRIES = FlowWriteConfigurations.getStringProperty(1, "retries");
    public static final String LINGER_MS = FlowWriteConfigurations.getStringProperty(1, "linger.ms");
    public static final Integer REQUEST_TIMEOUT_MS = FlowWriteConfigurations.getIntProperty(1, "request.timeout.ms");
    public static final Integer BATCH_SIZE = FlowWriteConfigurations.getIntProperty(1, "batch.size");
    public static final Integer BUFFER_MEMORY = FlowWriteConfigurations.getIntProperty(1, "buffer.memory");
    public static final Integer MAX_REQUEST_SIZE = FlowWriteConfigurations.getIntProperty(1, "max.request.size");
    public static final String HBASE_TABLE_NAME = FlowWriteConfigurations.getStringProperty(1, "hbase.table.name");


}