package es.um.redes.nanoChat.directory.server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class DirectoryThread extends Thread {
	// Tamaño máximo del paquete UDP
	private static final int PACKET_MAX_SIZE = 128;
	// Estructura para guardar las asociaciones ID_PROTOCOLO -> Dirección del
	// servidor
	protected HashMap<Integer, InetSocketAddress> servers;
	// Socket de comunicación UDP
	protected DatagramSocket socket = null;
	// Probabilidad de descarte del mensaje
	protected double messageDiscardProbability;

	public DirectoryThread(String name, int directoryPort, double corruptionProbability) throws SocketException {
		super(name);

		// TODO Anotar la dirección en la que escucha el servidor de Directorio
		InetSocketAddress serverAddress = new InetSocketAddress(directoryPort);

		// TODO Crear un socket de servidor
		socket = new DatagramSocket(serverAddress);

		messageDiscardProbability = corruptionProbability;
		// Inicialización del mapa
		servers = new HashMap<Integer, InetSocketAddress>();
	}

	public void run() {
		byte[] buf = new byte[PACKET_MAX_SIZE];

		System.out.println("Directory starting...");
		boolean running = true;
		while (running) {

			// TODO 1) Recibir la solicitud por el socket
			DatagramPacket pckt = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(pckt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// TODO 2) Extraer quién es el cliente (su dirección)
			InetSocketAddress ca = (InetSocketAddress) pckt.getSocketAddress();
			// 3) Vemos si el mensaje debe ser descartado por la probabilidad de descarte

			double rand = Math.random();
			if (rand < messageDiscardProbability) {
				System.err.println("Directory DISCARDED corrupt request from... ");
				continue;
			}
			// TODO 4) Analizar y procesar la solicitud (llamada a processRequestFromCLient)
			try {
				processRequestFromClient(pckt.getData(), ca);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// TODO 5) Tratar las excepciones que puedan producirse

		}
		socket.close();
	}

	// Método para procesar la solicitud enviada por clientAddr
	public void processRequestFromClient(byte[] data, InetSocketAddress clientAddr) throws IOException {
		// TODO 1) Extraemos el tipo de mensaje recibido
		ByteBuffer ret = ByteBuffer.wrap(data);
		byte opcode = ret.get(); // Obtiene un campo de 1 byte
		int protocol = ret.getInt();
		// TODO 2) Procesar el caso de que sea un registro y enviar mediante sendOK
		if (opcode == 1) {
			int puerto_del_server = ret.getInt();
			InetSocketAddress ca = new InetSocketAddress(clientAddr.getAddress(), puerto_del_server);
			int b = 0;
			if (!servers.containsKey(protocol)) {
				servers.put(protocol, ca);
				b = 1;
				sendOK(clientAddr, b);
			} else
				sendOK(clientAddr, b);

		}

		// TODO 3) Procesar el caso de que sea una consulta
		else if (opcode == 0) {
			// TODO 3.1) Devolver una dirección si existe un servidor (sendServerInfo) ->
			// opcode = 3
			if (servers.containsKey(protocol))
				sendServerInfo(servers.get(protocol), clientAddr);
			// TODO 3.2) Devolver una notificación si no existe un servidor (sendEmpty) ->
			// opcode = 4
			else
				sendEmpty(clientAddr);
		}
	}

	// Método para enviar una respuesta vacía (no hay servidor)
	private void sendEmpty(InetSocketAddress clientAddr) throws IOException {
		// TODO Construir respuesta
		ByteBuffer bb = ByteBuffer.allocate(1); // Crea un buffer de 1 byte
		byte opcode = 4;
		bb.put(opcode);
		byte[] empty = bb.array();
		// TODO Enviar respuesta
		DatagramPacket pckt = new DatagramPacket(empty, empty.length, clientAddr);
		try {
			socket.send(pckt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Método para enviar la dirección del servidor al cliente
	private void sendServerInfo(InetSocketAddress serverAddress, InetSocketAddress clientAddr) throws IOException {
		// TODO Obtener la representación binaria de la dirección
		byte[] ip = serverAddress.getAddress().getAddress();
		// TODO Construir respuesta
		ByteBuffer bb = ByteBuffer.allocate(9);
		byte opcode = 3;
		bb.put(opcode);
		bb.putInt(serverAddress.getPort());
		bb.put(ip);
		byte[] info = bb.array();
		// TODO Enviar respuesta
		DatagramPacket pckt = new DatagramPacket(info, info.length, clientAddr);
		socket.send(pckt);
	}

	// Método para enviar la confirmación del registro
	private void sendOK(InetSocketAddress clientAddr, int b) throws IOException {
		// TODO Construir respuesta
		ByteBuffer bb = ByteBuffer.allocate(1);
		byte opcode;
		if (b == 1) {
			opcode = 2;
		} else {
			opcode = 5;
		}
		bb.put(opcode);
		byte[] ok = bb.array();
		// TODO Enviar respuesta
		DatagramPacket pckt = new DatagramPacket(ok, ok.length, clientAddr);
		try {
			socket.send(pckt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
