package es.um.redes.nanoChat.messageML;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import java.util.List;


public abstract class NCMessage {
	protected byte opcode;

	// TODO IMPLEMENTAR TODAS LAS CONSTANTES RELACIONADAS CON LOS CODIGOS DE OPERACION
	public static final byte OP_INVALID_CODE = 0;
	
	public static final byte OP_NICK = 1;
	public static final byte OP_NICK_OK = 2;
	public static final byte OP_NICK_DUPLICATED = 3;
	
	public static final byte OP_STATS = 10;
	
	public static final byte OP_ENTER = 20;
	public static final byte OP_ACCEPTED = 21;
	public static final byte OP_DENIED = 22;
	public static final byte OP_ACCEPTEDNEWROOM = 29;
	
	public static final byte OP_LEAVE = 23;
	
	public static final byte OP_CHANGEROOMNAME = 24;
	public static final byte OP_NAMECHANGED = 27;
	public static final byte OP_NAMENOTCHANGED = 28;
	public static final byte OP_USERLEFT = 25;
	public static final byte OP_USERENTERED = 26;
	
	public static final byte OP_LIST_ROOMS = 30;
	public static final byte OP_ROOMS_LISTED = 31;
	
	public static final byte OP_QUIT = 40;
	
	public static final byte OP_ROOMINFO = 50;
	public static final byte OP_INFOLISTED = 51;
	
	public static final byte OP_SENDMESSAGE = 60;
	public static final byte OP_RCVMESSAGE = 61;
		
	public static final byte OP_SENDPM = 62;
	public static final byte OP_SENT= 63;
	public static final byte OP_NOTSENT= 64;
	
	public static final byte OP_HISTORY= 65;
	public static final byte OP_HISTORYPRINTED= 66;
	
	public static final char DELIMITER = ':';    //Define el delimitador
	public static final char END_LINE = '\n';    //Define el carácter de fin de línea
	
	public static final String OPERATION_MARK = "operation";
	public static final String MESSAGE_MARK = "message";

