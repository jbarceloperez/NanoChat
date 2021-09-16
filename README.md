# NanoChat
 
Aplicación Java cuya funcionalidad es la conexión de clientes a un servidor de chat, haciendo uso de un
intermediario que es un directorio o servidor con las listas de chats que gestionará las peticiones de
los clientes. La aplicación gestiona los protocolos UDP y TCP para las comunicaciones entre clientes,
servidores y el directorio.

## Funcionamiento de la aplicación

Las diferentes comunicaciones se llevan a cabo  a través de algunas funciones, algunas de las más importantes son:

<!-- markdownlint-disable MD013 -->
| Función                                 | Acción                               |
| --------------------------------------- | ------------------------------------ |
| **getServerForProtocol()**                 | Es usada por los clientes, le envían un mensaje de petición de información sobre cierto servidor de chat al Directory, y este último deberá de buscar en su lista de servidores de chat el que el cliente ha pedido(usando el protocol que se pasa como argumento), en el caso de que ese servidor exista pues le mandarán la información necesaria(sendServerInfo), y este obtendrá su IP mediante la función getAddressFromResponse().|
| **registerServerForProtocol()**             | Es usada por los servidores de chat y como su nombre indica, dado un protocolo intentarán registrar el servidor de chat en el server Directory, que se registrará satisfactoriamente(sendOK) a no ser que este ya esté registrado. |
| **processRequestFromClient()**              | Es la función que usa el Directory para ir analizando y tratando los distintos tipos de mensaje y peticiones que le envían tanto clientes como servidores de chat.|
|||
<!-- markdownlint-enable MD013 -->

A continuación se hace un breve resumen de cómo funcionan el directorio, el servidor de chat y el cliente de chat con sus respectivos autómatas de funcionamiento.

### Funcionamiento del servidor de directorio

El servidor del directorio se inicializa y queda esperando a la llegada de una solicitud de un servidor o de un cliente. Este estado inicial recibe el nombre de qWAIT, y además es el único estado final del autómata. 
Una vez que se encuentra en este estado, puede pasar a dos estados diferentes: cuando se recibe una petición de un cliente de chat de obtener información de un servidor (rcv(getServer), se pasa al estado q3. Entonces se revisa el conjunto de los servidores almacenados, y se vuelve al estado qWAIT con la información(send(info)) o con un mensaje en el que se especifica que el servidor no se encuentra registrado en el directorio (send(empty)).

Por otro lado, cuando en el estado qWAIT se recibe una solicitud de un servidor de chat para ser registrado (rcv(registerServer)) se pasa al estado q2. En este estado se intenta registrar el servidor. Si ya existe, se retorna al estado qWAIT con la denegación del registro (send(denied)). Sin embargo, cuando esto no sucede el servidor se registra y se devuelve la confirmación de esto (send(confirmed)).



<p align="center">
  <img width="320" src="https://raw.githubusercontent.com/jbarceloperez/NanoChat/main/doc/directorio.png" alt="Autómata del directorio">
</p>
<h5 align="center">Autómata del directorio</h5>

### Funcionamiento del cliente de chat

El cliente de chat tiene un estado inicial q1. Este estado además es final. Cuando el cliente decide enviar una solicitud al directorio para obtener la dirección de un servidor (send(query)), el autómata pasa al estado q2. En este estado pueden suceder diferentes cosas: si obtenemos una respuesta del directorio, volveríamos al estado inicial con la información del servidor (rcv(info)) o, en caso de no estar registrado en el directorio, con un mensaje que indica que el servidor de chat no está registrado (rcv(empty)).

Sin embargo, si se excede un tiempo máximo preestablecido (el timeout), el autómata pasa a un siguiente estado q3 idéntico en el que se vuelve a solicitar al directorio la información. El autómata está simplificado, pero este proceso se repetiría 10 veces en caso de error, y si el décimo también excede el timeout, el autómata llega al estado qerr, estado “trampa” al que se llega tras exceder todos los timeouts del que no se puede salir y es final.



<p align="center">
  <img width="500" src="https://raw.githubusercontent.com/jbarceloperez/NanoChat/main/doc/cliente.png" alt="Autómata del cliente">
</p>
<h5 align="center">Autómata del cliente</h5>



### Funcionamiento del servidor de chat

El servidor de chat tiene un estado inicial q1 que además también es su único estado final. En este estado inicial se pueden mandar solicitudes al directorio para ser registrados en él (snd(registration)), pasando de esta manera al esta q2 del autómata. En este estado del autómata se puede retornar al estado inicial con la confirmación del registro (rcv(confirmed)) o, en caso de hallarse ya en el directorio, el mensaje de denegación del registro (rcv(denied)).

Sin embargo, si se excede el timeout preestablecido, se pasará a un estado q3 en el que se volverá a enviar la solicitud de registro al directorio retornando de esta manera a q2.


<p align="center">
  <img width="400" src="https://raw.githubusercontent.com/jbarceloperez/NanoChat/main/doc/server.png" alt="Autómata del servidor">
</p>
<h5 align="center">Autómata del servidor</h5>


## Formato de los mensajes


