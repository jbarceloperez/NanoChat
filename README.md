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
| getServerForProtocol()                  | Es usada por los clientes, le envían un mensaje de petición de información sobre cierto servidor de chat al Directory, y este último deberá de buscar en su lista de servidores de chat el que el cliente ha pedido(usando el protocol que se pasa como argumento), en el caso de que ese servidor exista pues le mandarán la información necesaria(sendServerInfo), y este obtendrá su IP mediante la función getAddressFromResponse().|
| registerServerForProtocol()             | Es usada por los servidores de chat y como su nombre indica, dado un protocolo intentarán registrar el servidor de chat en el server Directory, que se registrará satisfactoriamente(sendOK) a no ser que este ya esté registrado. |
| processRequestFromClient()              | Es la función que usa el Directory para ir analizando y tratando los distintos tipos de mensaje y peticiones que le envían tanto clientes como servidores de chat.|
<!-- markdownlint-enable MD013 -->

## Formato de los mensajes


