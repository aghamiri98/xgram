package sections.backend.Channel;




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

//    public void setBitMap(final ImageView imageView) {
//       /* if(bitmap != null){
//            imageView.setImageBitmap(bitmap);
//            return;
//        }*/
//        if (byteString != null && byteString.length() > 0) {
//            Picasso.with(ApplicationLoader.applicationContext).load(byteString).placeholder(R.drawable.default_channel_icon).into(imageView);
////            return FileConvert.getBitmapFromString(imageUrl);
//        } /*else {
//            bitmap = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.default_channel_icon);
//            imageView.setImageBitmap(bitmap ); }*/
//    }
    public void setPhoto(String byteString){
        if(byteString == null || byteString.length() == 0){
            hasPhoto = false;
            return;
        }
        hasPhoto = true;
        this.byteString = byteString;
    }

//    public Bitmap getBitMap(){
//        if(byteString != null && byteString.length() > 0){
////            //Log.e("CH","Orginal Bitmap");
//            return FileConvert.getBitmapFromString(byteString);
//        }
//        else {
////            //Log.e("CH","Default Bit");
//            return BitmapFactory.decodeResource(ApplicationLoader.applicationContext.getResources(),
//                    R.drawable.default_channel_icon);
//        }
//    }
}
