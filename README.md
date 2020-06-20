# 虚拟文件系统

## 快速上手
配置文件[vfs.yaml](vfs.yaml.md)
```markdown
providers:
  aliyun-oss:
    className: cc.whohow.fs.provider.aliyun.oss.AliyunOSSFileProvider
    profiles:
    - accessKeyId: ******
      secretAccessKey: ******
    automount: true
```


## 文件存储支持
* 本地文件系统
* HTTP/HTTPS链接
* 阿里云OSS
* 腾讯云COS

## 优化

