---
layout: post
title:  "Springboot读取配置的另外一种方法"
date:   2021-11-04 11:11:54 +0800
tags: [springboot]
---

```java
@Component
@Configuration
public class MyConfig {
  public MyConfig(AbstractEnvirionment environment) {
    for (PropertySource instanceof OriginTrackedMapPropertySource && 
         propertySource.getName().contains(".properties")) {
      OriginTrackedMapPropertySource source = (OriginTrackedMapPropertySource) propertySource;
      for (Map.Entry<String, Object> entry : source.getSource().entrySet()) {
        System.out.println(entry.getKey() + ":" + entry.getValue());
      }
    }
  }
}
```

记录一下，之后研究。

