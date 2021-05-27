/**
 * 
 */
package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCOpcodeMessage;
import es.um.redes.nanoChat.messageML.NCRoomMessage;


public class Manager extends NCRoomManager {
	Map<String, Socket> users;
	long tlastmessage = 0;
	List<String> SavedMessages = new LinkedList<String>();
	boolean just_created;

	public Manager(String roomName) {
		this.roomName = roomName;
		this.users = new HashMap<String, Socket>();
	}
	public List<String> showMessageHistory() throws IOException {
		return this.SavedMessages;
	}
	@Override
	public boolean registerUser(String u, Socket s) {
		this.users.put(u, s);
		return true;
	}
	public boolean getJust_created() {
		return just_created;
	}
	public void setJust_created(boolean a) {
		just_created = a;
	}
	public void broadcastLeavingMessage(String user) throws IOException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime now = LocalDateTime.now();
		for (Socket s : users.values()) {
			if (!(users.get(user) == s)) {
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				NCRoomMessage mensaje = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_USERLEFT,
						"[" + dtf.format(now) + "] El usuario " + user + " se ha marchado de la sala!");
				String rawMessage = mensaje.toEncodedString();
				dos.writeUTF(rawMessage);
			}
		}
	}
	public void broadcastEnteringMessage(String user) throws IOException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime now = LocalDateTime.now();
		for (Socket s : users.values()) {
			if (!(users.get(user) == s)) {
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				NCRoomMessage mensaje = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_USERENTERED,
						"[" + dtf.format(now) + "] El usuario " + user + " se ha unido a la sala!");
				String rawMessage = mensaje.toEncodedString();
				dos.writeUTF(rawMessage);
			}
		}
	}
	
	public void just_created(String u,Socket s) throws IOException {
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		NCOpcodeMessage mensaje = (NCOpcodeMessage) NCMessage.makeOpcodeMessage(NCMessage.OP_ACCEPTEDNEWROOM);
		String rawMessage = mensaje.toEncodedString();
		dos.writeUTF(rawMessage);
	}
	
	@Override
	public void broadcastMessage(String u, String message) throws IOException {
		this.SavedMessages.add("["+u+"] "+message);
		this.tlastmessage = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime now = LocalDateTime.now();
		for (Socket s : users.values()) {
			if (!(users.get(u) == s)) {
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				NCRoomMessage mensaje = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_RCVMESSAGE,
						"[" + dtf.format(now) + "][" + u + "]: " + message);
				String rawMessage = mensaje.toEncodedString();
				dos.writeUTF(rawMessage);
			}
		}
	}

	public void broadcastPM(String u, String usuarioreceptor, String message) throws IOException {
		this.tlastmessage = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime now = LocalDateTime.now();
		if (users.containsKey(usuarioreceptor)) {
			Socket s = users.get(usuarioreceptor);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			NCRoomMessage mensaje = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_SENT,
					"[PRIVADO][" + dtf.format(now) + "][" + u + "]: " + message);
			String rawMessage = mensaje.toEncodedString();
			dos.writeUTF(rawMessage);
		} else {
			Socket s = users.get(u);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			NCRoomMessage mensaje = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_NOTSENT,
					"No ha llegado al destinatario, revisa que este exista!");
			String rawMessage = mensaje.toEncodedString();
			dos.writeUTF(rawMessage);
		}
	}

	@Override
	public void removeUser(String u) {
		this.users.remove(u);

	}

	@Override
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	@Override
	public NCRoomDescription getDescription() {
		List<String> a = new ArrayList<>(users.keySet());
		NCRoomDescription descripcion = new NCRoomDescription(this.roomName, a, tlastmessage);
		return descripcion;
	}

	@Override
	public int usersInRoom() {
		return this.users.size();
	}

}
