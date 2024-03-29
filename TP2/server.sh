pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

java -cp "$basepath"/server.jar:"$basepath"/shared.jar \
  -Djava.rmi.server.codebase=file:"$basepath"/shared.jar \
  -Djava.security.policy="$basepath"/policy \
  ca.polymtl.inf8480.tp1.server.Server $*
