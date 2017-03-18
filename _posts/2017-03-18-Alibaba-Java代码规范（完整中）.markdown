---
layout: post
title:  "Alibaba: Java 代码规范（完整中）"  
date:   2017-03-18 11:10:54 +0800  
tags: [Java, alibaba]
---  

# Java 代码规范

## 编程规约
### 命名规则
* 【强制】抽象类命名使用 Abstract 或 Base 开头；异常类命名使用 Exception 结尾；测试类
命名以它要测试的类的名称开始，以 Test 结尾。


* 【强制】POJO 类中布尔类型的变量，都不要加 is，否则部分框架解析会引起序列化错误。  
反例：定义为基本数据类型 boolean isSuccess；的属性，它的方法也是 isSuccess()，RPC
框架在反向解析的时候，“以为”对应的属性名称是 success，导致属性获取不到，进而抛出异
常。  

* 【强制】包名统一使用小写，点分隔符之间有且仅有一个自然语义的英语单词。包名统一使用**单数**形式，但是类名如果有复数含义，类名可以使用复数形式。  
正例： 应用工具类包名为 com.alibaba.open.util、类名为 MessageUtils（此规则参考
spring 的框架结构）

* 【推荐】接口类中的方法和属性不要加任何修饰符号（public 也不要加），保持代码的简洁
性，并加上有效的 Javadoc 注释。尽量不要在接口里定义变量，如果一定要定义变量，肯定是
与接口方法相关，并且是整个应用的基础常量。  
正例：接口方法签名：void f();
 接口基础常量表示：String COMPANY = "alibaba";  
反例：接口方法定义：public abstract void f();  
说明：JDK8 中接口允许有默认实现，那么这个 default 方法，是对所有实现类都有价值的默
认实现。

* 【参考】各层命名规约：  
A) Service/DAO 层方法命名规约  
--1） 获取单个对象的方法用 get 做前缀。  
--2） 获取多个对象的方法用 list 做前缀。  
--3） 获取统计值的方法用 count 做前缀。  
--4） 插入的方法用 save（推荐）或 insert 做前缀。  
--5） 删除的方法用 remove（推荐）或 delete 做前缀。  
--6） 修改的方法用 update 做前缀。  
B) 领域模型命名规约  
--1） 数据对象：xxxDO，xxx 即为数据表名。  
--2） 数据传输对象：xxxDTO，xxx 为业务领域相关的名称。  
--3） 展示对象：xxxVO，xxx 一般为网页名称。  
--4） POJO 是 DO/DTO/BO/VO 的统称，禁止命名成 xxxPOJO。  

### 常量定义
* 【强制】不允许出现任何魔法值（即未经定义的常量）直接出现在代码中。  
反例： String key="Id#taobao_"+tradeId；
 cache.put(key, value);
* 【强制】long 或者 Long 初始赋值时，必须使用大写的 L，不能是小写的 l，小写容易跟数字
1 混淆，造成误解。  
说明：Long a = 2l; 写的是数字的 21，还是 Long 型的 2? 

