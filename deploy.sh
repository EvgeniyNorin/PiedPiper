#!/bin/bash

#restart_jar() {
#  sshpass -p 'nvc932' ssh -T -p 2222 s208069@helios.cs.ifmo.ru <<+
#  jps | grep 'jar' | cut -d ' ' -f 1
#  a=7
#  kill -9 $a
##  kill -9 $(jps | grep "jar" | cut -d " " -f 1)
#+
#}

build_front() {
  cd /Users/e.norin/IdeaProjects/piedpiper_front
  npm run build
  sshpass -p "${PASSWORD}" scp -P 2222 -r /Users/e.norin/IdeaProjects/piedpiper_front/dist s208069@helios.cs.ifmo.ru:~/piedpiper
}

build_backend() {
  cd /Users/e.norin/IdeaProjects/PiedPiper/
  sbt compile
  sbt assembly
  sshpass -p "${PASSWORD}" scp -P 2222 /Users/e.norin/IdeaProjects/PiedPiper/target/scala-2.12/app.jar s208069@helios.cs.ifmo.ru:~/piedpiper
}

PASSWORD='nvc932'
#build_front
build_backend