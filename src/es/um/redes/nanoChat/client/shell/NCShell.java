package es.um.redes.nanoChat.client.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import es.um.redes.nanoChat.client.comm.NCConnector;

public class NCShell {
	/**
	 * Scanner para leer comandos de usuario de la entrada estándar
	 */
	private Scanner reader;

	byte command = NCCommands.COM_INVALID;
	String[] commandArgs = new String[0];

	public NCShell() {
		reader = new Scanner(System.in);

		System.out.println("NanoChat shell");
		System.out.println("For help, type 'help'");
	}

	// devuelve el comando introducido por el usuario
	public byte getCommand() {
		return command;
	}

	// Devuelve los parámetros proporcionados por el usuario para el comando actual
	public String[] getCommandArguments() {
		return commandArgs;
	}

	// Espera hasta obtener un comando válido entre los comandos existentes
	public void readGeneralCommand() {
		boolean validArgs;
		do {
			commandArgs = readGeneralCommandFromStdIn();
			// si el comando tiene parámetros hay que validarlos
			validArgs = validateCommandArguments(commandArgs);
		} while (!validArgs);
	}

	// Usa la entrada estándar para leer comandos y procesarlos
	private String[] readGeneralCommandFromStdIn() {
		String[] args = new String[0];
		Vector<String> vargs = new Vector<String>();
		while (true) {
			System.out.print("(nanoChat) ");
			// obtenemos la línea tecleada por el usuario
			String input = reader.nextLine();
			StringTokenizer st = new StringTokenizer(input);
			// si no hay ni comando entonces volvemos a empezar
			if (!st.hasMoreTokens()) {
				continue;
			}
			// traducimos la cadena del usuario en el código de comando correspondiente
			command = NCCommands.stringToCommand(st.nextToken()); // AQUI TENDRÉ LA ORDEN , ES DECIR EL "NICK"
			// Dependiendo del comando...
			switch (command) {
			case NCCommands.COM_INVALID:
				// El comando no es válido
				System.out.println("Invalid command");
				continue;
			case NCCommands.COM_HELP:
				// Mostramos la ayuda
				NCCommands.printCommandsHelp();
				continue;
			case NCCommands.COM_QUIT:
			case NCCommands.COM_ROOMLIST:
				// Estos comandos son válidos sin parámetros
				break;
			case NCCommands.COM_ENTER: // NECESITAREMOS PASARLE EL NOMBRE DE LA SALA!
				while (st.hasMoreTokens()) {
					vargs.add(st.nextToken()); // AÑADE A VARGS ESE SIGUIENTE PARÁMETRO QUE SE LE PASE TRAS EL ENTER
				}
			case NCCommands.COM_NICK:
				// Estos requieren un parámetro
				while (st.hasMoreTokens()) {
					vargs.add(st.nextToken()); // AÑADE A VARGS ESE SIGUIENTE PARÁMETRO QUE SE LE PASE TRAS EL NICK
				}
				break;
			default:
				System.out.println("That command is only valid if you are in a room");
			}
			break;
		}
		return vargs.toArray(args);
	}

	// Espera a que haya un comando válido de sala o llegue un mensaje entrante
	public void readChatCommand(NCConnector ngclient) {
		boolean validArgs;
		do {
			commandArgs = readChatCommandFromStdIn(ngclient);
			// si hay parámetros se validan
			validArgs = validateCommandArguments(commandArgs);
		} while (!validArgs);
	}

