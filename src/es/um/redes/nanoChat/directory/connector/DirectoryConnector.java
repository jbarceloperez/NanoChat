package es.um.redes.nanoChat.directory.connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	// Tamaño máximo del paquete UDP (los mensajes intercambiados son muy cortos)
	private static final int PACKET_MAX_SIZE = 128;
	// Puerto en el que atienden los servidores de directorio
	private static final int DEFAULT_PORT = 6868;
	// Valor del TIMEOUT
	private static final int TIMEOUT = 1000;

	private DatagramSocket socket; // socket UDP
	private InetSocketAddress directoryAddress; // dirección del servidor de directorio

	public DirectoryConnector(String agentAddress) throws IOException {
		// TODO A partir de la dirección y del puerto generar la dirección de conexión
		// para el Socket
		directoryAddress = new InetSocketAddress(agentAddress, DEFAULT_PORT);
		// TODO Crear el socket UDP
		socket = new DatagramSocket();
	}

	/**
	 * Envía una solicitud para obtener el servidor de chat asociado a un
	 * determinado protocolo
	 * 
	 */
	public InetSocketAddress getServerForProtocol(int protocol) throws IOException {
		int intentosReenvio = 10;
		boolean respuestaRecibida = false;
		byte[] req = buildQuery(protocol); 
		DatagramPacket pckt = new DatagramPacket(req,req.length,directoryAddress.getAddress(),directoryAddress.getPort());
		socket.setSoTimeout(TIMEOUT);
		byte[] response = new byte[9];
		while (intentosReenvio != 0 && !respuestaRecibida) {
			// TODO preparar el buffer para la respuesta
			try {
				// TODO Enviar datagrama por el socket
				socket.send(pckt);
				// TODO Recibir la respuesta 
				System.out.println("Estás recibiendo una respuesta");
				pckt = new DatagramPacket(response, response.length);
				socket.receive(pckt);
				respuestaRecibida = true;
			} catch (SocketTimeoutException t) {
				System.out.println("No se ha recibido respuesta del servidor, te quedan los siguientes intentos:"
						+ intentosReenvio);
				intentosReenvio--;
			}
		}
		if (intentosReenvio == 0) {
			System.out.println("Se ha producido un error al intentar conectar con el servidor");
		}

		// TODO Procesamos la respuesta para devolver la dirección que hay en ella
		socket.close();
		return getAddressFromResponse(pckt);
	}

	// Método para generar el mensaje de consulta (para obtener el servidor asociado
	// a un protocolo)
	private byte[] buildQuery(int protocol) {
		ByteBuffer bb = ByteBuffer.allocate(5);
		byte opcode = 0;
		bb.put(opcode);
		bb.putInt(protocol);
		byte[] men = bb.array();
		return men;
	}

	// Método para obtener la dirección de internet a partir del mensaje UDP de
	// respuesta
	private InetSocketAddress getAddressFromResponse(DatagramPacket packet) throws UnknownHostException {
		ByteBuffer datos = ByteBuffer.wrap(packet.getData());
		byte opcode = datos.get();
		//TODO Si la respuesta no está vacía, devolver la dirección (extraerla del mensaje)
		if (opcode == 3) {
			int puerto = datos.getInt();
			byte[] ip = new byte[4];
			datos.get(ip);
			InetAddress aux = InetAddress.getByAddress(ip);
			InetSocketAddress dir = new InetSocketAddress(aux, puerto);
			return dir;
		}
		//TODO Analizar si la respuesta no contiene dirección (devolver null)
		else if (opcode == 4) {
			System.err.println("LA RESPUESTA NO CONTIENE DIRECCIÓN");
			return null;
		}
		return null;
	}

	/**
	 * Envía una solicitud para registrar el servidor de chat asociado a un
	 * determinado protocolo
	 * 
	 */
	public boolean registerServerForProtocol(int protocol, int port) throws IOException {
		byte[] registro = buildRegistration(protocol, port);
		int intentosReenvio = 10;
		boolean respuestaRecibida = false;
		DatagramPacket pckt = new DatagramPacket(registro, registro.length,directoryAddress.getAddress(),directoryAddress.getPort());
		byte[] response = new byte[PACKET_MAX_SIZE];
		socket.setSoTimeout(TIMEOUT);
		while (intentosReenvio != 0 && !respuestaRecibida) {	
			try {
				socket.send(pckt);
				System.out.println("Estás recibiendo una respuesta");
				pckt = new DatagramPacket(response, response.length);
				socket.receive(pckt);
				respuestaRecibida = true;
			} catch (SocketTimeoutException t) {
				System.out.println("No se ha recibido respuesta del directorio, te quedan los siguientes intentos:"
						+ intentosReenvio);
				intentosReenvio--;
			}
		}
		if (intentosReenvio == 0) {
			System.err.println("Se ha producido un error al intentar conectar con el directorio");
			return false;
		}
		socket.close();
		//TODO Procesamos la respuesta para ver si se ha podido registrar correctamente
		ByteBuffer ok = ByteBuffer.wrap(pckt.getData()); 
		byte opcode = ok.get();
		if (opcode == 2) {
			System.out.println("Se ha registrado correctamente");
			return true;
		}
		else {
			System.err.println("Fallo en el registro");
			return false;
		}
	}

	// Método para construir una solicitud de registro de servidor
	// OJO: No hace falta proporcionar la dirección porque se toma la misma desde la
	// que se envió el mensaje
	private byte[] buildRegistration(int protocol, int port) {
		// TODO Devolvemos el mensaje codificado en binario según el formato acordado
		ByteBuffer bb = ByteBuffer.allocate(9);
		byte opcode = 1;
		bb.put(opcode);
		bb.putInt(protocol);
		bb.putInt(port);
		
		byte[] men = bb.array();
		return men;
		
	}

	public void close() {
		socket.close();
	}
}
