package com.zdjizhi.topology;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.functions.FilterNullFunction;
import com.zdjizhi.utils.functions.MapCompletedFunction;
import com.zdjizhi.utils.functions.ObjectCompletedFunction;
import com.zdjizhi.utils.functions.TypeMapCompletedFunction;
import com.zdjizhi.utils.kafka.Consumer;
import com.zdjizhi.utils.kafka.Producer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author qidaijie
 * @Package com.zdjizhi.topology
 * @Description:
 * @date 2021/5/2016:42
 */
public class LogFlowWriteTopology {
    private static final Log logger = LogFactory.get();

    public static void main(String[] args) {
        final StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        //开启Checkpoint，interval用于指定checkpoint的触发间隔(单位milliseconds)
//        environment.enableCheckpointing(5000);

        //两个输出之间的最大时间 (单位milliseconds)
        environment.setBufferTimeout(FlowWriteConfig.BUFFER_TIMEOUT);

        DataStreamSource<String> streamSource = environment.addSource(Consumer.getKafkaConsumer())
                .setParallelism(FlowWriteConfig.SOURCE_PARALLELISM);

        if (FlowWriteConfig.LOG_NEED_COMPLETE == 1) {
            DataStream<String> cleaningLog;
            switch (FlowWriteConfig.LOG_TRANSFORM_TYPE) {
                case 0:
                    //对原始日志进行处理补全转换等，不对日志字段类型做校验。
                    cleaningLog = streamSource.map(new MapCompletedFunction()).name("MapCompletedFunction")
                            .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);

                    break;
                case 1:
                    //对原始日志进行处理补全转换等，强制要求日志字段类型与schema一致。
                    cleaningLog = streamSource.map(new ObjectCompletedFunction()).name("ObjectCompletedFunction")
                            .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);

                    break;
                case 2:
                    //对原始日志进行处理补全转换等，对日志字段类型做若校验，可根据schema进行强转。
                    cleaningLog = streamSource.map(new TypeMapCompletedFunction()).name("TypeMapCompletedFunction")
                            .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);

                    break;
                default:
                    //对原始日志进行处理补全转换等，不对日志字段类型做校验。
                    cleaningLog = streamSource.map(new MapCompletedFunction()).name("MapCompletedFunction")
                            .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);

            }

//            //过滤空数据不发送到Kafka内
            DataStream<String> result = cleaningLog.filter(new FilterNullFunction()).name("FilterAbnormalData")
                    .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);

            //发送数据到Kafka
            result.addSink(Producer.getKafkaProducer()).name("LogSinkKafka")
                    .setParallelism(FlowWriteConfig.SINK_PARALLELISM);
        } else {
            //过滤空数据不发送到Kafka内
            DataStream<String> result = streamSource.filter(new FilterNullFunction()).name("FilterOriginalData")
                    .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);
            //发送数据到Kafka
            result.addSink(Producer.getKafkaProducer()).name("LogSinkKafka")
                    .setParallelism(FlowWriteConfig.SINK_PARALLELISM);
        }

        try {
            environment.execute(args[0]);
        } catch (Exception e) {
            logger.error("This Flink task start ERROR! Exception information is :" + e);
            e.printStackTrace();
        }

    }


}
