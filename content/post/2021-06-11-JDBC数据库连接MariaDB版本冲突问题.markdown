---
layout: post
title:  "JDBC数据库连接MariaDB版本冲突问题"
date:   2021-06-11 14:20:54 +0800
tags: [jdbc]
---

## 背景

项目中提供数据源连接服务，使用了相当多 JDBC 驱动插件，其中包含 MySQL 5.1.44 版本驱动和 MariaDB 2.2.3 版本驱动。



## 问题

有一个用户使用了较低版本的 Mycat，在连接数据源时，长时间连接不上。



## 排查

JDBC 的  DriverManager 在连接时，会自动尝试所有的驱动的连接（是的，没错，没法指定驱动连接，狗日的）。在本例中，当遍历至 MariaDB 的驱动时，在接收包的时候遇到版本不兼容问题（低版本 Mycat 数据库和高版本 MariaDB 驱动），DEBUG 调试发现，在读取数据库服务器返回的一个流时卡死。卡死原因不明。大致是数据库与驱动版本不兼容问题导致。



## 解决

一开始想尝试用注销再注册的方式把 MariaDB 的驱动放在 DriverManager 静态驱动列表的最尾，防止这一类 MySQL 数据源尝试用 MariaDB 驱动去连接以避免卡死问题。

尝试过程中追踪 MariaDB 驱动源码发现：

```java
public static boolean acceptsUrl(String url) {
  return (url != null) && (url.startsWith("jdbc:mariadb:")
      || (url.startsWith("jdbc:mysql:") && !url.contains(DISABLE_MYSQL_URL)));
}

private static final String DISABLE_MYSQL_URL = "disableMariaDbDriver";
```

即，不符合这个`acceptsUrl`方法条件的，则不会尝试使用 MariaDB 进行连接。



最终解决方式是在 JDBC 连接字符串上增加了 `?disableMariaDbDriver`。

Like `jdbc:mysql://localhost:3306/test?disableMariaDbDriver`