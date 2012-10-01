---
layout: doc
title: Want To Giveup
permalink: wanttogiveup.html
category: tutorial
---


{% include nav.markdown %}

# Stay calm
 Workflows are difficult to get write no matter what the language or process, some just make is easier while others
 especially when using xml and or bpel make it near to impossible.
 
 Remember that you are using multiple systems and processes at the same time, so take things one step at a time.
 
 
# Standup take a walk breath and come back.
 
  Just banging without sense on the keyboard is not going to get you anywhere. 
  Have a break, no matter what the frustrations and time-lines, standup refresh, walk around, these help your brain do some house keeping work that it needs,
  and is most of the times what is impeding you from seeing the problem starting you in the face.
  
# Design
 
 Take up a piece of paper (digital or real) and write the flow of what your trying to achieve. 
 If you can't write it out on paper, forget about writing it in any language. 
 
 # System Configuration
  
  Having a correctly configured system is vital to avoid major frustrations in workflows, also having a local development VM (fast not slow), 
  help speed up workflow development.
  
 
  
 # Check the /opt/glue/logs/serverlog.log
 
   This file contains all of the output good and bad of what is going on in the Glue Rest Server.
   
   
 # Check the Workflow Syntax with groovy
 
   All workflow are groovy files, check the syntax using groovy <workflow>.groovy
   
   if you get "method not found or similar" then the workflow is free of syntax errors,
   method not found etc is due to the DSL used and the groovy executable does not interpret this in the domain context.
    
   
   
   
   
   