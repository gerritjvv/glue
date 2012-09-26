---
layout: doc
title: Glue Temporary Files
permalink: tempfiles.html
category: tutorial
---


{% include nav.markdown %}

Many times workflows require to download and or write out temporary data to the local file system.

The local tmp directory is not a good place to put these files, as it might either not have sufficient space
available or data can be deleted from tmp at any time.

To write out files to the local file system a glue workflow can use the /opt/glue/log/<uuid> directory for this purpose.

The best way to do this is to use:

    def f = new File("/opt/glue/log/${context.unitId}")
    f.text = text where
