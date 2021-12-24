---
layout: post
title:  "Eureka 的 RestTemplate 问题"
date:   2017-03-04 14:53:54 +0800
tags: [Java, Eureka]
---  

环境中配置了 Eureka ，在发起 HttpClient 请求时，遇到「eureka No instances available for ...」的问题。这是由于发起请求的目标域名在 Eureka 服务器中没有实例。  
查 [资料](http://stackoverflow.com/questions/31574131/ribbon-with-spring-cloud-and-eureka-java-lang-illegalstateexception-no-instanc) ，有两种解决办法：  
1. 不要使用 netflix 这套路由服务（这居然也是一种办法么= =）；
2. 不要使用默认的 RestTemplate 来发起请求，应 new 一个新的 RestTemplate 来操作。

具体实现代码如下：  
```java  
JSONObject json = new JSONObject(sendParam);
HttpHeaders headers = new HttpHeaders();
MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
headers.setContentType(type);
HttpEntity<String> reqE = new HttpEntity<String>(json.toString(), headers);
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<Map> result = restTemplate.exchange(url, HttpMethod.POST, reqE, Map.class);
```

