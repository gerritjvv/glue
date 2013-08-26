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
 export JAVA_HEAP="-Xmx512m"
fi


# check envvars which might override default args
# CLASSPATH initially contains $GLUE_CONF_DIR
CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar

# so that filenames w/ spaces are handled correctly in loops below
# add libs to CLASSPATH.
for f in $GLUE_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

CLIENT_CLASS="org.glue.rest.ClassPathReader"

CLASSPATH=$GLUE_CONF_DIR:$GLUE_CONF_DIR/META-INF:$CLASSPATH

$JAVA $JAVA_HEAP $JAVA_OPTS  -classpath "$CLASSPATH" $CLIENT_CLASS $@