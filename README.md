
# codefocus-project
 
    致力于打造简单快捷高效可用的基础组件以及服务
    
# 最新版本：
    version：1.0.1-SNAPSHOT
    
## 基础组件结构
    1. codefocus-rabbit-starter 基于YML动态配置创建Rabbit 
        简介：
            基于rabbit动态创建rabbit和消费rabbit
        亮点：
            1.动态创建rabbit
            2.支持自定义配置
        具体操作：
            codefocus-rabbit-starter  readme.md            
    2. codefocus-cache 动态集成
        简介：
            基于caffeine和redis的一级缓存和二级缓存同步
        亮点:
            1.批量更新时,不使用Keys命令,提升Redis性能
            2.支持动态自定义缓存自动过期时间
            3.支持DistributedLock  分布式锁
            4.支持RequestLimit   接口限流
            5.支持服务限流
        性能：
            经测试,比只使用Redis要提升2倍之多
        具体操作：
            详见codefocus-cache  readme.md
    3. codefocus-dubbo-trace 自定义分布式链路追踪traceId
        简介：
            自定义分布式链路追踪traceId
        亮点：
            1.采用UUID策略生成traceId
            2.支持定义生成TraceId
            3.主异线程切换TraceId不丢失
        具体操作：
            codefocus-dubbo-trace  readme.md
        