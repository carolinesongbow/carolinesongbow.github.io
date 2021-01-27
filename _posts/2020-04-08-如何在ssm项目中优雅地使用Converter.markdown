---
layout: post
title:  "如何在ssm项目中优雅地使用Converter"
date:   2020-04-08 16:01:54 +0800
tags: [springboot, mybatis, converter]
---



## 读取springboot的配置文件时使用Converter

因为项目中引入了公司自己编写的一个支持 BaseMapper 的 Mybatis 包，但这个自编 Mybatis 包有个问题，没有默认方言配置，不配置方言会报 NPE。

而 Mybatis 的默认 yaml 文件配置中，没有关于方言的配置，之前项目中使用了丑陋麻烦的注入方案：

```java
@Configuration
@EnableTransactionManagement
@AutoConfigureAfter({SpringBootConfiguration.class})
public class MybatisConfig implements TransactionManagementConfigurer {
    private String dialect = "org.apache.ibatis.dialect.MysqlDialect";
    @Autowired
    private DataSource dataSource;
    
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean() {
        SqlSessionFactory sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.xxxx.xxx");
        sqlSessionFactoryBean.setDialect(dialect);
       
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath*:com/xxx/xxx/*.xml"));
            sqlSessionFactoryBean.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
            sqlSessionFactoryBean.getObject().getConfiguration().setMapUnderscoreToCamelCaseForMap(true);
            return sqlSessionFacotryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Bean
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Configuration
    @AutoConfigureAfter(MybatisConfig.class)
    public static class MyBatisMapperScannerConfig {
        @Bean
        public MapperScannerConfigurer mapperScannerConfigurer() {
            MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
            mapperScannerConfigurer.setCamelhumpToUnderline(true);
            mapperScannerConfigurer.setBasePackage("com.xxx.xxx.**.dao");
            mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
            return mapperScannerConfigurer;
        }
    }
}
```



如果已经有 Mybatis 的配置类，加入一行方言配置倒是比较方便的做法。

但我想试试有没有别的办法。



对代码进行分析，发现在自写的 Configuration 类中，有属性定义`protected Dialect dialect;`

问题来了，Configuration 类中的属性，又是什么时候设置的？只要我干涉这个设置方法，就有可能可使配置生效。

查看 MybatisProperties 类，重点代码缩略如下：

```java
@ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
public class MybatisProperties {

  public static final String MYBATIS_PREFIX = "mybatis";
    
  /**
   * A Configuration object for customize default settings. If {@link #configLocation} is specified, this property is
   * not used.
   */
  @NestedConfigurationProperty
  private Configuration configuration;
}
```

也就是说，配置文件中以`mybatis.configuration`为开头的配置都读取了进来，理论上，我配置上以`mybatis.configuration.dialect`为键的配置，该配置应该就能生效。

故我做了如下配置：

1. yaml 文件配置：

   ```yaml
   mybatis.configuration.dialect: org.apache.ibatis.dialect.MysqlDialect
   ```

   

这样配置以后，启动的时候报异常：无法把一个配置 String 转为一个 Dialect 对象。

配置中的所有键值，实质上是通过转换器转换为代码中定义的对象类型的，而一些基本的转换器都已经被定义好了，如 String 转 Integer，String 转 Enum 等等，我们现在要做的，就是自己写一个转换器，把`org.apache.ibatis.dialect.MysqlDialect`转成一个 Dialect 对象，并把这个转换器，注册到框架的转换器的集合中去。

中间寻找方案的过程就不赘述了，最后转换器的定义：



2. 增加将类型转换 Converter

    ```java
    @Component
    @ConfigurationPropertiesBinding
    public class DialectConverter implements Converter<String, Dialect> {
        @SneakyThrows
        @Override
        public Dialect convert(String clazz) {
            return (Dialect) Class.forName(clazz).getDeclaredConstructor().newInstance();
        }
    }
    ```



如上配置后，就可以将方言做正确的配置啦！



## 使用 Mybatis 时，自动将枚举和字符串之间进行转换

在定义实体类时，将其中一个岗位定义为了枚举。

在使用中发现，虽然查询条件中的枚举正常转为了字符串，但查询出来的结果在转为实体类时，该枚举字段值为 null。

实体类定义：

```java
@Entity
@Table(name = "applicant")
public class Applicant {
    @Id
    private String applicantId;
    private String applicantName;
    private PositionEnum applicantPosition;
    //...
}
```

枚举定义：

```java
/**
 * 职位枚举
 */
public enum PositionEnum {
    JAVA,
    H5,
    ANDROID,
    IOS;
}
```

Dao方法定义：

```java
List<Applicant> selectByPositionAndIsWorking(@Param("position")PositionEnum applicantPosition, @Param("isWorking")Integer isWorking);
```

Mapper定义：

```xml
<select id="selectByPositionAndIsWorking" resultType="Applicant">
        select * from applicant
        <where>
            <if test="isWorking!=null">
                is_working = #{isWorking}
            </if>
            <if test="position!=null">
                and applicant_position = #{position}
            </if>
        </where>
</select>
```



如上，

运行时翻译得到的 SQL 语句是正常的：

```sql
select applicant_id,applicant_name,applicant_position,is_working 
from applicant 
where is_working=1 and applicant_position='JAVA';
```

但是查出来的结果，Applicant 对象中的 applicantPosition 结果却为 null。

分析认为需要配置一个转换器。

修改 yaml 文件：

```yaml
mybatis.configuration.default-enum-type-handler: org.apache.ibatis.type.EnumTypeHandler
```

