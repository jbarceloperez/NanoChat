package es.um.redes.nanoChat.client.application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import es.um.redes.nanoChat.client.comm.NCConnector;
import es.um.redes.nanoChat.client.shell.NCCommands;
import es.um.redes.nanoChat.client.shell.NCShell;
import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCRoomMessage;
import es.um.redes.nanoChat.messageML.NCStringListMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCController {
	// Diferentes estados del cliente de acuerdo con el autómata
	private static final byte PRE_CONNECTION = 1;
	private static final byte PRE_REGISTRATION = 2;
	private static final byte REGISTERED_OUT_ROOM = 3;
	private static final byte REGISTERED_INSIDE_ROOM = 4;
	// Código de protocolo implementado por este cliente
	// TODO Cambiar para cada grupo
	private static final int PROTOCOL = 73608090;
	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;
	// Conector para enviar y recibir mensajes con el servidor de NanoChat
	private NCConnector ncConnector;
	// Shell para leer comandos de usuario de la entrada estándar
	private NCShell shell;
	// Último comando proporcionado por el usuario
	private byte currentCommand;
	// Nick del usuario
	private String nickname;
	// Sala de chat en la que se encuentra el usuario (si está en alguna)
	private String room;
	private String newroomname;
	// Mensaje enviado o por enviar al chat
	private String chatMessage;
	// Usuario al que va dirigido el PM
	private String usuarioPrivado;
	// Dirección de internet del servidor de NanoChat
	private InetSocketAddress serverAddress;
	// Estado actual del cliente, de acuerdo con el autómata
	private byte clientStatus = PRE_CONNECTION;

	// Constructor
	public NCController() {
		shell = new NCShell();
	}

	// Devuelve el comando actual introducido por el usuario
	public byte getCurrentCommand() {
		return this.currentCommand;
	}

	// Establece el comando actual
	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	// Registra en atributos internos los posibles parámetros del comando tecleado
	// por el usuario
	public void setCurrentCommandArguments(String[] args) {
		// Comprobaremos también si el comando es válido para el estado actual del
		// autómata
		switch (currentCommand) {
		case NCCommands.COM_NICK:
			if (clientStatus == PRE_REGISTRATION)
				nickname = args[0];
			break;
		case NCCommands.COM_ENTER:
			if (clientStatus == REGISTERED_OUT_ROOM)
				room = args[0];
			break;
		case NCCommands.COM_SEND:
			if (clientStatus == REGISTERED_INSIDE_ROOM)
				chatMessage = args[0];
			break;
		case NCCommands.COM_SENDPM:
			if (clientStatus == REGISTERED_INSIDE_ROOM)
				chatMessage = args[1];
			usuarioPrivado = args[0];
			break;
		case NCCommands.COM_CHANGEROOMNAME:
			if (clientStatus == REGISTERED_INSIDE_ROOM)
				newroomname = args[0];
			break;
		default:
		}
	}

	// Procesa los comandos introducidos por un usuario que aún no está dentro de
	// una sala
	public void processCommand() throws IOException {// CUANDO ESTAMOS FUERA DE LA SALA DE CHAT Y ESCRIBIMOS UNO DE
														// ESTOS .blablabla LLEGA A ESTA FUNCION
		switch (currentCommand) {
		case NCCommands.COM_NICK: // si introduzco el comando NICK
			if (clientStatus == PRE_REGISTRATION) // y estoy en pre_registration, es decir, me he conectado al server,
													// // pero no estoy en ninguna sala
				registerNickName();
			else
				System.out.println("* You have already registered a nickname (" + nickname + ")");
			break;
		case NCCommands.COM_ROOMLIST:
			if (clientStatus == REGISTERED_OUT_ROOM) {
				getAndShowRooms();
			}
			// TODO Si no está permitido informar al usuario
			else {
				System.out.println("You are not registered in any server yet");
			}
			break;
		case NCCommands.COM_ENTER:
			if (clientStatus == REGISTERED_OUT_ROOM) {
				enterChat();
			} else {
				if (clientStatus == REGISTERED_INSIDE_ROOM) {
					System.out.println("U must leave your current room!");
				} else {
					System.out.println("U must register before entering any room!");
				}
			}
			break;
		case NCCommands.COM_QUIT:
			if (clientStatus == REGISTERED_OUT_ROOM) {
				// Cuando salimos tenemos que cerrar todas las conexiones y sockets abiertos
				ncConnector.disconnect();
				directoryConnector.close();
			} else
				System.out.println("Debes de estar registrado y fuera de una sala para dejar el servidor!");
			break;
		default:
		}
	}

	// Método para registrar el nick del usuario en el servidor de NanoChat
	private void registerNickName() {
		try {
			// Pedimos que se registre el nick (se comprobará si está duplicado)
			boolean registered = ncConnector.registerNickname(nickname);
			if (registered) {
				clientStatus = REGISTERED_OUT_ROOM;
				System.out.println("* Your nickname is now " + nickname);
			} else
				// En este caso el nick ya existía
				System.out.println("* The nickname is already registered. Try a different one.");
		} catch (IOException e) {
			System.out.println("* There was an error registering the nickname");
		}
	}

	// Método que solicita al servidor de NanoChat la lista de salas e imprime el
	// resultado obtenido
	private void getAndShowRooms() throws IOException {
		// TODO Lista que contendrá las descripciones de las salas existentes
		List<NCRoomDescription> a = new ArrayList<>();
		// TODO Le pedimos al conector que obtenga la lista de salas
		// ncConnector.getRooms()
		a = ncConnector.getRooms();
		// TODO Una vez recibidas iteramos sobre la lista para imprimir información de
		// cada sala
		for (NCRoomDescription aa : a) {
			System.out.println();
			System.out.println("Sala " + aa.roomName);

			long tiempoactual = System.currentTimeMillis();
			long tlastmessage = 0;
			if (aa.timeLastMessage != 0) {
				tlastmessage = tiempoactual - aa.timeLastMessage;
			}

			if (tlastmessage != 0) {
				long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(tlastmessage);
				System.out.println("El último mensaje fue hace " + timeSeconds + " segundos");
			} else {
				System.out.println("No se ha enviado aún ningún mensaje en esta sala");
			}

			int i = 0;
			for (String miembro : aa.members) {
				i++;
				System.out.println("[miembro " + i + "]: " + miembro);
			}
			if (i == 0)
				System.out.println("Por ahora no hay ningún miembro en esta sala!");
		}
		System.out.println();
	}

	// Método para tramitar la solicitud de acceso del usuario a una sala concreta
	private void enterChat() throws IOException {
		if (ncConnector.enterRoom(room)) {
			System.out.println("Estás dentro de la sala " + room);
			clientStatus = REGISTERED_INSIDE_ROOM;
		} else {
			System.out.println("U couldnt enter the room " + room);
			return;
		}

		do {
			// Pasamos a aceptar sólo los comandos que son válidos dentro de una sala
			readRoomCommandFromShell();
			processRoomCommand();
		} while (currentCommand != NCCommands.COM_EXIT);
		System.out.println("* Your are out of the room");
		// TODO Llegados a este punto el usuario ha querido salir de la sala, cambiamos
		// el estado del autómata
		clientStatus = REGISTERED_OUT_ROOM;
	}

	// Método para procesar los comandos específicos de una sala when estas dentro
	// de una room
	private void processRoomCommand() throws IOException { // si queremos añadir una orden de mensaje privado, habría
															// que añadir una opcion
		// con ese tipo de mensaje nuevo
		switch (currentCommand) {
		case NCCommands.COM_ROOMINFO:
			// El usuario ha solicitado información sobre la sala y llamamos al método que
			// la obtendrá
			getAndShowInfo();
			break;
		case NCCommands.COM_SEND:
			// El usuario quiere enviar un mensaje al chat de la sala
			sendChatMessage();
			break;
		case NCCommands.COM_SOCKET_IN:
			// En este caso lo que ha sucedido es que hemos recibido un mensaje desde la
			// sala y hay que procesarlo
			processIncommingMessage();
			break;
		case NCCommands.COM_EXIT:
			exitTheRoom(); // le cambiamos el CLIENTSTATUS en la función
			break;
		case NCCommands.COM_SENDPM:
			sendPrivateMessage();
			break;
		case NCCommands.COM_CHANGEROOMNAME:
			changeRoomName();
			break;
		case NCCommands.COM_HISTORY:
			showHistory();
			break;
		}
	}

	private void showHistory() throws IOException {
		ncConnector.history();
	}

	private void changeRoomName() throws IOException {
		ncConnector.changeSalaName(newroomname);
	}

	private void sendPrivateMessage() throws IOException {
		ncConnector.sendPrivateM(usuarioPrivado, chatMessage);
	}

	// Método para solicitar al servidor la información sobre una sala y para
	// mostrarla por pantalla
	private void getAndShowInfo() throws IOException {
		// TODO Pedimos al servidor información sobre la sala en concreto
		NCRoomDescription inforoom = ncConnector.getRoomInfo(room);
		System.out.println();
		System.out.println("El nombre de la sala es " + inforoom.roomName);
		long tiempoactual = System.currentTimeMillis();
		long tlastmessage = 0;
		if (inforoom.timeLastMessage != 0) {
			tlastmessage = tiempoactual - inforoom.timeLastMessage;
		}

		if (tlastmessage != 0) {
			long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(tlastmessage);
			System.out.println("El último mensaje fue hace " + timeSeconds + " segundos");
		} else {
			System.out.println("No se ha enviado aún ningún mensaje en esta sala!");
		}

		int i = 0;
		for (String miembro : inforoom.members) {
			i++;
			System.out.println("[miembro " + i + "]: " + miembro);
		}
		System.out.println();
	}

	// Método para notificar al servidor que salimos de la sala
	private void exitTheRoom() throws IOException {
		// TODO Mandamos al servidor el mensaje de salida y cambiamos estado del
		// autómata.
		ncConnector.leaveRoom(room);
		clientStatus = REGISTERED_OUT_ROOM;
		System.out.println("Has salido de la sala " + room);
	}

	// Método para enviar un mensaje al chat de la sala
	private void sendChatMessage() throws IOException {
		ncConnector.sendMessage(chatMessage);
	}

	// Método para procesar los mensajes recibidos del servidor mientras que el
	// shell estaba esperando un comando de usuario
	private void processIncommingMessage() throws IOException {
		/*
		 * Como se puede ver, la gran mayoría de mensajes son ncroommessages y
		 * simplemente he de printear lo que contienen en su campo string, me ahorro el
		 * tratar el mensaje y según su opcode printear lo correspondiente, excepto
		 * para aquellos que era necesario tratarlo si o si y no bastaba con un simple
		 * printeo, como es el caso del historial de mensajes de una sala
		 */
		NCMessage mensaje = ncConnector.rcvMessage();

		if (mensaje instanceof NCStringListMessage) {
			NCStringListMessage mensajee = (NCStringListMessage) mensaje;
			System.out.println();
			for (String aa : mensajee.getList()) {
				System.out.println(aa);
			}
			System.out.println();
		} else {
			if (mensaje.getOpcode() == NCMessage.OP_ACCEPTEDNEWROOM) {
				System.out.println("La sala no exist�a, por lo que ha sido creada!");
			}else {
				if (mensaje.getOpcode() == NCMessage.OP_NAMECHANGED) {
					this.room = this.newroomname;
				}
				System.out.println(((NCRoomMessage) mensaje).getName());
			}
			
		}

	}

	// MNétodo para leer un comando de la sala
	public void readRoomCommandFromShell() {
		// Pedimos un nuevo comando de sala al shell (pasando el conector por si nos
		// llega un mensaje entrante)
		shell.readChatCommand(ncConnector);
		// Establecemos el comando tecleado (o el mensaje recibido) como comando actual
		setCurrentCommand(shell.getCommand());
		// Procesamos los posibles parámetros (si los hubiera)
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	// Método para leer un comando general (fuera de una sala)
	public void readGeneralCommandFromShell() {
		// Pedimos el comando al shell
		shell.readGeneralCommand();
		// Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		// Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	// Método para obtener el servidor de NanoChat que nos proporcione el
	// directorio
	public boolean getServerFromDirectory(String directoryHostname) {
		// Inicializamos el conector con el directorio y el shell
		System.out.println("* Connecting to the directory...");
		// Intentamos obtener la dirección del servidor de NanoChat que trabaja con
		// nuestro protocolo
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
			serverAddress = directoryConnector.getServerForProtocol(PROTOCOL);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			serverAddress = null;
		}
		// Si no hemos recibido la dirección entonces nos quedan menos intentos
		if (serverAddress == null) {
			System.out.println("* Check your connection, the directory is not available.");
			return false;
		} else
			return true;
	}

	// Método para establecer la conexión con el servidor de Chat (a través del
	// NCConnector)
	public boolean connectToChatServer() {
		try {
			// Inicializamos el conector para intercambiar mensajes con el servidor de
			// NanoChat (lo hace la clase NCConnector)
			ncConnector = new NCConnector(serverAddress);
		} catch (IOException e) {
			System.out.println("* Check your connection, the game server is not available.");
			serverAddress = null;
		}
		// Si la conexión se ha establecido con éxito informamos al usuario y
		// cambiamos
		// el estado del autómata
		if (serverAddress != null) {
			System.out.println("* Connected to " + serverAddress);
			clientStatus = PRE_REGISTRATION;
			return true;
		} else
			return false;
	}

	// Método que comprueba si el usuario ha introducido el comando para salir de
	// la
	// aplicación
	public boolean shouldQuit() {
		if (clientStatus == REGISTERED_OUT_ROOM) {
			return currentCommand == NCCommands.COM_QUIT;
		} else
			return false;
	}

}
