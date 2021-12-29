---
layout: post
title:  "Spring Cloud Contract契约测试实践"
date:   2018-11-21 16:33:54 +0800
tags: [spring cloud contract]
---



# 背景

今年公司在申请 ISO-9000，因此多了很多质量上的管理要求。之前听到测试部提出需要做大覆盖量的集成测试，感到非常不妥。因为目前我们使用的技术是微服务技术，且有相当一部分数据依赖于外部系统（如银联接口等），如果做集成测试，由于服务很多，因此第一，很难保证数据的一致性；第二，很难保证其他服务的稳定程度。后果就是出现错误的概率高、维护成本高、排查问题的成本高。  

因此我在网上搜索微服务适用的测试框架，发现现在有些新的测试框架是基于契约的测试，这样对微服务来说会更友好。  

由于目前项目使用的就是 spring cloud，因此选择了目前已经较成熟的契约测试框架 spring cloud contract 进行实践调研。  

[spring cloud contract 官方文档地址](https://cloud.spring.io/spring-cloud-contract/single/spring-cloud-contract.html)



# 代码实践

## 本地场景

由于一个项目组几乎都维护着多个服务，某个开发任务可能需要同时修改多个系统。因此在开发自测时，需要用本地的契约测试。  

### 服务端

* 导入 gradle 配置：  

```groovy
buildscript {
    ext {
        //...
        springCloudContractVersion = '2.0.1.RELEASE'
    }
    dependencies {
        //...
        classpath('org.springframework.cloud:spring-cloud-contract-gradle-plugin:${springCloudContractVersion}')
    }
}
//...
apply plugin: 'spring-cloud-contract'
apply plugin: 'maven-publish'

dependencyManagement {
    imports {
        //...
        mavenBom 'org.springframework.cloud:spring-cloud-contract-dependencies:' + springCloudContractVersion
    }
}

dependencies {
    //...
    testCompile('org.springframeword.cloud:spring-cloud-starter-contract-verifier')
}

contracts {
    // 基类所在的包
    packageWithBaseClasses 'com.carolinetest.appmanage.contract'
}

// contract publish
publishing {
    publications {
        stubs(MavenPublication) {
            groupId 'com.carolinetest'
            artifactId 'appmanage-stubs'
            version '0.0.1'
            artifact verifierStubsJar
        }
    }
    repositories {
        mavenLocal()
    }
}
```

待测试接口为：  

```java
@GetMapping("/app/appmanage/msgtemplate/findAlertOvertimeTemplateId")
public Map<String, Object> findAlertOvertimeTemplateId(@RequestParam String channelNo) {
    return success(msgTemplateService.findAlertOvertimeTemplateId(channelNo));
}
```

* 编写 contract 基类，放在 build.gradle 中配置的 packageWithBaseClasses 目录下：  

```java
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class})
public class ContractVerifierBase {
    @MockBean
    protected MsgTemplateService msgTemplateService;
    @Autowired
    protected MsgTemplateForAppController msgTemplateForAppController; // 接口所在的类
    @Autowired
    // 不能省略
    protected WebApplicationContext webApplicationContext;
    
    @Before
    // 这个注解千万别漏了！！！
    public void setup() throws Exception {
        RestAssuredMockMvc.standaloneSetup(msgTemplateForAppController);
        MsgTemplate msg1 = new MsgTemplate();
        MsgTemplate msg2 = new MsgTemplate();
        MsgTemplate msg3 = new MsgTemplate();
        // complete object
        List<MsgTemplate> msgList = new ArrayList<>();
        msgList.add(msg1);
        msgList.add(msg2);
        msgList.add(msg3);
    }
}
```

* 编写 groovy 脚本，放在 src.test.resources.contracts 目录下：  

```groovy
Contract.make {
    description("find_alert_overtime_template") //描述
    request {
        // 请求契约，可使用正则
        url '/app/appmanage/msgtemplate/findAlertOvertimeTemplateId'
        method 'GET'
    }
    response {
        // 返回契约
        status 200
        headers {
            contentType(applicationJsonUtf8)
        }
        body('''
        {
        	"head" : {
        		"retFlag":"00000",
        		"retMsg":"处理成功"
        	},
            "body" : [
            	{
            		"templateId":"0587147ea649403db9bfba52aac3decb",
            		"alertOvertime":10
            	},
            	{
            		"templateId":"6282bce9384c48dc9148fc718d45aa87",
            		"alertOvertime":10
            	}
            ]
        }
        ''')
    }
}
```

* 执行 gradle generateContractTests，生成测试位于 build/generated-test-sources/contracts 下。  

  生成的测试如下图：  ![contractverifiertest](/images/contractverifiertest.png)

  



* 直接运行该测试，即可进行单元测试。  

* 执行 gradle publishStubsPublicationToLocalRepository 发布到本地仓库。

* 查看本地仓库，已生成契约 stub。

  ![本地仓库](/images/本地仓库.png)



### 消费端

* 导入 gradle 配置，基本与服务端相同

* 正常编写单元测试：  

  ```java
  @RunWith(SpringRunner.class)
  @SpringBootTest
  @AutoConfigureStubRunner(ids = {"com.carolinetest:appmanage-stubs:0.0.1:8095"}, workOffline = true)
  @WebAppConfiguration
  public class AppManageServiceContractTest {
      private static final Log logger = LogFactory.getLog(AppManageServiceContractTest.class);
      
      @Autowired
      private StubFinder stubFinder;
      
      @Test
      public void findAlertOvertimeMsgTest() {
          int port = stubFinder.findStubUrl("com.carolinetest", "appmanage-stubs").getPort();
          Map<String, Object> result = 
              HttpUtil.restExchangeMapOrigin(HttpMethod.GET, "http://localhost:" + port + "/app/appmanage/msgtemplate/findAlertOvertimeTemplateId", null);
          logger.info("result:" + result);
          Assert.assertTrue(HttpUtil.isSuccess(result));
      }
  }
  ```

  其中 `@AutoConfigureStubRunner`的 ids 配置坑很多，需要特别注意。当 workOffline 是 true 时，配置格式为：  

  `{groupId}:{artifactId}:{version}:{port}`  

  其中 version 可写成「+」加号，意为自动使用最高版本；  

  port 意为 stub 包启动端口号，可省略，不配置的时候使用默认端口号启动。   

* 运行该单元测试，执行成功！

## 共连 maven 私服场景

### 服务端

* 增加 maven 库配置 gradle.properties:  

  ```properties
  # Project-wide Gradle settings.
  RELEASE_REPOSITORY_URL=http://10.164.194.139:8081/nexus/content/repostiries/release/
  SNAPSHOT_REPOSITORY_URL=http://10.164.194.139:8081/nexus/content/repositories/snapshots/
  # nexus服务器
  NEXUS_USERNAME=admin
  NEXUS_PASSWORD=admin123
  ```

* 修改 build.gradle:  

  ```groovy
  //...
  publishing {
      //...
      repositories {
          mavenLocal()
          maven {
              url RELEASE_REPOSITORY_URL
              credentials {
                  username = NEXUS_USERNAME
                  password = NEXUS_PASSWORD
              }
          }
      }
  }
  ```

* 发布

  只发布到远端 maven 库：  

  gradle publishStubsPublicationToMavenRepository

  发到所有配置的发布渠道（本地库和远端 maven 库）：

  gradle publish

* 查看，均已发布成功！

  ![stubspublish](/images/stubspublish.png)

### 消费端

* 修改@AutoConfigureStubRunner

  ids = {"com.carolinetest:appmanage-stubs:+", repositoryRoot = "http://10.164.194.139:8081/nexus/content/repostiries/release/"}

* 运行单元测试，成功！

在测试中，出现一个错误，程序报错：  

```
Caused by: java.lang.IllegalStateException: The artifact was found in the local repository but you have explicitly stated that it should be downloaded from a remote one
```

报错原因：  

本地 maven 库包与远端 maven 库包有一样的版本号，但本地 maven 库包更新（发布时间更晚），因此报错。  

解决方法：  

1. 删除本地 maven 库的包；  
2. 在服务端重新执行 publish 至远端；



注意：如果要同时发布至本地和远端 maven 库，直接使用 publish 命令即可，最好不要分别发布！如果必须分别发布，请先发布本地，再发布远端！



