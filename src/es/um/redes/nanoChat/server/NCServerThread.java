package es.um.redes.nanoChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import es.um.redes.nanoChat.messageML.*;
import es.um.redes.nanoChat.server.roomManager.*;

/**
 * A new thread runs for each connected client
 */
public class NCServerThread extends Thread {

	private Socket socket = null;
	// Manager global compartido entre los Threads
	private NCServerManager serverManager = null;
	// Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	// Usuario actual al que atiende este Thread
	String user;
	// RoomManager actual (dependerá de la sala a la que entre el usuario)
	NCRoomManager roomManager;
	// Sala actual
	String currentRoom;
	private NCServerManager manager;

	// Inicialización de la sala
	public NCServerThread(NCServerManager manager, Socket socket) throws IOException {
		super("NCServerThread");
		this.manager = manager;
		this.socket = socket;
		this.serverManager = manager;
	}

	// Main loop
	public void run() {
		try {
			// Se obtienen los streams a partir del Socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			// En primer lugar hay que recibir y verificar el nick
			receiveAndVerifyNickname(); // de aqui no salimos hasta que el cliente indique un nick valido
			
			// Mientras que la conexión esté activa entonces...
			while (true) {
				// TODO Obtenemos el mensaje que llega y analizamos su código de operación
				NCMessage message = NCMessage.readMessageFromSocket(dis); 
				switch (message.getOpcode()) { 
				// TODO 1) si se nos pide la lista de salas se envía llamando a sendRoomList();
				case NCMessage.OP_LIST_ROOMS:
				{
					sendRoomList();
					break;
				}
				case NCMessage.OP_QUIT:
				{
					this.serverManager.removeUser(user);
					break;
				}
				case NCMessage.OP_ENTER:
				{
					String sala = ((NCRoomMessage) message).getName();
					NCRoomManager rm = serverManager.enterRoom(user,sala,socket);	//en el entrar sala manda el ACCEPTED o el DENIED
						if (rm != null) {
							NCOpcodeMessage messagee = (NCOpcodeMessage) NCMessage.makeOpcodeMessage(NCMessage.OP_ACCEPTED);
							String infoo = messagee.toEncodedString();
							dos.writeUTF(infoo);
							currentRoom = sala;
							roomManager = rm;
							roomManager.broadcastEnteringMessage(user);
							if (roomManager.getJust_created()) {
								roomManager.setJust_created(false);
								roomManager.just_created(user,socket);
							}
							processRoomMessages();
						}else {
							NCOpcodeMessage nope = (NCOpcodeMessage) NCMessage.makeOpcodeMessage(NCMessage.OP_DENIED);
							String infooo = nope.toEncodedString();
							dos.writeUTF(infooo);
							break;
						}
						break;
				
				}
				}
			}
		} catch (Exception e) {
			// If an error occurs with the communications the user is removed from all the
			// managers and the connection is closed
			System.out.println("* User " + user + " disconnected.");
			serverManager.leaveRoom(user, currentRoom);
			serverManager.removeUser(user);
		} finally {
			if (!socket.isClosed())
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
	}
	
	// Obtenemos el nick y solicitamos al ServerManager que verifique si está
	// duplicado
	private void receiveAndVerifyNickname() throws IOException {
		// TODO Entramos en un bucle hasta comprobar que alguno de los nicks
		// proporcionados no está duplicado
		while (true) {
			NCRoomMessage nick = (NCRoomMessage) NCMessage.readMessageFromSocket(dis);

			if (nick.getOpcode() == NCMessage.OP_NICK) {
				this.user = nick.getName();
				

				if (serverManager.addUser(user)) {
					NCOpcodeMessage message = (NCOpcodeMessage) NCMessage.makeOpcodeMessage(NCMessage.OP_NICK_OK);
					String nick_ok = message.toEncodedString();
					dos.writeUTF(nick_ok);
					break;

				} else {
					NCOpcodeMessage message = (NCOpcodeMessage) NCMessage
							.makeOpcodeMessage(NCMessage.OP_NICK_DUPLICATED);
					String nick_duplicated = message.toEncodedString();
					dos.writeUTF(nick_duplicated);
				}
			}
		}
	}

	// Mandamos al cliente la lista de salas existentes
	private void sendRoomList() throws IOException {
		// TODO La lista de salas debe obtenerse a partir del NCServermanager con el
		// getroomlist() y después
		// enviarse mediante su mensaje correspondiente
		List<NCRoomDescription> infosalas = serverManager.getRoomList();
		NCListMessage message = (NCListMessage) NCMessage.makeListMessage(NCMessage.OP_ROOMS_LISTED, infosalas);
		String info = message.toEncodedString(); // mensaje codificado por el servidor, ya podemos mandarlo!!
		dos.writeUTF(info);
	}

	private void processRoomMessages() throws IOException {
		// TODO Comprobamos los mensajes que llegan hasta que el usuario decida salir de
		// la sala
		boolean exit = false;
		while (!exit) {
			// TODO Se recibe el mensaje enviado por el usuario
			NCMessage message = NCMessage.readMessageFromSocket(dis); 
			switch (message.getOpcode()) { 
				case NCMessage.OP_LEAVE:
				{	
					roomManager.broadcastLeavingMessage(user);
					serverManager.leaveRoom(user,currentRoom);
					roomManager.removeUser(user);
					if(roomManager.usersInRoom()==0) {
						serverManager.removeRoom(currentRoom);
					}
					exit = true;
					break;
				}
				case NCMessage.OP_ROOMINFO:
				{
					NCRoomDescription rm = roomManager.getDescription();
					NCInfoMessage messagee = (NCInfoMessage) NCMessage.makeInfoMessage(NCMessage.OP_INFOLISTED,rm);
					String info = messagee.toEncodedString(); // mensaje codificado por el servidor, ya podemos mandarlo!!
					dos.writeUTF(info);
					break;
				}
				case NCMessage.OP_SENDMESSAGE:
					String mensaje = ((NCRoomMessage)message).getName();
					roomManager.broadcastMessage(user,mensaje);
					break;
				case NCMessage.OP_SENDPM:
					String usuario = ((NCPmMessage)message).getUser();
					String mensajee = ((NCPmMessage)message).getMsg();
					roomManager.broadcastPM(user,usuario, mensajee);
					break;
				case NCMessage.OP_CHANGEROOMNAME:
					String room = ((NCRoomMessage)message).getName();
					String aux = currentRoom;
					if(serverManager.changeRoomName(aux,room)) {
						currentRoom = room;
						roomManager.setRoomName(room);
						NCRoomMessage messagee = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_NAMECHANGED,"Nombre cambiado satisfactoriamente");
						String info = messagee.toEncodedString();
						dos.writeUTF(info);
					}else {
						NCRoomMessage messagee = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_NAMENOTCHANGED,"Ya existe una sala con ese nombre!");
						String info = messagee.toEncodedString();
						dos.writeUTF(info);
					}
					
					break;
				case NCMessage.OP_HISTORY:
					List<String> aa = roomManager.showMessageHistory();
					if (aa.isEmpty()) {
						NCRoomMessage messagee = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_HISTORYPRINTED,"No ha habido ningún mensaje aún en el servidor");
						String info = messagee.toEncodedString();
						dos.writeUTF(info);
					}else {
						NCStringListMessage messagee = (NCStringListMessage) NCMessage.makeStringListMessage(NCMessage.OP_HISTORYPRINTED,aa);
						String info = messagee.toEncodedString();
						dos.writeUTF(info);
					}
			}
		}
	}
}
