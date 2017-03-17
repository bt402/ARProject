package greenwich.pointr;


import android.os.Environment;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmartSuggestions {

    public void saveInstance(String placeName, String placeInstance) throws Exception {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter xmlWriter = new StringWriter();
        File xmlFile = new File(Environment.getExternalStorageDirectory() + File.separator + "data.xml");
        xmlFile.createNewFile();
        FileWriter fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + File.separator + "data.xml");

        xmlSerializer.setOutput(xmlWriter);
        xmlSerializer.startDocument("UTF-8", null);

        // open tag <instances>
        xmlSerializer.startTag("", "instances");
        // open tag <place>
        xmlSerializer.startTag("", "place");
        xmlSerializer.attribute("", "name", placeName);

        // open tag <types>
        xmlSerializer.startTag("", "types");
        xmlSerializer.text(placeInstance);
        xmlSerializer.endTag("", "types");

        // open tag <timestamp>
        xmlSerializer.startTag("", "timestamp");
        xmlSerializer.text(getTimestamp());
        xmlSerializer.endTag("", "timestamp");

        // close tag </place>
        xmlSerializer.endTag("", "place");

        // close tag <instances>
        xmlSerializer.endTag("", "instances");
        xmlSerializer.endDocument();
        xmlSerializer.flush();

        fileWriter.write(xmlWriter.toString());
        fileWriter.close();
    }

    public void checkInstance() throws Exception{
        InputStream inputStream = new DataInputStream(new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + "data.xml"));

        String types = "";
        String attributes = "";
        List<String> timeStampList = new ArrayList<>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        //factory.setNamespaceAware(true);

        XmlPullParser parser = factory.newPullParser();
        //parser.require(XmlPullParser.START_TAG, "", "instances");
        //parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);

        while (parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("types")){
                if (parser.next() == XmlPullParser.TEXT) {
                    types = parser.getText();
                    parser.nextTag();
                }
            }
            else if (name.equals("timestamp")) {
               if (parser.next() == XmlPullParser.TEXT){
                   timeStampList.add(parser.getText());
                   parser.nextTag();
               }
            }
            else if (name.equals("place")){
                attributes = parser.getAttributeValue(0);
            }
        }

    }

    private String getTimestamp(){
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        return currentDateTimeString;
    }

    private static int hoursDifference(Date date1, Date date2) {
        final int MILLI_TO_HOUR = 1000 * 60 * 60;
        return (int) (date1.getTime() - date2.getTime()) / MILLI_TO_HOUR;
    }
}