	// Utiliza la entrada estándar para leer comandos y comprueba si hay datos en el
	// flujo de entrada del conector
	private String[] readChatCommandFromStdIn(NCConnector ncclient) { // le pasamos el servidor de chat
		String[] args = new String[0];
		Vector<String> vargs = new Vector<String>();
		while (true) {
			System.out.print("(nanoChat-room) ");
			// Utilizamos un BufferedReader en lugar de un Scanner porque no podemos
			// bloquear la entrada
			BufferedReader standardInput = new BufferedReader(new InputStreamReader(System.in));
			boolean blocked = true;
			String input = "";
			// Estamos esperando comando o mensaje entrante
			while (blocked) {
				try {
					if (ncclient.isDataAvailable()) {
						// Si el flujo de entrada tiene datos entonces el comando actual es SOCKET_IN y
						// debemos salir
						command = NCCommands.COM_SOCKET_IN; // si queremos implementar algo que necesita mandar un
															// mensaje, lo que tenemos que hacer en el ncccontroller es
															// capturar una llegada de COM_SOCKET_IN y capturar por la
															// entrada el mensaje
						return null;
					} else
					// Analizamos si hay datos en la entrada estándar (el usuario tecleó INTRO)
					if (standardInput.ready()) {
						input = standardInput.readLine();
						blocked = false;
					}
					// Puesto que estamos sondeando las dos entradas de forma continua, esperamos
					// para evitar un consumo alto de CPU
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (IOException | InterruptedException e) {
					command = NCCommands.COM_INVALID;
					return null;
				}
			}
//DENTRO DE SALA			//Si el usuario tecleó un comando entonces procedemos de igual forma que hicimos antes para los comandos generales
			StringTokenizer st = new StringTokenizer(input);
			if (!st.hasMoreTokens())
				continue;
			command = NCCommands.stringToCommand(st.nextToken());
			switch (command) {
			case NCCommands.COM_INVALID:
				System.out.println("Invalid command (" + input + ")");
				continue;
			case NCCommands.COM_HELP:
				NCCommands.printCommandsHelp();
				continue;
			case NCCommands.COM_ROOMINFO:
				break;
			case NCCommands.COM_EXIT:
				break;
			case NCCommands.COM_SEND:
				StringBuffer message = new StringBuffer();
				while (st.hasMoreTokens())
					message.append(st.nextToken() + " ");
				vargs.add(message.toString());
				break;
			case NCCommands.COM_SENDPM:
				StringBuffer messagee = new StringBuffer();
				if (st.hasMoreTokens()) {
					vargs.add(st.nextToken());
				}
				while (st.hasMoreTokens()) {
					messagee.append(st.nextToken() + " ");
				}
				vargs.add(messagee.toString());
				break;
			case NCCommands.COM_CHANGEROOMNAME:
				while (st.hasMoreTokens()) {
					vargs.add(st.nextToken());
				}
				break;
			case NCCommands.COM_HISTORY:
				break;
			default:
				System.out.println("That command is only valid if you are not in a room");
				;
			}
			break;
		}
		return vargs.toArray(args);
	}

	// Algunos comandos requieren un parámetro
	// Este método comprueba si se proporciona parámetro para los comandos
	private boolean validateCommandArguments(String[] args) {
		switch (this.command) {
		// enter requiere el parámetro <room>
		case NCCommands.COM_ENTER:
			if (args.length == 0 || args.length > 1) {
				System.out.println("Correct use: enter <room>");
				return false;
			}
			break;
		// nick requiere el parámetro <nickname>
		case NCCommands.COM_NICK:
			if (args.length == 0 || args.length > 1) {
				System.out.println("Correct use: nick <nickname>");
				return false;
			}
			break;
		// send requiere el parámetro <message>
		case NCCommands.COM_SEND:
			if (args.length == 0) {
				System.out.println("Correct use: send <message>");
				return false;
			}
			break;
		case NCCommands.COM_SENDPM:
			if ((args.length <= 1)) {
				System.out.println("Correct use: sendpm <user> <message>");
				return false;
			}
		case NCCommands.COM_CHANGEROOMNAME:
			if ((args.length == 0)) {
				System.out.println("Correct use: changeroomname <newname>");
				return false;
			}
			break;
		default:
		}
		// El resto no requieren parámetro
		return true;
	}
}