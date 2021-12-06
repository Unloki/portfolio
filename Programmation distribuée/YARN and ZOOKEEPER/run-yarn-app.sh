APP=csp_V3.jar

FILE_CSP=csp_V3/
SRC_CSP=csp_V3/*.java

#### Add maunally all the classpath due to a problem (bashrc)
CP_HADOOP=/share/common/hadoop-3.0.0/share/hadoop
CP_CLIENT=${CP_HADOOP}/client/*
CP_COMMON=${CP_HADOOP}/common/*
CP_HDFS=${CP_HADOOP}/hdfs/*
CP_YARN=${CP_HADOOP}/yarn/*~
CP_ZK=${ZK_HOME}/*:${ZK_HOME}/lib/*
CLASSPATH=${CP_ZK}:${CP_CLIENT}:${CP_COMMON}:${CP_HDFS}:${CP_YARN}:.

###### Compile and execute the project
javac -cp ${CLASSPATH} ${SRC_CSP}
###### Create the jar
jar -cvf ${APP} ${FILE_CSP}
###### Copy files on the hdfs filesystem
hdfs dfs -copyFromLocal -f ${APP} /user/e2002328/${APP}
###### run the jar with yarn
###### Client : <znode_jar_path> <n_instance> <config_file_path> <APP_jar>
yarn jar ${APP} csp_V3.YARNClient  10 buildnet/conf-buildnet.txt /user/e2002328/${APP}
