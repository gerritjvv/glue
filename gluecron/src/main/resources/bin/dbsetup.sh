############################### Creates the tables used by gluecron ##########################
#
#         Configuration is take from /opt/gluecron/conf/gluecron.conf
#         Syntax is written for MySQL Support.    
#     
#############################################################################################
abspath=$(cd ${0%/*} && echo $PWD/${0##*/})
GLUE_BIN_HOME=`dirname $abspath`

uid=$(grep db.uid $GLUE_BIN_HOME/../conf/gluecron.conf | sed -e "s/.*: //" -e "s/#.*//")
pwd=$(grep db.pwd $GLUE_BIN_HOME/../conf/gluecron.conf | sed -e "s/.*: //" -e "s/#.*//")
url=$(grep db.jdbc $GLUE_BIN_HOME/../conf/gluecron.conf | sed -e "s/.*: //" -e "s/#.*//")
db=$(grep db.name $GLUE_BIN_HOME/../conf/gluecron.conf | sed -e "s/.*: //" -e "s/#.*//")


url=${url/jdbc:mysql:\/\//}
url=${url/:3306/}
url=${url/db.jdbc=/}
uid=${uid/db.uid=/}
pwd=${pwd/db.pwd=/}
db=${db/db.name=/}



if [ -z $db ]; then
  db="glue"
fi

if [ -z $uid ]; then
  uid="root"
fi

if [ -z $url ]; then
  url="localhost"
fi

if [ -z $db ]; then
  db="glue"
fi

if [ -z $pwd ]; then
 pwd=""
else
 pwd=' -p"${pwd}"'
fi



mysql -h$url -u$uid -p$pwd $db -e 'CREATE TABLE IF NOT EXISTS hdfsfiles ( id int(11) NOT NULL AUTO_INCREMENT, path  varchar(1000) NOT NULL, ts bigint not null default 0, seen TINYINT DEFAULT 0, KEY seen1 (seen),  UNIQUE KEY path (path), PRIMARY KEY id1 (id) )'

mysql -h$url -u$uid -p$pwd $db -e 'CREATE TABLE IF NOT EXISTS hdfsfiles_history ( id int(11) NOT NULL AUTO_INCREMENT, path  varchar(1000) NOT NULL, datetime DATETIME, seen TINYINT DEFAULT 0, KEY seen1 (seen),  UNIQUE KEY path (path), PRIMARY KEY id1 (id) )'

mysql -h$url -u$uid -p$pwd $db -e 'create table IF NOT EXISTS unittriggers ( id int(11) NOT NULL AUTO_INCREMENT, unit  varchar(100), type varchar(20), data varchar(100), lastrun date, PRIMARY KEY  (id))'

mysql -h$url -u$uid -p$pwd $db -e 'create table IF NOT EXISTS unitfiles (unitid int(11), fileid int(11), status varchar(10), UNIQUE KEY a (unitid,  fileid))'

mysql -h$url -u$uid -p$pwd $db -e 'create table IF NOT EXISTS unitfiles_history (unitid int(11), fileid int(11), status varchar(10), datetime DATETIME, UNIQUE KEY a (unitid,  fileid)); CREATE INDEX unitfiles_history_index USING BTREE ON unitfiles_history(datetime);'

mysql -h$url -u$uid -p$pwd $db -e 'use glue; describe hdfsfiles; describe unittriggers; describe unitfiles; describe unitfiles_history;'


