 package es.um.redes.nanoChat.messageML;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class NCStringListMessage extends NCMessage {

	private List<String> name;
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_NAME = "<name>(.*?)</name>";
	private static final String NAME_MARK = "name";


	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCStringListMessage(byte opcode, List<String> aa) {
		this.opcode = opcode;
		this.name = aa;
	}

	@Override	//CODIFICA EL MENSAJE!	LO USA EL QUE ENVIA
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE);
		for (String namee: name) {
			sb.append("<"+NAME_MARK+">"+namee+"</"+NAME_MARK+">"+END_LINE);
		}
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje
		
	}

	//LO USA EL QUE RECIBE????????????????????????????
	
	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCStringListMessage readFromString(byte code, String message) {	//le pasamos el codigo y la cadena, coge esta y comienza a reconstruirla 
		List<String> ae = new LinkedList<String>();
		
		Pattern pat_name = Pattern.compile(RE_NAME);
		Matcher mat_name = pat_name.matcher(message);
		while (mat_name.find()) {
			ae.add(mat_name.group(1));	
		} 
		return new NCStringListMessage(code, ae); // y creo un mensaje de dos ccampos compuesto por el codigo y por el nick
	}


	//Devolvemos el nombre contenido en el mensaje
	public List<String> getList() {
        return new ArrayList<>(this.name);
    }

}
