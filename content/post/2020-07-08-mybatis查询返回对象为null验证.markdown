---
layout: post
title:  "mybatis查询返回对象为null验证"
date:   2020-07-08 09:06:54 +0800
tags: [mybatis]
---

昨天浏览项目代码的时候，发现代码中有如下判断：

```java
List<User> users = userDao.selectUsers(age);
for(User user : users) {
    if (user != null) { // 这里的null判断是我疑惑的点
        mqQueue.push(user);
    }
}
```

感觉如果返回对象属性都是 null 的情况下，Mybatis 应该会返回一个空的 User 对象才对，猜测应该不用进行 null 判断，故做了如下代码修改进行验证：



* 数据库数据准备

  User表：

  | id   | age  | remark1 | remark2 |
  | ---- | ---- | ------- | ------- |
  | 1    | 11   |         |         |
  | 2    | 11   |         |         |
  | 3    | 11   |         |         |

* User 类

  ```java
  @Data
  public class User {
      private Integer id;
      private Integer age;
      private String remark1;
      private String remark2;
  }
  ```

  



* XML 查询语句

  ```xml
  <select id="selectUsers" parameterType="User">
  	select remark1,remark2 from User where age='11'
</select> 
  ```
  
  

* Dao 定义

  ```java
  List<User> selectUsers();
  ```

* 测试代码

  ```java
  @Test
  public void testMybatisNull() {
      List<User> users = userDao.selectUsers();
      System.out.println(users);
  }
  ```

* 测试结果打印

  ```
  [null, null, null]
  ```



故当 Mybatis 查询结果不包含必不为空的字段时，应做 null 判断。

个人认为这是 Mybatis 的问题。可自行使用其他方式（比如查询结果中带上 id 等）避免查询返回对象为 null 的情况。