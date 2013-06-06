#Include the hadoop libraries in this variable
#HADOOP_LIB
#This script tries its best to automatically detect the correct hadoop install classpath

HADOOP_LIB=""

dirs=(     "/opt/hadoop"               
           "/opt/hadoop/lib"
           "/etc/hadoop"      
           "/usr/lib/hadoop/client"
          )

for d in "${dirs[@]}"
do
  if [ -d "$d" ]; then
    for f in $d/*; do
     HADOOP_LIB="$HADOOP_LIB:$f"
    done
  fi
done

export HADOOP_LIB