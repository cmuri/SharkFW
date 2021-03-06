package net.sharkfw.asip.engine;

import net.sharkfw.asip.ASIPInterest;
import net.sharkfw.asip.ASIPKnowledge;
import net.sharkfw.asip.ASIPStub;
import net.sharkfw.asip.SharkStub;
import net.sharkfw.asip.serialization.ASIPMessageSerializer;
import net.sharkfw.asip.serialization.ASIPSerializationHolder;
import net.sharkfw.asip.serialization.ASIPSerializerException;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.SystemPropertyHolder;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.protocols.MessageStub;
import net.sharkfw.protocols.StreamConnection;
import net.sharkfw.protocols.Stub;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkException;
import net.sharkfw.system.SharkSecurityException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Objects of this class are resultSet of the scanning process
 * of imcomming messages from underlying protocols
 *
 * @author thsc, j4rvis
 */
public class ASIPInMessage extends ASIPMessage implements ASIPConnection {
    private SharkEngine se;
    private StreamConnection con;
    private InputStream is;
    private SharkStub sharkStub;
    private ASIPKnowledge knowledge;
    private ASIPInterest interest;
    private InputStream raw;
    private ASIPOutMessage response;
    private boolean parsed = false;
    private ASIPSerializationHolder holder = null;
    private byte[] jsonMessageBuffer;
    private int messageRead;
    private MessageStub messageStub;
    //    private boolean isEmpty = true;

    public ASIPInMessage(SharkEngine se, StreamConnection con) throws SharkKBException {

        super(se, con);

        this.se = se;
        this.con = con;
        this.is = con.getInputStream();
    }

    public ASIPInMessage(SharkEngine se, byte[] msg, Stub stub) {
        super(se, null);

        this.se = se;
        this.messageStub = (MessageStub) stub;
        this.is = new ByteArrayInputStream(msg);
    }

    public ASIPInMessage(SharkEngine se, int asipMessageType, ASIPInterest anyInterest, StreamConnection con, ASIPStub asipStub) {
        super(se, con);
    }

    public ASIPInMessage(SharkEngine se, ASIPInterest interest, SharkStub stub) {
        super(se, null);

        this.se = se;
        this.interest = interest;
        this.sharkStub = stub;
        this.setCommand(ASIPMessage.ASIP_EXPOSE);
    }

