INSTALL "cc.whohow.fs.shell.provider.rsync.Rsync"

source = CWD + "src/test/"
target = CWD + "temp/test/"

Rsync(source, target)