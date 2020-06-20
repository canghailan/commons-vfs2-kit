# vfs.yaml配置文件说明

## 完整配置文件示例
```markdown
vfs: # 虚拟目录挂载点，Key值为虚拟路径，Value值为实际文件系统路径，默认只有在此挂载的文件路径才可访问
  /temp-oss/: oss://temp/path/
  /temp-cos/: cos://temp/path/

providers:
  file: # 本地文件系统，自动挂载
    className: cc.whohow.fs.provider.file.LocalFileProvider
  http: # HTTP链接文件系统，自动挂载
    className: cc.whohow.fs.provider.http.HttpFileProvider
  aliyun-oss: # 阿里云OSS对象存储
    className: cc.whohow.fs.provider.aliyun.oss.AliyunOSSFileProvider
    profiles: # 鉴权配置，目前仅支持AK/SK，可配置多个，可在不同账号
    - accessKeyId: ******
      secretAccessKey: ******
    scheme: oss # URI协议，默认oss
    automount: false # 是否自动挂载所有Bucket，默认不挂载
    watch: # 文件监听
      interval: PT1S # 扫描间隔，默认1S，格式为ISO 8601
  qcloud-cos: # 腾讯云COS对象存储
    className: cc.whohow.fs.provider.qcloud.cos.QcloudCOSFileProvider
    profiles: # 鉴权配置，目前仅支持AK/SK，可配置多个，可在不同账号
    - accessKeyId: ******
      secretAccessKey: ******
    scheme: cos # URI协议，默认cos
    automount: false # 是否自动挂载所有Bucket，默认不挂载
    watch: # 文件监听
      interval: PT1S # 扫描间隔，默认1S，格式为ISO 8601

executor: # IO线程池配置
  corePoolSize: 8 # 核心线程数，默认CPU核心数
  maximumPoolSize: 64 # 最大线程数，默认核心线程数 * 8
  keepAliveTime: PT1M # 线程最大空闲时间，默认1分钟
  maximumQueueSize: 512 # 队列大小，默认最大线程数 * 8

scheduler: # 定时线程池配置
  corePoolSize: 1 # 核心线程数，默认1

cache: # 缓存配置，用于缓存文件路径解析结果
  ttl: PT15M # 缓存过期时间，默认15分钟
  maximumSize: 4096 # 缓存大小，默认4096
```


## 最小阿里云OSS配置文件示例
```markdown
providers:
  aliyun-oss:
    className: cc.whohow.fs.provider.aliyun.oss.AliyunOSSFileProvider
    profiles:
    - accessKeyId: ******
      secretAccessKey: ******
    automount: true
```


## 说明
* 时间格式统一使用ISO 8601，Java可通过[Duration.parse](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-)解析。