package es.um.redes.nanoChat.messageML;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NCInfoMessage extends NCMessage {

    private NCRoomDescription descripcion;

    //Constantes asociadas a las marcas específicas de este tipo de mensaje
    private static final String RE_ROOM = "<room>(.*?)</room>";
    private static final String ROOM_MARK = "room";
    private static final String RE_MEMBERS = "<members>([\\n](.*?)[\\n]*)*</members>";
    private static final String MEMBERS_MARK = "members";
    private static final String RE_USER = "<user>(.*?)</user>";
    private static final String USER_MARK = "user";
    private static final String RE_TLASTMESSAGE = "<tlm>(.*?)</tlm>";
    private static final String TLASTMESSAGE_MARK = "tlm";

    /**
     * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
     */
    public NCInfoMessage(byte opcode, NCRoomDescription descripcion)
    {
        this.opcode = opcode;
        this.descripcion = descripcion;
    }

    @Override
    //Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
    public String toEncodedString() {
        StringBuffer sb = new StringBuffer();

        sb.append("<"+MESSAGE_MARK+">"+END_LINE);
        sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
        sb.append("<"+ROOM_MARK+">"+descripcion.roomName+"</"+ROOM_MARK+">"+END_LINE);
        sb.append("<"+MEMBERS_MARK+">"+END_LINE);
        for(String miembro : descripcion.members) {
        	sb.append("<"+USER_MARK+">"+miembro+"</"+USER_MARK+">"+END_LINE);
        }
        sb.append("</"+MEMBERS_MARK+">"+END_LINE);
        sb.append("<"+TLASTMESSAGE_MARK+">"+descripcion.timeLastMessage+"</"+TLASTMESSAGE_MARK+">"+END_LINE);
        sb.append("</"+MESSAGE_MARK+">"+END_LINE);
        return sb.toString(); //Se obtiene el mensaje
    }


    //Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
    public static NCInfoMessage readFromString(byte code, String message) {
        String found_members = null;

        String room = null;
        List<String> members = new LinkedList<>();
        long tlastmessage = 0;

        
        Pattern pat_room = Pattern.compile(RE_ROOM);
        Matcher mat_room = pat_room.matcher(message);
        while (mat_room.find()) {
            room = mat_room.group(1);
            
        }

        Pattern pat_members = Pattern.compile(RE_MEMBERS);
        Matcher mat_members = pat_members.matcher(message);
        Pattern pat_memberss = Pattern.compile(RE_USER);
        while (mat_members.find()) {
            found_members = mat_members.group(0);	// la lista con comas que hay entre los <members>
            Matcher mat_memberss = pat_memberss.matcher(found_members);
            while (mat_memberss.find()) {
                members.add(mat_memberss.group(1));
            }
        }

        Pattern pat_tlm = Pattern.compile(RE_TLASTMESSAGE);
        Matcher mat_tlm = pat_tlm.matcher(message);
        while (mat_tlm.find()) {
            tlastmessage = Long.parseLong(mat_tlm.group(1));
        }

        
		NCRoomDescription description = new NCRoomDescription(room,members,tlastmessage);
		
        

        return new NCInfoMessage(code,description);
    }

    public NCRoomDescription getInfo() {
        return descripcion;
    }
}