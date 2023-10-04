# Tenpo Challenge

* Creado por: Federico Amdan (https://www.linkedin.com/in/federico-amdan/)

### Como correr proyecto:
* Se debe correr docker-compose.yml con la operación "docker-compose up" (la primera vez requiere usar "docker-compose up --build")
* Éste comando va a levantar un container de Postgres, Redis, va a correr el Dockerfile para preparar el Docker container del proyecto, y luego levantarlo.
* Para usar el servicio, se debe acceder a su swagger: http://localhost:8081/swagger-ui/index.html#/

### Cómo correr tests:
* Se pueden correr los tests usando ./gradlew test

### Decisiones tomadas en el proyecto
- El ratelimiting de 3 request por minuto, al ser un limite que tiene cada instancia de esta api, se maneja internamente con una libreria de ratelimiting, y se controla al pasar por el interceptor: "com.tenpo.apirest.interceptor.ApiInterceptor"
- Puse la cache Redis por varios motivos:
  - Para delegar la logica de controlar los 30 minutos que dura el cacheo del valor, antes de volver a solicitarlo. (Pasado ese tiempo, Redis elimina el valor cacheado automáticamente).
  - Porque si pensamos que este servicio va a tener multiples instancias, el valor cacheado debe guardarse en un lugar fuera de la propia instancia, de rapido acceso. De este modo, todas las instancias accederian a este valor cacheado, hasta que automaticamente se borre. Luego cuando alguna instancia guarde un nuevo valor, todas van a poder volver a utilizarlo sacandolo de la cache.
- El guardado del historial lo puse en un filter. Tuve que usar éste instrumento en vez del interceptor, ya que me dejaba wrappear el response con un ContentCachingResponseWrapper para luego poder leerlo.
- Tanto el ratelimiting como el guardado de historial se hacen en instrumentos ajenos al servicio CalculationService, para sacarle esas responsabilidades, poder aplicar tambien esto a otros endpoints sin duplicar codigo, y aislar el caso de uso del calculo en el servicio correspondiente. Algo similar ocurre con los ExceptionHandler en los controladores, para atrapar las excepciones y arrojarlas con su status code correspondiente.
- La lógica que controla las dependencias externas, como la llamada a la cache o al servicio externo, fue separada en una clase aparte con una interfaz, para poder independizar la capa de negocio de la de infrastructura, y asi poder reemplzar esos componentes de ser necesario, tal como se hace con los mocks en los tests.
- Los logs del proyecto los manejé con el Logger de org.slf4j, para poder diferenciar cuando eran de debug, info y error.
- Creé un mock del servicio que devuelve el porcentaje, que está incluido dentro del mismo proyecto para simplificar la solución. Está detallado en un apartado siguiente.

### Configuración de logs
Los niveles de log se controlan desde application.properties de la siguiente manera:
logging.level.<paquete/clase a chequear>=<nivel de log buscado (DEBUG/INFO/ERROR)>
Ejemplos:
  logging.level.com.tenpo.apirest.service.CalculationService=DEBUG (Habilitada para mostrar la lógica para obtener el porcentaje del external service, pasando por la cache o por la llamada al servicio)
  logging.level.com.tenpo.apirest.ratelimit=DEBUG (Habilitada para mostrar el funcionamiento del rate limit)

### External Service Mock:
* Para probar en tiempo real la llamada al servicio externo, y cambiar su comportamiento sin tener que bajar la app, preparé un mock cuyo controller está incluido dentro del mismo proyecto sólo a fines de simplificar la solución.
* Tiene dos métodos:
* GET /v1/external-service , es el que usa el servicio principal para obtener el porcentaje
* POST /v1/external-service, el cual pueexternal.service.percentage.pathde recibir uno de estos 3 posibles valores:
  - RESPONSE_AT_FIRST_ATTEMPT
  - RANDOMLY_RESPOND_OR_NOT
  - NO_RESPONSE
* En caso de querer usar otro external service (que tenga la misma estructura del que propuse aca), bastaría con cambiar la property external.service.base.url del archivo application.properties.

### Lo que no pude hacer por falta de tiempo
- Tests de integración, especialmente para probar los exception handlers
- Mejorar interacción del post del external service mock, o incluso sacarlo de este proyecto para ponerlo en uno aparte.