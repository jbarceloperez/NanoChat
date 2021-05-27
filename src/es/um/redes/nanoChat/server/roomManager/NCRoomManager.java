package es.um.redes.nanoChat.server.roomManager;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public abstract class NCRoomManager {
	String roomName;
	List<String> members;
	boolean just_created;

	//Método para registrar a un usuario u en una sala (se anota también su socket de comunicación)
	public abstract boolean registerUser(String u, Socket s);
	//Método para hacer llegar un mensaje enviado por un usuario u
	public abstract void broadcastMessage(String u, String message) throws IOException;
	public abstract void broadcastLeavingMessage(String user) throws IOException;
	public abstract void broadcastEnteringMessage(String user) throws IOException;
	public abstract List<String> showMessageHistory() throws IOException;
	//Método para eliminar un usuario de una sala
	public abstract boolean getJust_created() ;
	
	public abstract void setJust_created(boolean a);
	public abstract void removeUser(String u);
	//Método para nombrar una sala
	public abstract void setRoomName(String roomName);
	//Método para devolver la descripción del estado actual de la sala
	public abstract NCRoomDescription getDescription();
	public abstract void broadcastPM(String u,String usuarioreceptor, String message) throws IOException;
	//Método para devolver el número de usuarios conectados a una sala
	public abstract int usersInRoom();
	public abstract void just_created(String u,Socket s)throws IOException;
	
}
