package sections.backend.UsernameJoin;


import org.telegram.tgnet.TLRPC;




public class Channel {

    public String name;
    public boolean hasPhoto = false;
    public String title;
    public String byteString;
    public long id;
    public TLRPC.FileLocation photo;
    public TLRPC.InputChannel inputChannel;
    public Channel(String name, long id){
        this.name = name;
        this.id = id;
    }

    public Channel(){

    }

    public void setPhoto(String byteString){
        if(byteString == null || byteString.length() == 0){
            hasPhoto = false;
            return;
        }
        hasPhoto = true;
        this.byteString = byteString;
    }

}
