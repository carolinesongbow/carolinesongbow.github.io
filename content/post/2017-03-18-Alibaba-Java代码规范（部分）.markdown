---
layout: post
title:  "Alibaba: Java 代码规范（部分）"  
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

### 并发处理  
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

* 【推荐】避免 Random 实例被多线程使用，虽然共享该实例是线程安全的，但会因竞争同一 seed 导致的性能下降。  
说明： Random 实例包括 java.util.Random 的实例或者 Math.random()实例。  
正例： 在 JDK7 之后，可以直接使用 API ThreadLocalRandom，在 JDK7 之前，可以做到每个线程一个实例。  
```java  
int randomInt = ThreadLocalRandom.current().nextInt();  
```  

* 【参考】 ThreadLocal 无法解决共享对象的更新问题， ThreadLocal 对象建议使用 static 修饰。这个变量是针对一个线程内所有操作共有的，所以设置为静态变量，所有此类实例共享此静态变量 ，也就是说在类第一次被使用时装载，只分配一块存储空间，所有此类的对象(只要是这个线程内定义的)都可以操控这个变量。  

### 控制语句  
* 【强制】在一个 switch 块内，每个 case 要么通过 break/return 等来终止，要么注释说明程序将继续执行到哪一个 case 为止； 在一个 switch 块内，都必须包含一个 default 语句并且放在最后，即使它什么代码也没有。
* 【推荐】除常用方法（如 getXxx/isXxx）等外，不要在条件判断中执行其它复杂的语句，将复杂逻辑判断的结果赋值给一个有意义的布尔变量名，以提高可读性。  
说明： 很多 if 语句内的逻辑相当复杂，阅读者需要分析条件表达式的最终结果，才能明确什么样的条件执行什么样的语句，那么，如果阅读者分析逻辑表达式错误呢？  
正例：  
```java  
//伪代码如下
boolean existed = (file.open(fileName, "w") != null) && (...) || (...);
if (existed) {
...
}
```  
反例：  
```java  
if ((file.open(fileName, "w") != null) && (...) || (...)) {
...
}  
```  
* 【推荐】循环体中的语句要考量性能，以下操作尽量移至循环体外处理，如定义对象、变量、获取数据库连接，进行不必要的 try-catch 操作（ 这个 try-catch 是否可以移至循环体外）。  
* 【强制】方法内部单行注释，在被注释语句**上方另起一行**，使用//注释。方法内部多行注释使用/* */注释，注意与代码对齐。  

### 其他  
* 【强制】在使用正则表达式时，利用好其预编译功能，可以有效加快正则匹配速度。
说明： 不要在方法体内定义： ```Pattern pattern = Pattern.compile(规则);```  

## 异常日志  
### 异常处理  
* 【强制】异常不要用来做流程控制，条件控制，因为异常的处理效率比条件分支低。
* 【强制】对大段代码进行 try-catch，这是不负责任的表现。 catch 时请分清稳定代码和非稳定代码，稳定代码指的是无论如何不会出错的代码。对于非稳定代码的 catch 尽可能进行区分异常类型，再做对应的异常处理。  
* 【强制】不能在 finally 块中使用 return， finally 块中的 return 返回后方法结束执行，不会再执行 try 块中的 return 语句。
* 【推荐】方法的返回值可以为 null，不强制返回空集合，或者空对象等，必须添加注释充分说明什么情况下会返回 null 值。调用方需要进行 null 判断防止 NPE 问题。  
说明： 本规约明确防止 NPE 是**调用者**的责任。即使被调用方法返回空集合或者空对象，对调用者来说，也并非高枕无忧，必须考虑到远程调用失败，运行时异常等场景返回 null 的情况。  
* 【推荐】返回类型为包装数据类型，有可能是 null，返回 int 值时注意判空。  
反例： public int f(){ return Integer 对象}; 如果为 null，自动解箱抛 NPE。
* 【推荐】定义时区分 unchecked / checked 异常，避免直接使用 RuntimeException 抛出，更不允许抛出 Exception 或者 Throwable，应使用有业务含义的自定义异常。推荐业界已定义过的自定义异常，如： DAOException / ServiceException 等。  
### 日志规约
* 【强制】日志文件推荐至少保存 15 天，因为有些异常具备以“周”为频次发生的特点。
* 【强制】对 trace/debug/info 级别的日志输出，必须使用条件输出形式或者使用占位符的方式。  
说明： logger.debug("Processing trade with id: " + id + " symbol: " + symbol);  
如果日志级别是 warn，上述日志不会打印，但是会执行字符串拼接操作，如果 symbol 是对象，会执行 toString()方法，浪费了系统资源，执行了上述操作，最终日志却没有打印。  
正例： （条件）  
```java  
if (logger.isDebugEnabled()) {
logger.debug("Processing trade with id: " + id + " symbol: " + symbol);
}
```  
正例： （占位符）
```java  
logger.debug("Processing trade with id: {} symbol : {} ", id, symbol);  
```  
## 工程规约
### 应用分层
【参考】分层领域模型规约：  
1. DO（ Data Object） ：与数据库表结构一一对应，通过 DAO 层向上传输数据源对象。  
2. DTO（ Data Transfer Object） ：数据传输对象， Service 和 Manager 向外传输的对象。  
3. BO（ Business Object） ：业务对象。 可以由 Service 层输出的封装业务逻辑的对象。  
4. QUERY：数据查询对象，各层接收上层的查询请求。 注：超过 2 个参数的查询封装，禁止使用 Map 类来传输。  
5. VO（ View Object） ：显示层对象，通常是 Web 向模板渲染引擎层传输的对象。  

## 安全规约
* 【强制】在使用平台资源，譬如短信、邮件、电话、下单、支付，必须实现正确的防重放限制，如数量限制、疲劳度控制、验证码校验，避免被滥刷、资损。  
说明： 如注册时发送验证码到手机，如果没有限制次数和频率，那么可以利用此功能骚扰到其它用户，并造成短信平台资源浪费。  