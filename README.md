# Shuai数据库

#### 介绍
一款java实现的内存非关系型数据库

#### 软件架构
![image](https://user-images.githubusercontent.com/99642002/221918471-eb77b4c2-f5bf-49b4-b963-e645097a34b7.png)
功能一览：
1. 事件处理器：使用多路复用监听多个套接字，采用主从Reactor模式
2. 存储结构：内置字符串、列表、哈希、集合、有序集合五种存储结构
3. 并发性能： 充分利用java并发结构设计，提高并发程度和执行速度
4. 过期、淘汰：使用DelayQueue实现过期队列，采用LRU、RANDOM等多种淘汰策略
5. 持久化：拥有AOF、RDB持久化方式外，开创AOF+RDB和lsm树配合分块的持久化方式
6. raft：使用raft完成集群功能，拥有复制和通信功能

#### 安装教程

1.  运行ShuaiServer和ShuaiClient可以采用命令行对数据库进行交互
2.  运行ShuaiTest可以模拟benchmark对数据库进行批量命令并发测试（前提是resources/test中有output.xlsx文件）
3.  访问https://yiqikang.xyz可以看到使用ShuaiDatabase作为缓存的论文系统

#### 使用说明

https://meeting.tencent.com/v2/cloud-record/share?id=504e63eb-c6e1-47bd-8916-55bd1598e2e5&from=3

补充：https://meeting.tencent.com/v2/cloud-record/share?id=03aa8917-50e0-40c4-b1fe-6f09b7bdd9a8&from=3

第一个视频是主体，第二个视频是一点补充
ShuaiServer第18行配置端口，第20行配置最大并发数，第26行配置淘汰策略，第38和39行配置持久化策略。
可查看ShuaiCommand或ShuaiRequest查看命令表。命令具体作用可查看https://redis.io/commands/

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