### OOP规约 
* 【强制】 Object 的 equals 方法容易抛空指针异常，应使用常量或确定有值的对象来调用equals。  
正例： "test".equals(object);  
反例： object.equals("test");  
说明： 推荐使用 java.util.Objects#equals （JDK7 引入的工具类）  
* 【强制】关于基本数据类型与包装数据类型的使用标准如下：  
--1） 所有的 POJO 类属性必须使用包装数据类型。  
--2） RPC 方法的返回值和参数必须使用包装数据类型。  
--3） 所有的局部变量【推荐】使用基本数据类型。  
说明： POJO 类属性没有初值是提醒使用者在需要使用时，必须自己显式地进行赋值，任何 NPE 问题，或者入库检查，都由使用者来保证。  
正例： 数据库的查询结果可能是 null，因为自动拆箱，用基本数据类型接收有 NPE 风险。  
反例： 比如显示成交总额涨跌情况，即正负 x%， x 为基本数据类型，调用的 RPC 服务，调用不成功时，返回的是默认值，页面显示： 0%，这是不合理的，应该显示成中划线-。所以包装数据类型的 null 值，能够表示额外的信息， 如：远程调用失败，异常退出。  
* 【强制】定义 DO/DTO/VO 等 POJO 类时，不要设定任何属性默认值。  
反例： POJO 类的 gmtCreate 默认值为 new Date();但是这个属性在数据提取时并没有置入具体值，在更新其它字段时又附带更新了此字段，导致创建时间被修改成当前时间。  
* 【强制】构造方法里面禁止加入任何业务逻辑，如果有初始化逻辑，请放在 init 方法中。  
* 【强制】 POJO 类必须写 toString 方法。  
说明： 在方法执行抛出异常时，可以直接调用 POJO 的 toString()方法打印其属性值，便于排查问题。  
* 【推荐】使用索引访问用 String 的 split 方法得到的数组时，需做最后一个分隔符后有无
内容的检查，否则会有抛 IndexOutOfBoundsException 的风险。
说明：  
```java  
String str = "a,b,c,,";
String[] ary = str.split(",");
//预期大于 3，结果是 3
System.out.println(ary.length);
```  
* 【推荐】 final 可提高程序响应效率，声明成 final 的情况：  
--1） 不需要重新赋值的变量，包括类属性、局部变量。  
--2） 对象参数前加 final，表示不允许修改引用的指向。  
--3） 类方法确定不允许被重写。  

### 集合处理  
* 【强制】 关于 hashCode 和 equals 的处理，遵循如下规则：  
--1） 只要重写 equals，就必须重写 hashCode。  
--2） 因为 Set 存储的是不重复的对象，依据 hashCode 和 equals 进行判断，所以 Set 存储的对象必须重写这两个方法。  
--3） 如果自定义对象做为 Map 的键，那么必须重写 hashCode 和 equals。  
正例： String 重写了 hashCode 和 equals 方法，所以我们可以非常愉快地使用 String 对象作为 key 来使用。  

* 【强制】使用集合转数组的方法，必须使用集合的 toArray(T[] array)，传入的是类型完全一样的数组，大小就是 list.size()。  
反例： 直接使用 toArray 无参方法存在问题，此方法返回值只能是 Object[]类，若强转其它类型数组将出现 ClassCastException 错误。  
正例：  
```java  
List<String> list = new ArrayList<String>(2);
list.add("guan");
list.add("bao");
String[] array = new String[list.size()];
array = list.toArray(array);
```  
说明： 使用 toArray 带参方法，入参分配的数组空间不够大时， toArray 方法内部将重新分配内存空间，并返回新数组地址； 如果数组元素大于实际所需，下标为[ list.size() ]的数组元素将被置为 null，其它数组元素保持原值，因此最好将方法入参数组大小定义与集合元素个数一致。

* 【强制】不要在 foreach 循环里进行元素的 remove/add 操作。 remove 元素请使用 Iterator 方式，如果并发操作，需要对 Iterator 对象加锁。  
反例：  
```java  
List<String> a = new ArrayList<String>();
a.add("1");
a.add("2");
for (String temp : a) {
    if("1".equals(temp)){
        a.remove(temp);
    }
}
```  
说明： 以上代码的执行结果肯定会出乎大家的意料，那么试一下把“1”换成“2”，会是同样的结果吗？  
正例：  
```java  
Iterator<String> it = a.iterator();
while(it.hasNext()){
    String temp = it.next();
    if(删除元素的条件){
        it.remove();
    }
}
```  
* 【强制】 SimpleDateFormat 是线程不安全的类，一般不要定义为 static 变量，如果定义为 static，必须加锁，或者使用 DateUtils 工具类。  
正例： 注意线程安全，使用 DateUtils。亦推荐如下处理：  
```java  
private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {  
    @Override
    protected DateFormat initialValue() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }
};  
```  
说明： 如果是 JDK8 的应用，可以使用 Instant 代替 Date， LocalDateTime 代替 Calendar，DateTimeFormatter代替Simpledateformatter，官方给出的解释：simple beautiful strong immutable thread-safe。  
```java  
String time = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
System.out.println(time);  
```  
