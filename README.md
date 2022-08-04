# log-stream-completion-schema

基于Nacos的动态日志预处理程序，接收原始日志根据对应Schema定义进行数据清洗，并将结果回写Kafka。  
当Nacos上Schame变更后可动态获取到最新版本的信息，无需重启任务。


## 函数功能列表
* current_timestamp
> 获取当前时间戳，若追加字段已有时间戳，不予以覆盖。
* snowflake_id
> 雪花ID函数，返回一个一定条件内不重复的 long 类型数值。  
> https://git.mesalab.cn/bigdata/algorithm/snowflake
* geo_ip_detail
> IP定位库，获取对应IP的详细地理位置信息，城市,州/省,国家
* geo_asn
> ASN定位库，获取对应IP的ASN信息
* geo_ip_country
> IP定位库，获取对应IP的地理位置信息，仅包含 国家
* set_value
> 给予字段固定值。
* get_value
> 获取字段值并追加到新的字段。
* if
> IF函数实现，解析日志构建三目运算;包含判断是否为数字若为数字则转换为long类型返回结果。
* sub_domain
> 获取顶级域名
* radius_match
> 根据IP获取对应的Raidus用户信息。  
> 实际数据存储在HBase中，依赖RADIUS-RELATIONSHIP-HBASE-V2程序；使用时加载到内存中加速查询。
* app_match
> 根据APP_ID获取对应的APP名称
* decode_of_base64
> 根据编码解码base64，若编码字段为空则根据默认编码解析(UTF-8)
* flattenSpec
> 根据表达式解析json,使用jsonPath工具类  
> https://github.com/json-path/JsonPath

