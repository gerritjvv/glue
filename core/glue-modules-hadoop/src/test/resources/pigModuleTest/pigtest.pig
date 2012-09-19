a = LOAD '$INPUT' as (p:chararray, n:int);
g = group a by p;
r = foreach g generate group;
 dump r;