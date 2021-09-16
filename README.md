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

<!-- markdownlint-enable MD013 -->

A continuación se hace un breve resumen de cómo funcionan el directorio, el servidor de chat y el cliente de chat con sus respectivos autómatas de funcionamiento.

### ___Funcionamiento del servidor de directorio___

El servidor del directorio se inicializa y queda esperando a la llegada de una solicitud de un servidor o de un cliente. Este estado inicial recibe el nombre de qWAIT, y además es el único estado final del autómata. 
Una vez que se encuentra en este estado, puede pasar a dos estados diferentes: cuando se recibe una petición de un cliente de chat de obtener información de un servidor (rcv(getServer), se pasa al estado q3. Entonces se revisa el conjunto de los servidores almacenados, y se vuelve al estado qWAIT con la información(send(info)) o con un mensaje en el que se especifica que el servidor no se encuentra registrado en el directorio (send(empty)).

Por otro lado, cuando en el estado qWAIT se recibe una solicitud de un servidor de chat para ser registrado (rcv(registerServer)) se pasa al estado q2. En este estado se intenta registrar el servidor. Si ya existe, se retorna al estado qWAIT con la denegación del registro (send(denied)). Sin embargo, cuando esto no sucede el servidor se registra y se devuelve la confirmación de esto (send(confirmed)).



<p align="center">
  <img width="320" src="https://raw.githubusercontent.com/jbarceloperez/NanoChat/main/doc/directorio.png" alt="Autómata del directorio">
</p>
<h5 align="center">Autómata del directorio</h5>

### ___Funcionamiento del cliente de chat___

El cliente de chat tiene un estado inicial q1. Este estado además es final. Cuando el cliente decide enviar una solicitud al directorio para obtener la dirección de un servidor (send(query)), el autómata pasa al estado q2. En este estado pueden suceder diferentes cosas: si obtenemos una respuesta del directorio, volveríamos al estado inicial con la información del servidor (rcv(info)) o, en caso de no estar registrado en el directorio, con un mensaje que indica que el servidor de chat no está registrado (rcv(empty)).

Sin embargo, si se excede un tiempo máximo preestablecido (el timeout), el autómata pasa a un siguiente estado q3 idéntico en el que se vuelve a solicitar al directorio la información. El autómata está simplificado, pero este proceso se repetiría 10 veces en caso de error, y si el décimo también excede el timeout, el autómata llega al estado qerr, estado “trampa” al que se llega tras exceder todos los timeouts del que no se puede salir y es final.



<p align="center">
  <img width="500" src="https://raw.githubusercontent.com/jbarceloperez/NanoChat/main/doc/cliente.png" alt="Autómata del cliente">
</p>
<h5 align="center">Autómata del cliente</h5>



### ___Funcionamiento del servidor de chat___

El servidor de chat tiene un estado inicial q1 que además también es su único estado final. En este estado inicial se pueden mandar solicitudes al directorio para ser registrados en él (_snd(registration_)), pasando de esta manera al esta q2 del autómata. En este estado del autómata se puede retornar al estado inicial con la confirmación del registro (_rcv(confirmed)_) o, en caso de hallarse ya en el directorio, el mensaje de denegación del registro (_rcv(denied)_).

Sin embargo, si se excede el timeout preestablecido, se pasará a un estado q3 en el que se volverá a enviar la solicitud de registro al directorio retornando de esta manera a q2.


<p align="center">
  <img width="400" src="https://raw.githubusercontent.com/jbarceloperez/NanoChat/main/doc/server.png" alt="Autómata del servidor">
</p>
<h5 align="center">Autómata del servidor</h5>


## Formato de los mensajes

Se han utilizado diversos formatos de mensajes durante la elaboración del nano-chat. Estos formatos se han elaborado para funcionar sobre los dos protocolos de nivel de transporte que había que utilizar: UDP para los mensajes encargados de comunicar el directorio con los clientes y servidores del chat, y TCP para el mecanismo de comunicación entre el cliente y el servidor de chat.

### ___Mensajes de UDP___

A continuación se presentan y explican brevemente los mensajes creados para la comunicación entre el directorio en la clase _DirectoryThread_ y el cliente o servidor de chat con la clase _DirectoryConnector_.

- __Mensaje de consulta del cliente:__ Tiene el código de operación _opcode_ = 0. Es el mensaje usado por el cliente de chat para consultar al directorio la dirección de un servidor asociada a cierto protocolo (_getServerForProtocol_). Ocupa 5 bytes, 1 para el _opcode_ y 4 para el protocolo (que es un entero).
- __Mensaje de registro del servidor:__ Tiene el código de operación _opcode_ = 1. Es el mensaje usado por el servidor de chat para solicitar su registro en el _HashMap_ que almacena en el directorio los diferentes servidores asociados a los protocolos (_registerServerForProtocol_). Ocupa 9 bytes, uno para el _opcode_, 4 para el protocolo y 4 para el puerto del servidor.
- __Mensaje de confirmación/denegación del servidor:__ Tiene el código de operación 2. Este mensaje es la respuesta del directorio a la solicitud de registro del servidor (_sendOK_), y ocupa 2 bytes, uno para el _opcode_ y otro para un byte _b_ que está programado para poder tener dos estados posibles. El valor b puede tomar el valor 1 en el que se confirma la solicitud cuando el protocolo del servidor no tiene asociado ningún servidor en el directorio y de esta manera se confirma el registro; o puede tomar el valor 0 cuando ya existe un servidor asociado al protocolo denegando de esta manera el registro.
- __Mensaje de información del servidor:__ Tiene el _opcode_ = 3. Es el mensaje que responde el directorio a una solicitud del cliente _getServerForProtocol()_ cuando existe el servidor en el directorio (_sendServerInfo_).  Ocupa 9 bytes, 1 para el _opcode_, 4 para la dirección IP en formato de array de bytes y 4 para el puerto del servidor (entero).
- __Mensaje de no existencia del servidor:__ Tiene el código de operación 4. Este mensaje complementa al anterior, ya que es el mensaje que se manda cuando para la solicitud mencionada del cliente no hay servidor registrado (_sendEmpty_). Consta de un solo byte, el del opcode. 


### ___Mensajes una vez conectado al servidor de chat___

A continuación se presentan los diferentes formatos de mensajes utilizados para la comunicación entre cliente y servidor, referente a las tareas del boletín 6. Se ha utilizado lenguaje de marcas. Posteriormente se encuentran los mensajes de respuesta.

### Mensajes del cliente

| Mensaje                  | Opcode      | Comando            | Tipo de mensaje  |
| -------------------------| ----------- | ------------------ | ---------------- |
|Registrarte en el servidor con un nick|3|“nick nombre”(__COM_NICK__)|NCRoomMessage|
|Intentar entrar a una sala|2|“enter nombre”(__COM_ENTER__)|NCRoomMessage|
|Pedir una lista de las salas existentes en el servidor|1|“roomlist”(__COM_ROOMLIST__)|NCRoomMessage|
|Enviar un mensaje de chat al servidor |4|“send mensaje”(__COM_SEND__)|NCRoomMessage|
|Salir de una sala del servidor de chat|5|“exit”(__COM_EXIT__)|NCOpcodeMessage|
|Pedir información acerca de la sala en la que te encuentras|7|“roominfo”(__COM_ROOMINFO__)|NCOpcodeMessage|
|Salir del servidor de chat|8|“quit”(__COM_QUIT__)||
|Pedir ayuda acerca de los comandos disponibles|9|“help”(__COM_HELP__)|NCPmMessage|
|Enviar un mensaje privado a un usuario de tu sala|10|“sendpm user mensaje”(__COM_SENDPM__)|NCRoomMessage|
|Cambiar el nombre de la sala|11|”changeroomname nombre”(__COM_CHANGEROOMNAME__)|NCRoomMessage|
|Esperar una supuesta recepción de mensaje en la sala|12|" "(__COM_SOCKET_IN__)||

### Mensajes de respuesta

| Mensaje                  | Opcode      | Código de opcode| Tipo de mensaje  |
| -------------------------| ----------- | ----------------| ---------------- |
|El nick ha sido aceptado|2|__OP_NICK_OK__|NCOpcodeMessage|
|El nick ha sido rechazado|2|__OP_NICK_DUPLICATED__|NCOpcodeMessage|
|El cliente ha sido aceptado en la sala|21|__OP_ACCEPTED__|NCOpcodeMessage|
|El cliente no ha sido aceptado en la sala|22|__OP_DENIED__|NCOpcodeMessage|
|El cliente “x” ha salido de la sala|25|__OP_USERLEFT__|NCRoomMessage|
|El cliente “x” ha entrado en la sala|26|__OP_USERENTERED__|NCRoomMessage|
|Se ha cambiado el nombre de la sala|27|__OP_NAMECHANGED__|NCRoomMessage|
|La lista de mensajes que tiene guardados la sala|28|__OP_NAMENOTCHANGED__|NCRoomMessage|
|La lista de la información de las salas|51|__OP_INFOLISTED__|NCListMessage|
|El mensaje que envía un usuario por la sala|61|__OP_RCVMESSAGE__|NCRoomMessage|
|El mensaje privado ha sido enviado satisfactoriamente|63|__OP_SENT__|NCRoomMessage|
|El mensaje privado no se ha podido enviar|64|__OP_NOTSENT__|NCRoomMessage|
|La lista de mensajes que tiene guardados la sala|66|__OP_HISTORYPRINTED__|NCStringListMessage|