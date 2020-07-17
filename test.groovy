println "READ test.groovy:"
println READ(CWD + "test.groovy")

INSTALL "cc.whohow.fs.shell.provider.checksum.Checksum"

println "FISH: " + FISH
println "CWD: " + CWD
println "Checksum MD5 test.groovy:"
println Checksum("MD5", CWD + "test.groovy")
println "Parent:"
println FILE(CWD + "../")
println "List src/:"
println LIST(CWD + "src/")
println "Tree src/:"
println TREE(CWD + "src/")