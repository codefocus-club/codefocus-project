
# codefocus-project
 
    致力于打造简单快捷高效可用的基础组件以及服务

## 基础组件结构
    1. codefocus-rabbit-starter 基于YML动态配置创建Rabbit 
        version-list：1.0-SNAPSHOT done
    2. codefocus-cache 动态集成
        版本;
            version-list：1.0-SNAPSHOT done
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
            
        