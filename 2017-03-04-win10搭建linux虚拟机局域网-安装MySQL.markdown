---
layout: post
title:  "win10 搭建 linux 虚拟机局域网，安装 MySQL"
date:   2017-03-04 13:29:54 +0800
tags: [linux, MySQL]
---
  
> 为了完成搭建 MySQL 主从数据库的任务，在自己电脑上从头搭建环境。遇到的问题和解决方法在这里做一一记录。

#### 问题1：  
> 使用 VMware 安装虚拟机时，出现「not enough free space on disks」的提示。  

解决方法：在配置虚拟机硬盘大小时，应大于等于4GB，否则就会报这个错误。

#### 安装 JDK 记录：
* 使用```su root```命令切换至 root 用户。  
* 使用```rpm -qa | grep java```查看本机是否已安装 Java。
* 使用```yum -y list java*```查看可以下载安装的 Java 包。
* 使用```yum -y install java-1.8.0-openjdk*```安装1.8.0版本的 JDK。 

---
#### 问题2：
> 安装 JDK 时，出现「Disk Requirements:  At least 821MB more space needed on the / filesystem.」的错误提示。  

解决方法：在 VMware 上扩展磁盘后，还需在 linux 上执行硬盘空间的挂载。中间遇到较多问题难以解决，笔者对于 linux 硬盘挂载的原理也不甚了解，目前 linux 虚拟机上没有什么重要的东西，基于时间原因决定放弃，重新安装虚拟机。

**总结：安装虚拟机的时候，千万不要心疼硬盘空间，能省不少麻烦！**

---
#### Linux 网络配置记录：
网络连接设置配置文件：
>* /etc/sysconfig/network
* /etc/sysconfig/network-scripts/ifcfg-eth0
* /etc/sesolv.conf


```shell  
# network 基础配置：
NETWORKING=yes
HOSTNAME=localhost.localdomain
GATEWAY=192.168.129.2
```

```shell  
# ifcfg-eth0 基础配置：
DEVICE="eth0"
#BOOTPROTO="dhcp"
BOOTPROTO="static"
IPADDR=192.168.129.129
NETMASK=255.255.255.0
HWADDR="00:0C:29:56:8F:AD"
IPV6INIT="no"
NM_CONTROLLED="yes"
ONBOOT="yes"
TYPE="Ethernet"
UUID="ba48a4c0-f33d-4e05-98bd-248b01691c20"
DNS1=192.168.129.2
```

```shell  
# resolv.conf 基础配置：
nameserver 192.168.129.2
```

——查阅自[zhanjindong 博客](http://www.cnblogs.com/zhanjindong/p/3250393.html)

---
#### 问题3：
> 在局域网中有两台 linux 虚拟机，网络连接使用的是默认的 NAT，导致两个虚拟机的 IP 地址完全相同。  

解决方法：连接方式更换为 Bridge 桥接模式，顺利解决。

---
#### 卸载 MySQL 记录：
由于搭建 linux 虚拟机的目的是搭建 MySQL 集群，因此需要给两个虚拟机安装同样的 MySQL。
经检查，其中一个虚拟机中已安装 MySQL，因此把它卸载了。

* ```rpm -qa | grep -i mysql```检查机器上是否已安装 MySQL。
* ```service mysql status```检查 MySQL 运行状况。
* 若运行中，```service mysql stop```关闭 MySQL 服务。
* ```rpm -ev MySQL-client.x86_64```进行卸载（需卸载所有已安装的服务，通常有多个）。
* 若失败，可尝试```yum remove  mysql mysql-server mysql-libs mysql-server```
* ```find / -name mysql```系统中残留的 MySQL 相关文件
* ```rm -rf /usr/lib64/mysql```删除这些文件。
* ```userdel mysql```&```groupdel mysql```删除 MySQL 用户及用户组。
* 检查是否卸载干净：```whereis mysql```&```more /etc/passwd | grep mysql```&```more /etc/shadow | grep mysql```&```more /etc/group | grep mysql```

——参考自[[潇湘隐者博客](http://www.cnblogs.com/kerrycode/)]

---

#### 安装 MySQL 记录：
* 安装5.7版本 MySQL : http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.17-1.el6.x86_64.rpm-bundle.tar （CentOS 6.5 应下载 el6 版本，CentOS 7 应下载 el7 版本）。
* ```tar -xvf mysql-5.7.14-1.el6.x86_64.rpm-bundle.tar```解压 tar 包。
* ```rpm -ivh mysql-community-common-5.7.14-1.el6.x86_64.rpm
rpm -ivh mysql-community-libs-5.7.14-1.el6.x86_64.rpm
rpm -ivh mysql-community-client-5.7.14-1.el6.x86_64.rpm
rpm -ivh mysql-community-server-5.7.14-1.el6.x86_64.rpm```
* ```service mysqld start```启动 MySQL 服务。
* ```vi /var/log/mysqld.log```查找临时登陆密码。
* **问题：**登陆后修改密码时遇到无论设什么样的密码都报```ERROR 1819 (HY000): Your password does not satisfy the current policy requirements```，可先在 /etc/my.cnf 配置 ```validate-password=off```，重启服务。
* 修改 mysql 密码```set password=`123` ```。
* 添加远程登陆用户。
```
use mysql;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123' WITH GRANT OPTION;
```
* 检查用户表，刷新内存权限。  
```
mysql> select host,user from user;
+-----------+-----------+
| host      | user      |
+-----------+-----------+
| %         | root      |
| localhost | mysql.sys |
| localhost | root      |
+-----------+-----------+
3 rows in set (0.00 sec)
mysql> flush privileges;
Query OK, 0 rows affected (0.00 sec)
```
* 设置防火墙3306端口开启。
```shell  
vim /etc/sysconfig/iptables
```
添加一行：
```shell  
-A INPUT -m state --state NEW -m tcp -p tcp --dport 3306 -j ACCEPT
```
* 重启防火墙。
```shell  
service iptables restart
```

---
#### 记录：配置虚拟机静态IP
* 关闭 NetworkManager：
```shell  
$ chkconfig NetworkManager off
$ service NetworkManager stop
Stopping NetworkManager daemon: [ OK ]
```
* 配置网络配置文件：
```shell  
$ cd /etc/sysconfig/network-scripts 
$ vim ifcfg-eth0
```
* ifcfg-eth0 配置如下：
```shell  
DEVICE="eth0"
#BOOTPROTO="dhcp"
BOOTPROTO="static"
IPADDR=192.168.1.111
NETMASK=255.255.255.0
HWADDR="00:0C:29:01:2E:9F"
#IPV6INIT="yes"
IPV6INIT="no"
NM_CONTROLLED="yes"
ONBOOT="yes"
TYPE="Ethernet"
UUID="d15902b1-0564-4cd5-9be2-714936bbcd1f"
DNS1=192.168.1.1
GATEWAY=192.168.1.50 #宿主机器IP
BRIDGE="br0"                   
```
* ifcfg-br0 配置如下：
```shell  
DEVICE="br0"
BOOTPROTO="static"
ONBOOT="yes"
IPADDR=192.168.1.111
TYPE="Bridge"
GATEWAY=192.168.1.1
DNS1=192.168.1.1
```
* 重启网络服务。