    public boolean parse() throws IOException, SharkSecurityException {

        if(this.is.available() > 0){
            if(holder == null){
                byte[] configBuffer = new byte[ASIPSerializationHolder.CONFIG_LENGTH];
                this.is.read(configBuffer);
                try {
                    holder = new ASIPSerializationHolder(new String(configBuffer, StandardCharsets.UTF_8));
                } catch (ASIPSerializerException e) {
                    e.printStackTrace();
                    L.d(e.getMessage(), this);
                    return false;
                }

                if(!holder.isASIP()){
                    return false;
                }
                jsonMessageBuffer = new byte[(int) holder.getMessageLength()];
                messageRead = 0;
//                L.d("Config read", this);
            }

            if(messageRead < jsonMessageBuffer.length){
                byte[] tempBuffer = new byte[jsonMessageBuffer.length - messageRead];
                int tempRead = this.is.read(tempBuffer);

                if( tempBuffer.length <= jsonMessageBuffer.length){
                    System.arraycopy(tempBuffer, 0, jsonMessageBuffer, messageRead, tempRead);
                    messageRead += tempRead;
//                    L.d("Still reading json message", this);
                }
            }
            if(messageRead > 0 && messageRead == jsonMessageBuffer.length){
                holder.setMessage(new String(jsonMessageBuffer, StandardCharsets.UTF_8));
//                L.d("Finished reading message", this);
                if(this.is.available() > 0){
//                    L.d("There is more available", this);
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[1024];

//                    L.d("Start reading from stream", this);

                    try{
                        while ((nRead = this.is.read(data, 0, data.length)) != -1) {
//                            L.d("Read: " + nRead, this);
                            buffer.write(data, 0, nRead);
                        }
                    } catch (IOException e){
                    } finally {
//                        L.d("finished reading from stream", this);
                        buffer.flush();
                        holder.setContent(buffer.toByteArray());
                    }
                }
                this.parsed = ASIPMessageSerializer.deserializeInMessage(this, holder);
                return this.parsed;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

//    public boolean isEmpty() {
//        return isEmpty;
//    }

    public ASIPKnowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(ASIPKnowledge knowledge) {
        this.knowledge = knowledge;
    }

    public ASIPInterest getInterest() {
        return interest;
    }

    public void setInterest(ASIPInterest interest) {
        this.interest = interest;
    }

    public InputStream getRaw() {
        return raw;
    }

    public void setRaw(InputStream raw) {
        this.raw = raw;
    }

    public void finished() {
        if (this.se.getAsipStub() != null && this.con != null) {
            this.se.getAsipStub().handleStream(this.con);
        }
    }

    public ASIPConnection getConnection() {
        return this;
    }

    public boolean keepOpen() {
        return true;
    }

    public ASIPOutMessage createResponse(SemanticTag topic, SemanticTag type) throws SharkKBException {
        if(this.con!=null){
            return this.se.createASIPOutResponse(this.con, this, topic, type);
        } else if(this.messageStub != null){
            return this.se.createASIPOutResponse(this.messageStub, this, topic, type);
        }
        return null;
    }

    @Override
    public void sendMessage(ASIPOutMessage msg, String[] addresses) throws SharkException {
        // TODO sendMessage
    }

    @Override
    public void sendMessage(ASIPOutMessage msg) throws SharkException {
        // TODO sendMessage
    }

    @Override
    public InputStream getInputStream() {
        return is;
    }

    @Override
    public boolean receivedMessageEncrypted() {
        return isEncrypted();
    }

    @Override
    public boolean receivedMessageSigned() {
        return isSigned();
    }

    @Override
    public void expose(ASIPInterest interest) throws SharkException {
        this.expose(interest, new String[]{});
    }

    @Override
    public void expose(ASIPInterest interest, String receiveraddress) throws SharkException {
        this.expose(interest, new String[]{receiveraddress});
    }

    @Override
    public void expose(ASIPInterest interest, String[] receiveraddresses) throws SharkException {
        if (interest == null)
            L.d("no interest", this);
//        if (receiveraddresses.length < 0)
//            L.d("no address", this);

        this.response = this.createResponse(null, null);
        if (this.response != null) {
            this.response.expose(interest);
        }
    }

    @Override
    public void insert(ASIPKnowledge k, String receiveraddress) throws SharkException {
        this.insert(k, new String[]{receiveraddress});
    }

    @Override
    public void insert(ASIPKnowledge k, String[] receiveraddresses) throws SharkException {
        this.response = this.createResponse(null, null);
        if (this.response != null) {
            this.response.insert(k);
        }
    }

    @Override
    public void raw(InputStream stream, String address) throws SharkException {
        this.raw(stream, new String[]{address});
    }

    @Override
    public void raw(InputStream stream, String[] address) throws SharkException {
        //TODO address not used
        ASIPOutMessage outMessage = this.createResponse(null, null);
        if (outMessage != null) {
            outMessage.raw(stream);
        }
    }

    @Override
    public void raw(byte[] bytes, String address) throws SharkException {
        this.raw(bytes, new String[]{address});
    }

    @Override
    public void raw(byte[] bytes, String[] address) throws SharkException {
        //TODO address not used
        ASIPOutMessage outMessage = this.createResponse(null, null);
        if (outMessage != null) {
            outMessage.raw(bytes);
        }
    }

    public void resetResponse() {
        this.response = null;
    }

    @Override
    public boolean responseSent() {
        if (this.response == null) {
            return false;
        }
        return response.responseSent();
    }

    @Override
    public void sendToAllAddresses(PeerSemanticTag pst) {

    }

    public boolean isParsed() {
        return this.parsed;
    }
}
