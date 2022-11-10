package com.zdjizhi.topology;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.CustomFile;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.functions.map.MapCompleted;
import com.zdjizhi.tools.functions.map.TypeMapCompleted;
import com.zdjizhi.tools.functions.broadcast.KnowledgeBaseSource;
import com.zdjizhi.tools.functions.filter.FilterNull;
import com.zdjizhi.tools.kafka.KafkaConsumer;
import com.zdjizhi.tools.kafka.KafkaProducer;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Map;

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

        //两个输出之间的最大时间 (单位milliseconds)
        environment.setBufferTimeout(FlowWriteConfig.BUFFER_TIMEOUT);
        if (FlowWriteConfig.LOG_NEED_COMPLETE == 1) {

            //广播流，只开一个并行度
            DataStreamSource<List<CustomFile>> customFileDataStreamSource = environment.addSource(new KnowledgeBaseSource())
                    .setParallelism(1);

            //用于存储广播数据的state
            MapStateDescriptor<String, List<CustomFile>> descriptor = new MapStateDescriptor<>("descriptor", Types.STRING, Types.LIST(TypeInformation.of(CustomFile.class)));

            //将该流广播赋给缓存
            BroadcastStream<List<CustomFile>> broadcast = customFileDataStreamSource.broadcast(descriptor);

            //业务流连接广播流
            BroadcastConnectedStream<Map<String, Object>, List<CustomFile>> connect = environment.addSource(KafkaConsumer.timestampDeserializationConsumer())
                    .setParallelism(FlowWriteConfig.SOURCE_PARALLELISM)
                    .name(FlowWriteConfig.SOURCE_KAFKA_TOPIC)
                    .connect(broadcast);

            //日志清洗
            SingleOutputStreamOperator<String> completed;
            switch (FlowWriteConfig.LOG_TRANSFORM_TYPE) {
                case 0:
                    //对原始日志进行处理补全转换等，不对日志字段类型做校验。
                    completed = connect.process(new MapCompleted())
                            .name("MapCompletedFunction")
                            .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);
                    break;
                case 1:
                    //对原始日志进行处理补全转换等，对日志字段类型做若校验，可根据schema进行强转。
                    completed = connect.process(new TypeMapCompleted())
                            .name("TypeMapCompletedFunction")
                            .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);
                    break;
                default:
                    //对原始日志进行处理补全转换等，对日志字段类型做若校验，可根据schema进行强转。
                    completed = connect.process(new TypeMapCompleted())
                            .name("TypeMapCompletedFunction")
                            .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);
            }

            //发送数据到Kafka
            completed.addSink(KafkaProducer.getKafkaProducer()).name(FlowWriteConfig.SINK_KAFKA_TOPIC)
                    .setParallelism(FlowWriteConfig.SINK_PARALLELISM);
        } else {
            //不进行任何处理，仅发送到kafka内。
            DataStreamSource<String> streamSource = environment.addSource(KafkaConsumer.flinkConsumer())
                    .setParallelism(FlowWriteConfig.SOURCE_PARALLELISM);

            //过滤空数据不发送到Kafka内
            DataStream<String> result = streamSource.filter(new FilterNull()).name("FilterOriginalData")
                    .setParallelism(FlowWriteConfig.TRANSFORM_PARALLELISM);

            //发送数据到Kafka
            result.addSink(KafkaProducer.getKafkaProducer()).name(FlowWriteConfig.SINK_KAFKA_TOPIC)
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
