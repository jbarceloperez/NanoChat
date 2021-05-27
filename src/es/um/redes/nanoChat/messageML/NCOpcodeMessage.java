/**
 * 
 */
package es.um.redes.nanoChat.messageML;

/*
 * OPCODE
----

<message>
<operation>operation</operation>
</message>

Operaciones válidas:

Nick_OK
*/

public class NCOpcodeMessage extends NCMessage {
	
	public NCOpcodeMessage(byte opcode) {
		this.opcode = opcode;
	}
	
	
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje
		
	}
	
	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
		public static NCOpcodeMessage readFromString(byte code) {	 
			return new NCOpcodeMessage(code); 
		}
		
		
}
