package es.um.redes.nanoChat.messageML;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
ROOM
----

<message>
	<operation>operation</operation>
	<room>room</room>
		<members>
			<user>usuario</user>
			<user>usuario</user>
			<user>usuario</user>
		</members>
	<room>room</room>
		<members>
			<user>usuario</user>
			<user>usuario</user>
			<user>usuario</user>
		</members>
</message>

*/


public class NCListMessage extends NCMessage {

    private List<NCRoomDescription> ldescripcion;

    //Constantes asociadas a las marcas específicas de este tipo de mensaje
    private static final String RE_ROOM = "<room>(.*?)</room>";
    private static final String ROOM_MARK = "room";
    private static final String RE_MEMBERS = "<members>([\n](.*?)[\n]*)*</members>";
    private static final String MEMBERS_MARK = "members";
    private static final String RE_USER = "<user>(.*?)</user>";
    private static final String USER_MARK = "user";
    private static final String RE_TLASTMESSAGE = "<tlm>(.*?)</tlm>";
    private static final String TLASTMESSAGE_MARK = "tlm";

    /**
     * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
     */
    public NCListMessage(byte opcode, List<NCRoomDescription> list)
    {
        this.opcode = opcode;
        this.ldescripcion = list;
    }

    @Override
    //Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
    public String toEncodedString() {
        StringBuffer sb = new StringBuffer();

        sb.append("<"+MESSAGE_MARK+">"+END_LINE);
        sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
        for (NCRoomDescription description : ldescripcion) {
            sb.append("<"+ROOM_MARK+">"+description.roomName+"</"+ROOM_MARK+">"+END_LINE);
            sb.append("<"+MEMBERS_MARK+">"+END_LINE);
            for(String miembro : description.members) {
            	sb.append("<"+USER_MARK+">"+miembro+"</"+USER_MARK+">"+END_LINE);
            }
            sb.append("</"+MEMBERS_MARK+">"+END_LINE);
            sb.append("<"+TLASTMESSAGE_MARK+">"+description.timeLastMessage+"</"+TLASTMESSAGE_MARK+">"+END_LINE);
        }
        sb.append("</"+MESSAGE_MARK+">"+END_LINE);

        return sb.toString(); //Se obtiene el mensaje
    }


    //Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
    public static NCListMessage readFromString(byte code, String message) {
        String found_name = null;
        String found_members = null;
        long found_tlastmessage = 0;

        List<String> rooms = new LinkedList<>();
        List<List<String>> members = new LinkedList<>();
        List<Long> tlastmessage = new LinkedList<>();
        List<NCRoomDescription> a = new LinkedList<NCRoomDescription>();

        
        Pattern pat_room = Pattern.compile(RE_ROOM);
        Matcher mat_room = pat_room.matcher(message);
        while (mat_room.find()) {
            found_name = mat_room.group(1);
            rooms.add(found_name);
        }

        Pattern pat_members = Pattern.compile(RE_MEMBERS);
        Matcher mat_members = pat_members.matcher(message);
        Pattern pat_user = Pattern.compile(RE_USER);
        while (mat_members.find()) {
            found_members = mat_members.group(0);	//todos los <user> estan aqui dentro
            Matcher mat_user = pat_user.matcher(found_members);
            List<String> aux = new LinkedList<>();
            while (mat_user.find()) {
                aux.add(mat_user.group(1));
            }
            members.add(aux);	//lista de listas de miembros
        }

        Pattern pat_tlm = Pattern.compile(RE_TLASTMESSAGE);
        Matcher mat_tlm = pat_tlm.matcher(message);
        while (mat_tlm.find()) {
            found_tlastmessage = Long.parseLong(mat_tlm.group(1));
            tlastmessage.add(found_tlastmessage);
        }

        for(int i = 0; i< rooms.size();i++) {
			NCRoomDescription description = new NCRoomDescription(rooms.get(i),members.get(i),tlastmessage.get(i));
			a.add(description);
		}
        

        return new NCListMessage(code, a);
    }

    public List<NCRoomDescription> getList() {
        return new ArrayList<>(ldescripcion);
    }
}