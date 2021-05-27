package es.um.redes.nanoChat.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import es.um.redes.nanoChat.messageML.*;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor de NanoChat
public class NCConnector {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;

	public NCConnector(InetSocketAddress serverAddress) throws UnknownHostException, IOException {
		socket = new Socket(serverAddress.getAddress(), serverAddress.getPort());
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
	}

	// Método para registrar el nick en el servidor. Nos informa sobre si la
	// inscripción se hizo con éxito o no.
	public boolean registerNickname(String nick) throws IOException {
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_NICK, nick);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);

		NCOpcodeMessage response = (NCOpcodeMessage) NCMessage.readMessageFromSocket(dis);
		return (response.getOpcode() == NCMessage.OP_NICK_OK);
	}

	// Método para obtener la lista de salas del servidor
	public List<NCRoomDescription> getRooms() throws IOException {
		NCOpcodeMessage message = (NCOpcodeMessage) NCMessage.makeOpcodeMessage(NCMessage.OP_LIST_ROOMS);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		
		NCListMessage response = (NCListMessage) NCMessage.readMessageFromSocket(dis);
		return response.getList();
		
	}
	public void changeSalaName(String room) throws IOException {
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_CHANGEROOMNAME,room);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
	}
	// Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException {
		// Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or RCV(REJECT)
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_ENTER,room);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		
		NCOpcodeMessage response = (NCOpcodeMessage) NCMessage.readMessageFromSocket(dis);
		return (response.getOpcode() == NCMessage.OP_ACCEPTED ||response.getOpcode() == NCMessage.OP_ACCEPTEDNEWROOM);
	}
	public void history() throws IOException {
		NCOpcodeMessage message = (NCOpcodeMessage) NCMessage.makeOpcodeMessage(NCMessage.OP_HISTORY);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
	}
	// Método para salir de una sala
	public void leaveRoom(String room) throws IOException {
		NCOpcodeMessage message = (NCOpcodeMessage) NCMessage.makeOpcodeMessage(NCMessage.OP_LEAVE);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		
	}

	// Método que utiliza el Shell para ver si hay datos en el flujo de entrada
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}

	// IMPORTANTE!!
	// TODO Es necesario implementar métodos para recibir y enviar mensajes de chat
	// a una sala
	public void sendMessage(String mensaje) throws IOException {
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_SENDMESSAGE,mensaje);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
	}
	public void sendPrivateM(String usuario,String mensaje) throws IOException {
		NCPmMessage message = (NCPmMessage) NCMessage.makePmMessage(NCMessage.OP_SENDPM,usuario,mensaje);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
	}
	
	public NCMessage rcvMessage() throws IOException {
		NCMessage response =NCMessage.readMessageFromSocket(dis);
		return response;
	}
	// Método para pedir la descripción de una sala
	public NCRoomDescription getRoomInfo(String room) throws IOException {
		// Funcionamiento resumido: SND(GET_ROOMINFO) and RCV(ROOMINFO)
		NCOpcodeMessage message = (NCOpcodeMessage) NCMessage.makeOpcodeMessage(NCMessage.OP_ROOMINFO);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		
		NCInfoMessage response = (NCInfoMessage) NCMessage.readMessageFromSocket(dis);
		return response.getInfo();
	}

	// Método para cerrar la comunicación con la sala
	// TODO (Opcional) Enviar un mensaje de salida del servidor de Chat
	public void disconnect() {
		try {
			NCOpcodeMessage message = (NCOpcodeMessage) NCMessage.makeOpcodeMessage(NCMessage.OP_QUIT);
			String rawMessage = message.toEncodedString();
			dos.writeUTF(rawMessage);
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
		} finally {
			socket = null;
		}
	}

}
