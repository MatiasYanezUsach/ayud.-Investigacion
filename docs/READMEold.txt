--------------------------------------
Título:	Hiperheurísticas para el VRPSPD
--------------------------------------

Requisitos:
	- JRE Library 1.6
	- JDK 7 (Kit de desarrollo java)
	- ECJ v21 (Plataforma basa en PG)

Opcionales:
	- Graphviz 2.38 (Graficar arboles de texto en imagen)
	- Eclipse 3.7.2 (IDE para java)
	- Windows 7 Home Premium (SO)
	- Ubuntu 12.04 LTS (SO)
	

Configuración de ejecución:
	- Evolución:	java ec.Evolve -file src/model/params/misp.params
	- Evaluación:	java ec.Evolve -file src/model/params/misp.params -p eval.problem=model.PDPProblemEva -p pop.subpop.0.size=1 -p pop.subpop.0.extra-behavior=truncate -p pop.file=$out/results/evaluation/evaluatedjobs/ListBestIndividual.in -p jobs=1 -p generations=1

Otros:
	- Para cambiar la ubicación de los archivos de lectura (instancias) editar la ruta definida:
		Para evolución: en "src/model/MISProblemEvo.java:36", que por defecto: "Instacespath =  'data/evolution'"
		Para evolución: en "src/model/MISProblemEva.java:26", que por defecto: "Instacespath =  'data/evaluation'"		
	- El parámetro de evaluación "pop.subpop.0.size" debe ser de la cantidad exacta de individuos incluidos en el archivo "data/evaluation/BestIndividual.in"
	- Para modificar otros parámetros editar alguno de los archivos en "src/model/params/"
	- Para cambiar la ubicación de los archivos de salida (estadísticas) editar la ruta definida:
		Para evolución: en "src/model/MISProblemEvo.java:35", que por defecto: "Outputpath =  'out/results/evolution'"
		Para evolución: en "src/model/MISProblemEva.java:25", que por defecto: "Outputpath = 'out/results/evaluation/'"
	
	- Resultados o achivos de la estadísticas, por cada ejecución(donde se realicen mas de una ejecución,sea X un número de ejecución):
		Proceso de Evolución, en la carpeta:[out/evolutionX/]
			job.X.BestFitness.csv:			Hoja de calculo con el mejor fitness de cada genereación
			job.X.BestIndividual.dot:		Arbol sintactico en formato dot del mejor individuo de la ejecucion.
			job.X.BestIndividual.in:		Arbol sintáctico en formato in del mejor individuo de la ejecución
			job.X.BestIndividual.png:		Arbol sintáctico en formato png del mejor individuo de la ejecución
			job.X.Estadistica_Todos.csv:	Estadísticas(mejor fitness,fitness promedio,peor fitness,tamaño promedio, mayor tamaño, menor tamaño, altura promedio y mayor altura) de cada generación
			job.X.EstadisticaProm&Mej.csv:	Estadísticas(Promedio: tamaño, ERL,ERP, fitness. Del mejor: tamaño, ERL, ERP, fitness) de cada generación
			job.X.MISPResults.out:			Log de la ejecución con toda la información de la evolución
			job.X.Semillas.csv:				Intento de guardar la semilla, sin éxito
			job.X.Statistics.out:			Lista de arboles sintácticos en formato dot, de los mejores individuos de cada generación
		
		Proceso de Evaluación, en la carpeta:[out/evaluation/]
			evaluatedjobs/:				Subcarpeta, contiene los arboles en formato dot y png, y estadísticas de todas las ejecuciones con sus generaciones sobre: ERP, ERL, fitness y tamaño.
			ListBestIndividual.in:		Lista de los arboles sintácticos en formato in de los mejores individuos de todas las ejecuciones
			BestIndividual.dot:			Arbol sintáctico en formato dot del mejor individuo de todas las ejecuciones
			BestIndividual.in:			Arbol sintáctico en formato in del mejor individuo de todas las ejecuciones
			BestIndividual.png:			Arbol sintáctico en formato png del mejor individuo de todas las ejecuciones
			Estadistica_ResumenEva.csv:	Estadísticas(ERL,ERP,fitness evolución,fitness evaluación, número de nodos, profundidad,hits=optimos obtenidos,id de ejecución, tiempo de ejecución promedio) de todos los individuos evaluados
			MISPResults.out:			Log de la ejecución de evaluación, con toda la información de la evaluación
			Statistics.out:				Arbol sintáctico en formato dot del mejor individuo de todas las ejecuciones
			
			
			
			 -Djava.library.path=C:\Program Files\IBM\ILOG\CPLEX_Studio1271\cplex\bin\x64_win64\ -p eval.problem=model.PDPProblemEva -p pop.subpop.0.size=1 -p pop.subpop.0.extra-behavior=truncate -p pop.file=$out/results/evaluation/evaluatedjobs/ListBestIndividual.in -p jobs=1 -p generations=1