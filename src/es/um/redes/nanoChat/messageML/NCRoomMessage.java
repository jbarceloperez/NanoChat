 package es.um.redes.nanoChat.messageML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NCRoomMessage extends NCMessage {

	private String name;
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_NAME = "<name>(.*?)</name>";
	private static final String NAME_MARK = "name";


	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCRoomMessage(byte opcode, String name) {
		this.opcode = opcode;
		this.name = name;
	}

	@Override	//CODIFICA EL MENSAJE!	LO USA EL QUE ENVIA
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE);
		sb.append("<"+NAME_MARK+">"+name+"</"+NAME_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje
		
	}

	//LO USA EL QUE RECIBE????????????????????????????
	
	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCRoomMessage readFromString(byte code, String message) {	//le pasamos el codigo y la cadena, coge esta y comienza a reconstruirla 
		String found_name = null;

		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
		Pattern pat_name = Pattern.compile(RE_NAME); //compilo el matron entre name y noname, lo de dentro de este entre estos dos será el nick
		Matcher mat_name = pat_name.matcher(message);
		if (mat_name.find()) {
			// Name found
			found_name = mat_name.group(1);	//si lo encuentro este será el nick
		} else {
			System.out.println("Error en RoomMessage: no se ha encontrado parametro.");
			return null;
		}
		
		return new NCRoomMessage(code, found_name); // y creo un mensaje de dos ccampos compuesto por el codigo y por el nick
	}


	//Devolvemos el nombre contenido en el mensaje
	public String getName() {
		return name;
	}

}
