println "READ test.groovy:"
println READ(PWD + "test.groovy")

INSTALL "cc.whohow.fs.shell.provider.checksum.Checksum"

println "FISH: " + FISH
println "PWD: " + PWD
println "Checksum MD5 test.groovy:"
println Checksum("MD5", PWD + "test.groovy")
println "Parent:"
println FILE(PWD + "../")
println "List src/:"
println LIST(PWD + "src/")
println "Tree src/:"
println TREE(PWD + "src/")