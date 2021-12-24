---
layout: post
title:  "kubernetes学习笔记"
date:   2019-04-09 10:54:54 +0800
tags: [kubernetes docker]
---



# 入门

Service 是分布式集群架构的核心，特征：

* 拥有一个唯一指定的名字
* 拥有一个虚拟 IP 和端口号
* 能够提供某种远程服务能力
* 被映射到了提供这种服务能力的一组容器应用上

容器提供了强大的隔离功能，所以有必要把为 Service 提供服务的这**组**进程放入容器中隔离。为此，Kubernetes 设计了 Pod 对象，将每个服务进程包装到相应的 Pod 中，使其成为 Pod 中运行的一个 Container。  

为 Pod 贴上标签，为 Service 定义标签选择器 Label Selector，以此关联 Pod 和 Service。



```mermaid
graph TD
A[Node 虚拟机/物理机] --> B(Pod)
A --> C(Pod)
A --> D(Pod)
C --> E((Pause))
C --> F(业务容器)
C --> G(业务容器)
C --> H(业务容器)
E -->|共享| F
E -->|共享| G
E -->|共享| H
```

