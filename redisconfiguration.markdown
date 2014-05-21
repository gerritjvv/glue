---
layout: doc
title: Glue Redis Module Configuration
permalink: redisconfiguration.html
category: tutorial
---


{% include nav.markdown %}


# Overview

The redis module is a wrapper for https://github.com/xetorthio/jedis, https://github.com/abelaska/jedis-lock.

# Configuration file

All modules are configured in the /opt/glue/conf/workflow_modules.groovy file


# Configuration Example

```groovy
redis{

  className="org.glue.modules.RedisModule"
  config{
    host = "localhost"
    port = 6379
  }

}
```