	/**
	 * Códigos de los opcodes válidos  El orden
	 * es importante para relacionarlos con la cadena
	 * que aparece en los mensajes
	 */
	private static final Byte[] _valid_opcodes = { 
		OP_NICK,
		OP_NICK_OK,
		OP_NICK_DUPLICATED,
		OP_STATS,
		OP_ENTER,
		OP_ACCEPTED,
		OP_DENIED,
		OP_ACCEPTEDNEWROOM,
		OP_LEAVE,
		OP_CHANGEROOMNAME,
		OP_NAMECHANGED, 
		OP_NAMENOTCHANGED,
		OP_USERLEFT,
		OP_USERENTERED,
		OP_LIST_ROOMS,
		OP_ROOMS_LISTED,
		OP_QUIT,
		OP_ROOMINFO,
		OP_INFOLISTED,
		OP_SENDMESSAGE,
		OP_RCVMESSAGE,
		OP_SENDPM,
		OP_SENT,
		OP_NOTSENT,
		OP_HISTORY,
		OP_HISTORYPRINTED
		};

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_operations_str = {	
		"Nick",
		"Nick_ok",
		"Nick_duplicated",
		"Stats",
		"Enter",
		"Accepted",
		"Denied",
		"Accepted_new_room",
		"Leave",
		"ChangeRoomName",
		"NameChanged",
		"NameNotChanged",
		"UserLeft",
		"UserEntered",
		"ListRooms",
		"Roomslisted",
		"quit",
		"roominfo",
		"infolisted",
		"sendmessage",
		"rcvmensaje",
		"sendpm",
		"sent",
		"notsent",
		"history",
		"historyprinted"
	};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;
	
	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0 ; i < _valid_operations_str.length; ++i)
		{
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}
	
	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte stringToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OP_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	protected static String opcodeToString(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
	
	//Devuelve el opcode del mensaje
	public byte getOpcode() {
		return opcode;
	}

	//Método que debe ser implementado por cada subclase de NCMessage
	protected abstract String toEncodedString(); 

	//Analiza la operación de cada mensaje y usa el método readFromString() de cada subclase para parsear
	public static NCMessage readMessageFromSocket(DataInputStream dis) throws IOException {	//cuando tengo que leer un mensaje de un socket
		String message = dis.readUTF();
		String regexpr = "<"+MESSAGE_MARK+">(.*?)</"+MESSAGE_MARK+">";
		Pattern pat = Pattern.compile(regexpr,Pattern.DOTALL);
		Matcher mat = pat.matcher(message);
		if (!mat.find()) {
			System.out.println("Mensaje mal formado:\n"+message);
			return null;
			// Message not found
		} 
		String inner_msg = mat.group(1);  // extraemos el mensaje
		
		String regexpr1 = "<"+OPERATION_MARK+">(.*?)</"+OPERATION_MARK+">";
		Pattern pat1 = Pattern.compile(regexpr1);
		Matcher mat1 = pat1.matcher(inner_msg);
		if (!mat1.find()) {
			System.out.println("Mensaje mal formado:\n" +message);
			return null;
			// Operation not found
		} 
		String operation = mat1.group(1);  // extraemos la operación
		
		byte code = stringToOpcode(operation);
		if (code == OP_INVALID_CODE) return null;
		
		switch (code) {
		//TODO Parsear el resto de mensajes 
		case OP_NICK:		
		{
			return NCRoomMessage.readFromString(code, message);
		}
		case OP_NICK_OK:
		{
			return NCOpcodeMessage.readFromString(code);
		}
		case OP_NICK_DUPLICATED:
		{
			return NCOpcodeMessage.readFromString(code);
		}
		case OP_ENTER:
		{
			return NCRoomMessage.readFromString(code, message);
		}
		case OP_ACCEPTED:
		{
			return NCOpcodeMessage.readFromString(code);
		}	
		case OP_DENIED:
		{
			return NCOpcodeMessage.readFromString(code);
		}	
		case OP_LEAVE:
		{
			return NCOpcodeMessage.readFromString(code);
		}
		case OP_ACCEPTEDNEWROOM:
		{
			return NCOpcodeMessage.readFromString(code);
		}
		case OP_LIST_ROOMS:
		{
			return NCOpcodeMessage.readFromString(code);
		}
		case OP_ROOMS_LISTED:
		{
			return NCListMessage.readFromString(code, message);
		}
		case OP_QUIT:
		{
			return NCOpcodeMessage.readFromString(code);
		}
		case OP_ROOMINFO:
		{
			return NCOpcodeMessage.readFromString(code);
		}
		case OP_INFOLISTED:
		{
			return NCInfoMessage.readFromString(code,message);
		}
		case OP_SENDMESSAGE:
		{
			return NCRoomMessage.readFromString(code, message);
		}
		case OP_RCVMESSAGE:
		{
			return NCRoomMessage.readFromString(code, message);
		}
		case OP_SENDPM:
		{
			return NCPmMessage.readFromString(code,message);
		}
		case OP_SENT:
		{
			return NCRoomMessage.readFromString(code,message);
		}
		case OP_NOTSENT:
		{
			return NCRoomMessage.readFromString(code,message);
		}
		case OP_CHANGEROOMNAME:
		{
			return NCRoomMessage.readFromString(code, message);
		}
		case OP_NAMECHANGED:
		{
			return NCRoomMessage.readFromString(code, message);
		}
		case OP_NAMENOTCHANGED:
		{
			return NCRoomMessage.readFromString(code, message);
		}
		case OP_USERLEFT:
		{
			return NCRoomMessage.readFromString(code, message);
		}
		case OP_USERENTERED:
		{
			return NCRoomMessage.readFromString(code, message);
		}
		case OP_HISTORY:
		{
			return NCOpcodeMessage.readFromString(code);
		}
		case OP_HISTORYPRINTED:
		{
			return NCStringListMessage.readFromString(code, message);
		}
		default:
			System.err.println("Unknown message type received:" + code);
			return null;
		}

	}

	//TODO Programar el resto de métodos para crear otros tipos de mensajes
	
	public static NCMessage makeRoomMessage(byte code, String room) {
		return new NCRoomMessage(code, room);
	}
	public static NCMessage makeListMessage(byte code, List<NCRoomDescription> descripcion) {
		return new NCListMessage(code, descripcion);
	}
	public static NCMessage makeOpcodeMessage(byte code) {
		return new NCOpcodeMessage(code);
	}
	public static NCMessage makeInfoMessage(byte code,NCRoomDescription descripcion) {
		return new NCInfoMessage(code,descripcion);
	}
	public static NCMessage makePmMessage(byte code,String usuario,String mensaje) {
		return new NCPmMessage(code,usuario,mensaje);
	}
	public static NCMessage makeStringListMessage(byte code,List<String>aa) {
		return new NCStringListMessage(code,aa);
	}
}
