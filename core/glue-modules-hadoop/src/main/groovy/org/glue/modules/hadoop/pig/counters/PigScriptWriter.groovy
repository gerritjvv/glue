package org.glue.modules.hadoop.pig.counters

class PigScriptWriter {

	public static String write(Load load, int parallel, Collection<Projection> projections, TypeReport typeReport, Generate generate = null){
		
		def template = "SET default_parallel 1;\n"
		
		template += "l = load '${load.path}' using ${load.function}"

	    if(load.schema){
			template += " as (${load.schema})"
		}
		
		template += ';\n'

		int filterCounter = 0
		int orderCounter = 0
		int resultCounter = 0
		def lastProjectionVars = []
		
		def initialVar = "l"
		
		if(generate){
			template += "rr = foreach l generate ${generate.expression};\n"
			initialVar = "rr"
		}
		
		
		projections.eachWithIndex{ Projection p, pCounter ->

			def lastVar = initialVar
			
			p.generates?.eachWithIndex { Generate f, fCounter ->
				def var = "r_${pCounter}_${fCounter}"
				template += "$var = foreach ${lastVar} generate ${f.expression};\n"
				lastVar = var
			}
			
			p.filters?.eachWithIndex { Filter f, fCounter ->
				def var = "f_${pCounter}_${fCounter}"
				template += "$var = FILTER ${lastVar} BY ${f.expression} PARALLEL ${parallel};\n"
				lastVar = var
			}

			def groupForEach = ""

			if(p.groups){
				def groupExpr = ""
				p.groups?.eachWithIndex { Group g, gCounter ->

					if(gCounter != 0){
						groupExpr += ","
						groupForEach += ","
					}

					if(g.function)
						groupExpr += "${g.function}(${g.column})"
					else
						groupExpr += g.column

					groupForEach += "group.${deNest(g.column)} AS ${deNest(g.alias)}"
				}

				if(p.groups?.size() > 1)
					groupExpr = "($groupExpr)"
				else{
					groupForEach = "group.\$1"
				}

				def var = "g_${pCounter}"
				template += "$var = GROUP $lastVar BY $groupExpr PARALLEL $parallel;\n"
				lastVar = var
			}//eof groups

			if(p.counters){
				def counterExpr = ""
				p.counters?.eachWithIndex { Counter c, cCounter ->
					if(cCounter != 0) counterExpr += ","

					def colName
					 
					
					if(c.column.trim().size() > 0 && !c.column.equals('*')){
						colName = c.column.contains("?") ? c.column : "\$1.${c.column}"
						if(c.function)
							counterExpr += "${c.function}($colName) as ${deNest(c.alias)}"
						else
							counterExpr += "$colName as ${deNest(c.alias)}"
						
					}else{
					    if(c.alias.trim().size() > 0)
							counterExpr += "${c.function}(\$1) as ${deNest(c.alias)}"
					    else
							counterExpr += "${c.function}(\$1)"
					}
					    
					
					
				}

				def var = "r_${pCounter}"
				if(typeReport){
					def type = new File(p.store.path).name
					template += "$var = FOREACH $lastVar GENERATE '$type' as reportType, FLATTEN(group), ${counterExpr};\n"
				}else{
					template += "$var = FOREACH $lastVar GENERATE FLATTEN(group), ${counterExpr};\n"
				}
				lastVar = var
			}
			
			if(p.orders){
				def orderExpr = ""
				p.orders.eachWithIndex { Order o, oCounter ->
					
					def var = "o_${pCounter}_${oCounter}"
					
					if(o.expression.toLowerCase().contains('parallel')){
						orderExpr = "$var = ORDER $lastVar BY ${o.expression};\n"
					}else{
						orderExpr = "$var = ORDER $lastVar BY ${o.expression} PARALLEL ${parallel};\n"
					}
					
					template += orderExpr
					lastVar = var
					
				}
				
			}

			lastProjectionVars << lastVar
			
			if(!typeReport){
				template += "rmf ${p.store.path};\n"
				template += "STORE $lastVar INTO '${p.store.path}' USING ${p.store.function};\n"
			}

		}//eof projections
		
		
		if(typeReport){
		
			template += "rmf ${typeReport.path};\n"
			def pCols = ""
			if(lastProjectionVars.size() > 1){
				template += "u = UNION ${lastProjectionVars.join(',')};\n"
				template += "u_o = ORDER u BY \$1 PARALLEL 1;\n"
				template += "STORE u_o INTO '${typeReport.path}' using PigStorage('|');\n"
			}else{
				template += "u_o = ORDER ${lastProjectionVars[0]} BY \$1 PARALLEL 1;\n"
				template += "STORE u_o INTO '${typeReport.path}' using PigStorage('|');\n"
			}
				
		}
		
		return template
	}

	/**
	 * If name is a.b then b will be returned
	 * @param var
	 * @return
	 */
	private static String deNest(String var){
		int index = var.lastIndexOf('.')

		return (index > -1) ? var[index+1..var.size()-1] : var

	}

}
