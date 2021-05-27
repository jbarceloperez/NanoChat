package es.um.redes.nanoChat.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.um.redes.nanoChat.server.roomManager.*;


/**
 * Esta clase contiene el estado general del servidor (sin la lógica relacionada
 * con cada sala particular)
 */
class NCServerManager {

	// Primera habitación del servidor		//el profesor dice que le gustaría que al intentar entrar a una sala que no esté creada se cree
	final static String INITIAL_ROOM = "B";	// esta es la primera habitacion 	
 	final static String ROOM_PREFIX = "Room";	
	// Siguiente habitación que se creará	//la implementacion que está hecha aquí es para que cada vez que entres a una sala, se cree la siguiente
	String nextRoom;
	// Usuarios registrados en el servidor
	private Set<String> users = new HashSet<String>();
	// Habitaciones actuales asociadas a sus correspondientes RoomManagers
	private Map<String, NCRoomManager> rooms = new HashMap<String, NCRoomManager>();	//el nombre de la habitacion y su ADMIN

	NCServerManager() {
		nextRoom = INITIAL_ROOM;
	}

	// Método para registrar un RoomManager, tambien PARA AÑADIR NUEVAS SALAS
	public void registerRoomManager(NCRoomManager rm) { 
		// TODO Dar soporte para que pueda haber más de una sala en el servidor
		String roomName = nextRoom; 
		rooms.put(roomName, rm);
	}
	public synchronized boolean changeRoomName(String room,String newroom) {
		NCRoomManager manager = rooms.get(room);
		if (!rooms.containsKey(newroom)) {
			rooms.remove(room,manager);
			rooms.put(newroom,manager);
			return true;
		}
		return false;
	}
	// Devuelve la descripción de las salas existentes
	public synchronized List<NCRoomDescription> getRoomList() {	
		// TODO Pregunta a cada RoomManager cuál es la descripción actual de su sala
		ArrayList<NCRoomDescription> a = new ArrayList<>();
		for (NCRoomManager manager: rooms.values()) {
			a.add(((Manager)manager).getDescription());
		}
		return a;
	}

	// Intenta registrar al usuario en el servidor.
	public synchronized boolean addUser(String user) {
		if (users.contains(user)) {
			return false;
		} else {	
			users.add(user);
			return true;
		}
		
	}

	// Elimina al usuario del servidor
	public synchronized void removeUser(String user) {
		users.remove(user);
	}
	public synchronized void removeRoom(String room) {
		rooms.remove(room);
	}

	// Un usuario solicita acceso para entrar a una sala y registrar su conexión en ella
	public synchronized NCRoomManager enterRoom(String user, String room, Socket s) throws IOException {	//le dices que usuario es, que sala quiere entrar y el socket por el que se está conectando
		// TODO Verificamos si la sala existe
		if (!rooms.containsKey(room)) {
			nextRoom = room;
			Manager managerr = new Manager(nextRoom);
			registerRoomManager(managerr);
			if (managerr.registerUser(user, s)) {
				managerr.setJust_created(true);
				return managerr;	
			}
		}
		else {
			Manager managerrr = (Manager) rooms.get(room);
			if (managerrr.registerUser(user,s)) {
				return managerrr;
			}else {
				return null;
			}
			
		}
		
		return null;
	}

	// Un usuario deja la sala en la que estaba
	public synchronized void leaveRoom(String u, String room) {
		// TODO Verificamos si la sala existe
		if (rooms.containsKey(room)) {
			NCRoomManager rm = rooms.get(room);
			rm.removeUser(u);
		}
	}
}
