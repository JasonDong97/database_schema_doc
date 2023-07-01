### 数据库表结构文档构建器

根据配置文件连接 mysql 数据库，读取数据库表结构，生成 markdown 文档。

#### 使用方式 1

1. 编辑配置文件 `config.yaml`，配置数据库连接信息

```yaml
# 配置数据库连接信息
hostname: <hostname>
port: <port>
dbName: <dbname>
username: <username>
password: <password>
```

2. 在Java IDE中运行 `src/main/java/com/djx/DatabaseDocumentBuilder.java`，生成的文档在 `doc` 目录下

#### 使用方式 2

1. 打包项目

```shell
mvn clean package
```

2. 编辑配置文件 `config.yaml`，配置数据库连接信息

```yaml
hostname: <hostname>
port: <port>
dbName: <dbname>
username: <username>
password: <password>
```

3. 运行打包后的 jar 包

```shell 
java -jar database-document-builder.jar config.yaml
```

#### 相关截图

![image-20230701160109545](https://raw.githubusercontent.com/JasonDong97/blog_pics/master/posts/image-20230701160109545.png)
