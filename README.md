
# codefocus-project
 
    致力于打造开源简单快捷高效可用的基础组件以及服务,保持初心,砥砺前行
    
# 最新版本：
    version：1.0.3-SNAPSHOT
    
# 仓库地址：

     <repository>
        <id>oss-snapshots</id>
        <name>Nexus</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
     </repository> 
        
## 基础组件介绍

    1. codefocus-rabbit-starter 基于YML动态配置创建Rabbit 
        简介：
            基于rabbit动态创建rabbit和消费rabbit
        亮点：
            1.动态创建rabbit
            2.支持自定义配置
        具体操作：
            codefocus-rabbit-starter  readme.md   
                     
    2. codefocus-cache 基于YML动态配置Redis，集成Spring Cache，增加二级缓存caffeine，优化keys及scan命令，
       过期时间扩展，使用方式和Spring Cache完全一样，支持其它更多的功能
    
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
