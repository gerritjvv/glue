#!/usr/bin/env bash
#Creates the hadoop user and group
#see http://fedoraproject.org/wiki/Packaging%3aUsersAndGroups
#safety for when the commands to not exist
type getent || exit 0
						 
getent group hadoop >/dev/null || groupadd -r hadoop
       getent passwd hadoop >/dev/null || \
       useradd -r -g hadoop -d HOMEDIR -s /sbin/nologin \
       -c "Glue Cron runs under the hadoop user and group" hadoop
exit 0
