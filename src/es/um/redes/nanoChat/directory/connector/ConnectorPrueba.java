package es.um.redes.nanoChat.directory.connector;

import java.io.IOException;
import java.net.InetSocketAddress;


public class ConnectorPrueba {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		DirectoryConnector cliente = new DirectoryConnector("127.0.0.1");
		InetSocketAddress address = cliente.getServerForProtocol(0); // e protocolo debe de ser el mismo que el del servidor del chat y para eso necesitamos su ip/puerto, que no lo conocemos, pero si que conocemos el ip/port del servidor directorio, le pedimos a este un getserverforprotocol(x) y este nos lo dirá con un mensaaje que contendrá la info(ip puerto) del servidor de chat, tb te puede devolver no colega no tenemos ese servidor con protocolo(x), para que el directorio contenga el ip/puerto del servidor de chat, dicho servidor deberá de haberle mandado al directorio con un Setregisterforprotocol(x,ip,protocol) y este directorio lo guarda en su lista de sus servers de chat
		System.out.println(address);
	}

}
