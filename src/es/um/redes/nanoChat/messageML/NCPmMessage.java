 package es.um.redes.nanoChat.messageML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class NCPmMessage extends NCMessage {

	private String name;
	private String mensaje;
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_NAME = "<name>(.*?)</name>";
	private static final String NAME_MARK = "name";
	private static final String RE_MSG = "<mensaje>(.*?)</mensaje>";
	private static final String MSG_MARK = "mensaje";


	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCPmMessage(byte opcode, String name,String msg) {
		this.opcode = opcode;
		this.name = name;
		this.mensaje = msg;
	}

	@Override	//CODIFICA EL MENSAJE!	LO USA EL QUE ENVIA
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE);
		sb.append("<"+NAME_MARK+">"+name+"</"+NAME_MARK+">"+END_LINE);
		sb.append("<"+MSG_MARK+">"+mensaje+"</"+MSG_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje
		
	}
	
	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCPmMessage readFromString(byte code, String message) {	//le pasamos el codigo y la cadena, coge esta y comienza a reconstruirla 
		String found_name = null;
		String found_msg = null;
		
		Pattern pat_name = Pattern.compile(RE_NAME);  
		Matcher mat_name = pat_name.matcher(message);
		if (mat_name.find()) {
			found_name = mat_name.group(1);	
		}
		
		Pattern pat2_name = Pattern.compile(RE_MSG);
		Matcher mat2_name = pat2_name.matcher(message);
		if (mat2_name.find()) {
			found_msg = mat2_name.group(1);	
		}
		return new NCPmMessage(code, found_name, found_msg); 
	}


	//Devolvemos el nombre contenido en el mensaje
	public String getUser() {
		return name;
	}
	public String getMsg() {
		return mensaje;
	}

}
