---
layout: post
title:  "Java 1.8 学习笔记——Lambda"
date:   2017-04-2 17:35:54 +0800
tags: [Java]
---

## java中重要的函数接口


| 接口        | 参数           | 返回类型  | 示例  |  
| ------------- |:-------------:| :-----:| -----:|  
| Predicate< T > | T | boolean | 这张唱片已经发行了吗 |  
| Consumer< T > | T | void | 输出一个值 |  
| Function< T,R > | T | R | 获得Artist对象的名字  
| Supplier< T > | None | T | 工厂方法  
| UnaryOperator< T > | T | T | 逻辑非  
| BinaryOperator< T > | (T,T) | T | 求两个数的乘积  


## stream 中的 filter 方法和 count 方法
filter 方法并未做什么实际性的工作，只刻画出了stream，但没有产生新的集合，这类方法叫**惰性求值**；  
count 方法最终会从 stream 产生值，这类方法叫**及早求值方法**。

## Stream.of()和*.stream()的区别
Stream.of(T t...)中t是流的多个元素，.stream()是把集合的每个元素变成流   
```java  
Stream<List<Track>> stream = Stream.of(tracks);  
Stream<Track> stream2 = tracks.stream();
```



* map  
元素处理，一一映射   
```java  
List<Integer> collect1 = allInt.stream().filter(ele -> ele > 5)
		.map(ele -> ele * 10).collect(Collectors.toList());  
System.out.println(collect1);    
```


* filter  
过滤元素
* flatMap  
可以把多个流合并成一个流
返回值是stream   
```java
List<Integer> collect = Stream.of(list1, list2).flatMap(ele -> ele.stream())  
                            .collect(Collectors.toList());  
System.out.println(collect);   
```  

* min() & max()   
比较   
```java   
List<Track> tracks = Arrays.asList(new Track("imagine", 92),new Track
					("super star", 28), new Track("halo", 64));
Track track1 = tracks.stream().min(Comparator.comparing(track -> 
					track.getName().length())).get();   
```  

* reduce  
循环计算   
```reduce(初始值, BinaryOperator<T>(传递的循环计算结果, 遍历的流中的值))```   
```java   
Integer reduce = Stream.of(1, 2, 3).reduce(10, (acc, element) -> acc - element);  
```  

* range  
```java  
IntStream.range(0, 5).forEach(num -> System.out.print(num + "  "));
// 输出 0，1，2，3，4，5  
```  

* skip  
跳过前 n 个元素  

* limit  
截取至第 n 个元素  

## 拆装箱  
int => Integer : intFunction  
Integer => int : toIntFunction  
高阶函数使用基本类型：mapToInt()——方法 + to + 基本类型  
mapToInt 返回的不是一个一般的 stream，这个 stream 的 map() 等方法都用的不是原来的接口。  
该特殊 stream 有个 summaryStatistics() 方法得到的对象中有诸多计算属性，方便计算。  
```java
IntSummaryStatistics iss = album.getTracks().stream()
                .mapToInt(trac -> trac.getLength())
                .map(length -> length + 1)
                .summaryStatistics();  
System.out.println(iss);

// 输出
IntSummaryStatistics{count=1, sum=11, min=11, average=11.000000, max=11}  
```

## 重载  
会优先使用最具体的类型的函数  
错误： ```Ambiguous method call .Both```  
重载函数模糊调用，参数列表都匹配。  
* 只有一个可能的目标类型：由相应函数接口里的参数类型推导得出；  
* 多个可能的目标类型：由最具体的参数类型推导得出；  
* 多个可能的目标且具体类型不明：需人为指定类型，否则编译报错。

## 默认方法  
接口（无论函数还是非函数）告诉它的所有子类：如果你没有实现这个方法，就使用我的吧。
子类重写方法覆盖默认方法。  
如果一个类实现了两个含有相同签名函数的接口，编译报错。解决方法：实现类实现该函数。  
* 类胜于接口  
* 子类胜于父类  
接口：允许多重继承，没有成员变量；  
抽象类：不能多重继承，可继承成员变量。

## Optional
```java
String s = null;
Optional<String> optional = Optional.ofNullable(s);
s = "aa";
```  
在上面这段代码中，最后一行赋值并没有任何作用：Optional对象直接被赋予了String的值，而非String对象

Optional对空值更简单的处理：
```java  
Optional<String> op = Optional.ofNullable(str);
String result = op.orElseGet(() -> "");

//相当于：  
String result = str==null ? "" : str;
```

#### orElse 和 orElseGet 的区别
```java  
optional.orElse("unknow");
optional.orElseGet(() -> "unknow");
```

## 流的顺序  
原集合有序，则出去的流依然有序；原集合无序，则出去的流也无序。  
一些操作在有序的流上开销更大，可调用unordered方法消除顺序；大多数操作（filter map reduce）在有序流上效率更高。  

## Collect  
stream类库在collect的时候自动挑选合适的集合类型。  
如果需要指定类型，则可以：  
```java
stream.collect(Collectors.toCollection(TreeSet::new));
```
### 转换成值
```java
// 找出成员最多的乐队
Function<Artist, Long> getCount = artist -> artist.getMembers.count();
Optional<Artist> maxArtist = artists.stream().
    						collect(Collectors.maxBy(
                            Comparator.comparing(getCount)));
    
// 一组乐队的成员平均数
Double averageMember = artists.stream().collect(
    				Collectors.averagingInt(
                    artist -> artist.getMembers().size()));

```

