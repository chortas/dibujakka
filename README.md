# Dibujakka  
## Teoría del Lenguaje - FIUBA
### Pictionary server en Scala con akka toolkit

**Integrantes:** \
Robinson Fang (rfang@fi.uba.ar)\
Cecilia Hortas (chortas@fi.uba.ar)\
Juan Pablo Rombolá (jrombola@fi.uba.ar)\
Ariel Vergara (avergara@fi.uba.ar)

**Video de Scala:** https://www.youtube.com/watch?v=MPnKMAPqb6o  

**Link al juego**: http://dibujakka.herokuapp.com

## Trabajo práctico
La propuesta del trabajo era elegir un lenguaje de programación y hacer un análisis completo del mismo, pasando por el origen, los usos, la sintaxis, paradigmas soportados y más tópicos. Además tuvimos que elegir un proyecto e implementarlo en el lenguaje elegido. Nosotros decidimos hacer un pictionary online multijugador, aprovechando la facilidad de manejo de concurrencia que nos provee akka, una librería de Scala, usando el modelo de actores.  
Para el front-end decidimos usar Vue.js lo que nos permitió conseguir vistas bien logradas en poco tiempo. El repositorio es https://github.com/JuampiRombola/dibujakka-web

## El juego
![dibujakka3](https://user-images.githubusercontent.com/11811232/126231802-85f257c7-a657-4528-a9b4-35367384f92c.PNG)

---  

![dibujakka2](https://user-images.githubusercontent.com/11811232/126231905-7260e2df-f7b5-4b7a-be05-08a0858e0565.PNG)

---

### Running the project
Run the application from a console:

Enter ```./sbt``` on OSX/Linux or ```sbt.bat``` on Windows, sbt downloads project dependencies.  
The ```>``` prompt indicates sbt has started in interactive mode.

At the sbt prompt, enter ```reStart```.
