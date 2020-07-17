println "FISH: " + FISH
println "CWD: " + CWD


println "PARENT: " + FILE(CWD + "../")

println "LIST src/:"
println LIST(CWD + "src/")

println "TREE src/:"
println TREE(CWD + "src/")

println "FILES src/:"
FILES(TREE(CWD + "src/")).each { file ->
    if (file.regularFile) {
        println file
        println STAT(file)
    }
}

println "READ test.groovy:"
println READ(CWD + "test.groovy")

println "WRITE test.txt:"
println WRITE(CWD + "test.txt", "hello world!")

println "COPY test.txt -> test-copy.txt:"
println COPY(CWD + "test.txt", CWD + "test-copy.txt")

println "MOVE test-copy.txt -> test-move.txt:"
println MOVE(CWD + "test-copy.txt", CWD + "test-move.txt")

println "DELETE test-move.txt: " + DELETE(CWD + "test-move.txt")

INSTALL "cc.whohow.fs.shell.provider.checksum.Checksum"
println "MD5 test.groovy: " + Checksum("MD5", CWD + "test.groovy")