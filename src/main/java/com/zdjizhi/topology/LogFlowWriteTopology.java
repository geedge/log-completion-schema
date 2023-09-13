package com.zdjizhi.topology;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.operator.map.MapCompleted;
import com.zdjizhi.operator.map.TypeMapCompleted;
import com.zdjizhi.operator.filter.FilterNull;
import com.zdjizhi.tools.connections.kafka.KafkaConsumer;
import com.zdjizhi.tools.connections.kafka.KafkaProducer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author qidaijie
 * @version 2021/5/2016:42
 */
public class LogFlowWriteTopology {
    private static final Log logger = LogFactory.get();

    public static void main(String[] args) {

        final StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        //两个输出之间的最大时间 (单位milliseconds)
        environment.setBufferTimeout(FlowWriteConfig.BUFFER_TIMEOUT);
        if (FlowWriteConfig.LOG_NEED_COMPLETE == 1) {
            if (FlowWriteConfig.LOG_TRANSFORM_TYPE == 0) {//不对日志字段类型做校验。
                environment.addSource(KafkaConsumer.flinkConsumer()).name(FlowWriteConfig.SOURCE_KAFKA_TOPIC).setParallelism(FlowWriteConfig.SOURCE_PARALLELISM)
                        .process(new MapCompleted()).name("MapCompletedFunction").setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM)
                        .addSink(KafkaProducer.getKafkaProducer()).name(FlowWriteConfig.SINK_KAFKA_TOPIC).setParallelism(FlowWriteConfig.SINK_PARALLELISM);
            } else {//对日志字段类型做弱校验，可根据schema进行强转。
                environment.addSource(KafkaConsumer.flinkConsumer()).name(FlowWriteConfig.SOURCE_KAFKA_TOPIC).setParallelism(FlowWriteConfig.SOURCE_PARALLELISM)
                        .process(new TypeMapCompleted()).name("TypeMapCompletedFunction").setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM)
                        .addSink(KafkaProducer.getKafkaProducer()).name(FlowWriteConfig.SINK_KAFKA_TOPIC).setParallelism(FlowWriteConfig.SINK_PARALLELISM);
            }
        } else {
            environment.addSource(KafkaConsumer.flinkConsumer()).setParallelism(FlowWriteConfig.SOURCE_PARALLELISM)
                    .filter(new FilterNull()).name("FilterOriginalData").setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM)
                    .addSink(KafkaProducer.getKafkaProducer()).name(FlowWriteConfig.SINK_KAFKA_TOPIC).setParallelism(FlowWriteConfig.SINK_PARALLELISM);
        }

        try {
            environment.execute(args[0]);
        } catch (Exception e) {
            logger.error("This Flink task start ERROR! Exception information is :" + e);
            e.printStackTrace();
        }

    }


}
