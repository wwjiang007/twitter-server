#!/bin/bash

sbtver=0.13.15
sbtjar=sbt-launch.jar
sbtsha128=61bfa3f5791325235f6d7cc37a7e7f6bfeb83531

sbtrepo=http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch

if [ ! -f $sbtjar ]; then
  echo "downloading $PWD/$sbtjar" 1>&2
  if ! curl --location --silent --fail --remote-name $sbtrepo/$sbtver/$sbtjar; then
    exit 1
  fi
fi

checksum=`openssl dgst -sha1 $sbtjar | awk '{ print $2 }'`
if [ "$checksum" != $sbtsha128 ]; then
  echo "bad $PWD/$sbtjar.  delete $PWD/$sbtjar and run $0 again."
  exit 1
fi

[ -f ~/.sbtconfig ] && . ~/.sbtconfig

java -ea                          \
  $SBT_OPTS                       \
  $JAVA_OPTS                      \
  -Djava.net.preferIPv4Stack=true \
  -XX:+AggressiveOpts             \
  -XX:+UseParNewGC                \
  -XX:+UseConcMarkSweepGC         \
  -XX:+CMSParallelRemarkEnabled   \
  -XX:+CMSClassUnloadingEnabled   \
  -XX:ReservedCodeCacheSize=128m  \
  -XX:SurvivorRatio=128           \
  -XX:MaxTenuringThreshold=0      \
  -Xss8M                          \
  -Xms512M                        \
  -Xmx2G                          \
  -server                         \
  -jar $sbtjar "$@"
