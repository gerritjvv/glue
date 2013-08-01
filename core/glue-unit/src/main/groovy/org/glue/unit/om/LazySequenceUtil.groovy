package org.glue.unit.om

import clojure.lang.RT

@Typed(TypePolicy.MIXED)
class LazySequenceUtil {

	
	static{
		RT.init()
		clojure.lang.Compiler.load(new StringReader("""
 
			(ns glue)
			(defn buffered-select [f-select init-pos]
			  "Creates a lazy sequence of messages for this datasource"
			  (letfn [  
			           (m-seq [buff pos] 
			                   (let [buff2 (if (empty? buff) (f-select pos) buff)]
			                         (cons (first buff2) (lazy-seq (m-seq (rest buff2) (inc pos) )))         
			                     )
			                   )
			           ]
			    (m-seq nil init-pos)
			    )
			  )

				 (defmacro java-method-apply
					([method obj] method obj)
	      			([method obj & args] `(~method ~obj ~@args)))


             (defn closure->fn1 [cls] (fn [x]  (java-method-apply .call cls x) )) 
             
        """))
	}
	
	public static Collection seq(Closure fSelect, Number pos){
		
		return RT.var("glue", "buffered-select").invoke(
			RT.var("glue", "closure->fn1").invoke(fSelect)
			, pos)
	}
	
}