### partitioningBy收集器  
```java  
// 区分乐队和单个歌手
Map<Boolean,List<Artist>> map = 
    			stream.collect(Collectors.partitioningBy
                (artist -> artist.isSolo()));
// 另一种写法：使用方法
Map<Boolean, List<Artist>> map = stream.collect(
    			Collectors.partitioningBy(Artist:: isSolo));
```
   
### 拼接字符串    
把流中的元素按照规律拼接成字符串。    
`joining(元素中分隔字符，开始字符，结束字符)`  
```java
String collect = f4.getMembers().stream()
    			.map(Artist:: getName)
                .collect(Collectors.joining(",","[","]"));
```  

### 组合收集器  
把流中的元素按照规则分组，并与想收集的值关联。    
```groupingBy(分组的方式Function, 收集的数据Collector)```  
```java
// 计算每个艺术家的专辑数
Map<Artist, Long> map = albums.stream().collect(
				Collectors.groupingBy(Album:: getMainArtist, Collectors.counting()));
	
// 每个艺术家的专辑名
Map<Artist, List<String>> map = albums.stream().collect(
				Collectors.groupingBy(Album:: getMainArtist, 
				Collectors.mapping(Album:: getName, Collectors.toList())));
```  

### map的遍历  
```java  
Map<Artist, Integer> countOfAlbums = new HashMap<>();
albumMap.forEach((artist, albumList) -> countOfAlbums.put(artist, albumList.size()));  
```  

## 并行化  
lambda 并行化示例：  
```java  
// 计算专辑曲目总长度
albumList.parallelStream().flatMap(Album:: getTracks).mapToInt(Track:: getLength).sum();  
```  
影响并行流性能的主要因素：  
1. 数据大小  
数据足够大、每个数据处理管道花费的时间足够多时适用并行；  
2. 源数据结构  
不同的数据源分割相对容易；  
3. 装箱  
基本类型快于装箱类型；  
4. 核的数量  
空闲核越多，越适合并行；  
5. 单元处理开销  
花在流中每个元素身上的时间越长，越适合并行操作。  

分割性能好坏：  
* 性能好  
ArrayList、数组、IntStream.range。  
支持随机读取，能轻而易举地被分解。  
* 性能一般  
HashSet、TreeSet。  
不易被公平地分解。  
* 性能差  
LinkedList、Streams.iterate、BufferedReader.lines……  
长度未知，很难预测该在哪里分解。  

无状态操作比有状态操作并行性能更佳。  
无状态操作：map、filter、flatMap  
有状态操作：sorted、distinct、limit  

### 数组并行化  
在工具类 Arrays 中  
parallelPrefix : 任意给定一个 BinaryOperator 函数，计算两个值的结果  
parallelSetAll : 使用 Lambda 表达式更新数组元素  
parallelSort : 并行化对数组元素排序  
```java  
int[] intArray = new int[100];
Arrays.parallelSetAll(intArray, index -> intArray[index] = index + 1);  
```  
求滑动窗口平均数：  
我的做法：  
```java  
double[] mySum = Arrays.copyOf(values, values.length);
double[] myAverage = IntStream.range(0, values.length - movingLength + 1).mapToDouble(i -> {
      return Arrays.stream(mySum).skip(i).limit(movingLength).sum() / movingLength;
}).toArray();  
```  
使用 parallelPrefix 来做：  
```java  
double[] sums = Arrays.copyOf(values, values.length);
// 求得各角标的累计和
Arrays.parallelPrefix(sums, Double:: sum);

int start = movingLength - 1;
double[] average = IntStream.range(start, sums.length).mapToDouble(i -> {
      double prefix = i == start ? 0 : sums[i - movingLength];
      return (sums[i] - prefix) / movingLength;
}).toArray();  
```  

## 代码重构  
### ThreadLocal 的优化  
重构前：  
```java  
ThreadLocal<String> threadLocal = new ThreadLocal(){
	@override  
	protected String initialValue() {
		return getChannelNo();
	}
};
```  
重构后：  
```java  
ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> getChannelNo());  
```  

### 重复代码的优化  
重构前：  
```java  
public long countRunningTime() {
	return albums.stream().mapToLong(album -> album.getTracks()
		.mapToLong(track -> track.getLength()).sum()).sum();
}

public long countMusicians() {
	return albums.stream().mapToLong(album -> album.getMusicians().count()).sum();
}
```  
重构后：  
```java  
public long countFeature(ToLongFunction<Album> function) {
	return albums.stream().mapToLong(function).sum();
}

public long countTracks() {
	return countFeature(album -> album.getTracks().count());
}

public long countMusicians() {
	return countFeature(album -> album.getMusicians().count());
}  
```  

### peek 记录中间值  
peek 方法可以用来记录流中的值以便对程序进行调试。  
```java  
Set<String> nations = album.getMusicians().map(artist -> artist.getNationality())
	.peek(nation -> logger.debug(nation)).collect(Collectors.toSet());  
```  
如果有在 lambda 表达式中打断点的需要，可以定义一个空的方法放在peek中，给这个空方法打断点。  


## 几个 lambda 表达式的最佳实践  
* [对流中的 pojo 元素的排序](https://wizardforcel.gitbooks.io/java8-tutorials/content/Java%208%20Lambda%20%E8%A1%A8%E8%BE%BE%E5%BC%8F%E5%A2%9E%E5%BC%BA%E7%89%88%20Comparator%20%E5%92%8C%E6%8E%92%E5%BA%8F.html)  
```java  
List<Person> persons = new ArrayList<>();
persons.sort((p1, p2) -> p1.getAge().compareTo(p2.getName()));
```  
