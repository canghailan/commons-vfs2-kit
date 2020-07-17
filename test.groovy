// 打印FISH对象
println "FISH: " + FISH
// 打印当前工作目录
println "CWD: " + CWD


// 获取上级目录
println "PARENT: " + FILE(CWD + "../")

// 获取下级文件列表
println "LIST src/:"
println LIST(CWD + "src/")

// 获取文件树
println "TREE src/:"
println TREE(CWD + "src/")

// 遍历文件树
println "FILES src/:"
FILES(TREE(CWD + "src/")).each { file ->
    if (file.regularFile) {
        // 打印普通文件
        println file
        // 打印文件属性
        println STAT(file)
    }
}

// 读取文本文件内容
println "READ test.groovy:"
println READ(CWD + "test.groovy")

// 写入文本文件内容
println "WRITE test.txt:"
println WRITE(CWD + "test.txt", "hello world!")

// 复制文件
println "COPY test.txt -> test-copy.txt:"
println COPY(CWD + "test.txt", CWD + "test-copy.txt")

// 移动文件
println "MOVE test-copy.txt -> test-move.txt:"
println MOVE(CWD + "test-copy.txt", CWD + "test-move.txt")

// 删除文件
println "DELETE test-move.txt: "
println DELETE(CWD + "test-move.txt")

// 安装校验和工具命令
INSTALL "cc.whohow.fs.shell.provider.checksum.Checksum"
// 计算文件MD5值
println "MD5 test.groovy: " + Checksum("MD5", CWD + "test.groovy")