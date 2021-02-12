sshpass -p 'nvc932' ssh -p 2222 -L 10079:localhost:10079 s208069@helios.cs.ifmo.ru
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/"
sshpass -p 'nvc932' ssh -p 2222 s208069@helios.cs.ifmo.ru

##########################

cp -R /Users/e.norin/IdeaProjects/piedpiper_front/dist /Users/e.norin/IdeaProjects/PiedPiper/src/main/resources/

rm -rf /Users/e.norin/IdeaProjects/PiedPiper/src/main/resources/dist

##########################
