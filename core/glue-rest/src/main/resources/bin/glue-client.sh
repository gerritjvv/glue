###########################
#
# Start the glue server
# e.g. glue.sh
#!/usr/bin/env bash


abspath=$(cd ${0%/*} && echo $PWD/${0##*/})
GLUE_BIN_HOME=`dirname $abspath`

GLUE_HOME=/opt/glue

export GLUE_CONF_DIR=$GLUE_HOME/conf

#source environment variables

# some Java parameters
if [ "$JAVA_HOME" != "" ]; then
    #echo "run java in $JAVA_HOME"
   JAVA_HOME=$JAVA_HOME
fi

if [ "$JAVA_HOME" = "" ]; then
     echo "Error: JAVA_HOME is not set."
     exit 1
fi

JAVA=$JAVA_HOME/bin/java


if [ -z $JAVA_HEAP ]; then
 export JAVA_HEAP="-Xmx2048m"
fi

#read the processClassPath array and add to CLASSPATH
cp=$(grep processClassPath $GLUE_BIN_HOME/../conf/exec.groovy | sed -e "s/.*: //" -e "s/#.*//")
cp=$(echo $cp | tr "=,[]'\"" "\n" | sed "s;processClassPath;;g")


for d in $cp
do
  if [ -e "$d" -a "$d" != "//" ]; then
    CLASSPATH="$CLASSPATH:$d"
  fi

done


# check envvars which might override default args
# CLASSPATH initially contains $GLUE_CONF_DIR
CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar

# so that filenames w/ spaces are handled correctly in loops below
# add libs to CLASSPATH.
for f in $GLUE_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

CLIENT_CLASS="org.glue.rest.Client"

CLASSPATH=$GLUE_CONF_DIR:$GLUE_CONF_DIR/META-INF:$CLASSPATH

$JAVA -XX:MaxDirectMemorySize=2048M -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:SurvivorRatio=6 -XX:NewRatio=3 -XX:+DisableExplicitGC $JAVA_HEAP $JAVA_OPTS -Djava.library.path="$STREAMS_HOME/lib/native/Linux-amd64-64/" -classpath "$CLASSPATH" $CLIENT_CLASS $@
