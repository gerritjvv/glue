set job.priority VERY_HIGH
set job.name 'lalala - $banners_location'
b = LOAD '$banners_location' as (
        bid: chararray,
        nid: int
   );
   
ns = group b by nid;
result = foreach ns generate group, COUNT($1);
dump result;