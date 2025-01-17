package greenwich.pointr;


import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class SmartSuggestions {

    public void saveInstance(String placeName, String placeTypes) throws Exception {
            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter xmlWriter = new StringWriter();
            File xmlFile = new File(Environment.getExternalStorageDirectory() + File.separator + "data.xml");
        if (!xmlFile.exists()) {
            xmlFile.createNewFile();
            FileWriter fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + File.separator + "data.xml");

            xmlSerializer.setOutput(xmlWriter);
            xmlSerializer.startDocument("UTF-8", null);

            // open tag <instances>
            xmlSerializer.startTag("", "instanceList");
            // open tag <place>
            xmlSerializer.startTag("", "place");

            // open tag <placeName>
            xmlSerializer.startTag("", "placeName");
            xmlSerializer.text(placeName);
            xmlSerializer.endTag("", "placeName");

            // open tag <types>
            xmlSerializer.startTag("", "types");
            xmlSerializer.text(placeTypes);
            xmlSerializer.endTag("", "types");

            // open tag <timestamp>
            xmlSerializer.startTag("", "timestamp");
            xmlSerializer.text(getTimestamp());
            xmlSerializer.endTag("", "timestamp");

            // close tag </place>
            xmlSerializer.endTag("", "place");

            // close tag <instances>
            xmlSerializer.endTag("", "instanceList");
            xmlSerializer.endDocument();
            xmlSerializer.flush();

            fileWriter.write(xmlWriter.toString());
            fileWriter.close();
        }
        else if (placeExists(placeName)) {
            saveChildNode(placeName);
        }
        else if (!placeExists(placeName)){
            saveParent(placeName, placeTypes);
        }
    }

    public void checkInstance() throws Exception{
        InputStream inputStream = new DataInputStream(new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + "data.xml"));

        String types = "";
        String placeName = "";
        List<String> timeStampList = new ArrayList<>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        //factory.setNamespaceAware(true);

        XmlPullParser parser = factory.newPullParser();
        //parser.require(XmlPullParser.START_TAG, "", "instances");
        //parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);

        while (parser.next() != XmlPullParser.END_DOCUMENT){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                Collections.sort(timeStampList);
                boolean frequent = isFrequent(timeStampList, placeName);
                if (frequent){
                    MainActivity.instance.showSnackbar("Would you like to search for " + placeName, types);
                    timeStampList = new ArrayList<>();
                }
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
            else if (name.equals("placeName")){
                if (parser.next() == XmlPullParser.TEXT){
                    placeName = parser.getText();
                    parser.nextTag();
                }
            }
        }
    }

    public boolean normalise(List<String> timeStampList)throws ParseException{
        // Feature scaling normalization
        long xPrime;
        boolean suggest = false;

        int noOfZeros = 0;
        int noOfOnes = 0;

        final int MILLI_TO_HOUR = 1000 * 60 * 60;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss a");

        // Convert to hours
        long xmin = dateFormat.parse(timeStampList.get(0)).getTime() / MILLI_TO_HOUR;
        long xmax = dateFormat.parse(timeStampList.get(timeStampList.size()-1)).getTime() / MILLI_TO_HOUR;

        if (xmin == xmax){
            suggest = true;
            return suggest;
        }
        {
            for (int i = 0; i < timeStampList.size(); i++) {
                long x = dateFormat.parse(timeStampList.get(i)).getTime() / MILLI_TO_HOUR;
                xPrime = (x - xmin) / (xmax - xmin);
                if (xPrime == 0) {
                    noOfZeros++;
                } else if (xPrime == 1) {
                    noOfOnes++;
                }
            }
            if (noOfZeros > noOfOnes) {
                suggest = false;
                return suggest;
            } else {
                suggest = true;
                return suggest;
            }
        }
    }

    private boolean placeExists(String pName) throws Exception{
        InputStream inputStream = new DataInputStream(new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + "data.xml"));

        boolean exists = false;
        String placeName = "";

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, null);

        while (parser.next() != XmlPullParser.END_DOCUMENT){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("placeName")){
                if (parser.next() == XmlPullParser.TEXT){
                    placeName = parser.getText();
                    parser.nextTag();
                    if (placeName.equals(pName)) {
                        exists = true;
                        return exists;
                    }
                }
            }
        }

        return exists;
    }

    private void deleteInstance(String id, Document document){
        Node deleteNode = searchByID(id, document);
        //document.getDocumentElement().removeChild(deleteNode);

        clearChildNodes(deleteNode);
        document.setXmlVersion("1.0");
        document.setNodeValue("<xml version=\"1.0\" encoding=\"UTF-8\"");
        document.normalize();
        getStringFromDocument(document);
        try {
            saveData(document, Environment.getExternalStorageDirectory() + File.separator + "data.xml");
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    private void saveChildNode(String placeName) throws Exception{
        // Add timestamp to existing node
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(Environment.getExternalStorageDirectory() + File.separator + "data.xml"));

        Node appendToNode = searchByID(placeName, doc);

        Element timeStamp = doc.createElement("timestamp");
        timeStamp.setTextContent(getTimestamp());
        appendToNode.appendChild(timeStamp);
        try {
            saveData(doc, Environment.getExternalStorageDirectory() + File.separator + "data.xml");
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    private void saveParent(String placeName, String types) throws Exception{
        // Create new instance of a POI
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(Environment.getExternalStorageDirectory() + File.separator + "data.xml"));
        Node existingNode = searchByID(placeName, doc);
        if (existingNode == null){
            // Create new parent node
            Element element = doc.getDocumentElement();
            Node placeNode = doc.createElement("place");
            element.appendChild(placeNode);

            Node placeNameNode = doc.createElement("placeName");
            placeNameNode.setTextContent(placeName);
            placeNode.appendChild(placeNameNode);

            Node typeNode = doc.createElement("types");
            typeNode.setTextContent(types);
            placeNode.appendChild(typeNode);

            Node timeStamp = doc.createElement("timestamp");
            timeStamp.setTextContent(getTimestamp());
            placeNode.appendChild(timeStamp);
            saveData(doc, Environment.getExternalStorageDirectory() + File.separator + "data.xml");
        }
    }

    public static void clearChildNodes(Node node){
        while(node.hasChildNodes()){
            NodeList nList = node.getChildNodes();
            int index = node.getChildNodes().getLength() - 1;

            Node n = nList.item(index);
            clearChildNodes(n);
            node.removeChild(n);
        }

    }

    public static Node searchByID(String id, Document document){
        NodeList list = document.getElementsByTagName("placeName");
        Node p = null;
        for (int i = 0; i < list.getLength(); i++){
            String content = list.item(i).getTextContent();
            if (content.equalsIgnoreCase(id)){
                p = list.item(i).getParentNode();
                return p;
            }
        }
        return p;
    }

    public void saveData(Document document, String path){
        try {
            DOMSource source = new DOMSource(document);
            File file = new File(path);
            Result result = new StreamResult(file);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.transform(source, result);
        }
        catch (TransformerException te){
            System.out.println(te.getStackTrace());
        }
    }

    private String getTimestamp(){
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        return currentDateTimeString;
    }

    public String getStringFromDocument(Document doc)
    {
        try
        {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.transform(domSource, result);
            return writer.toString();
        }
        catch(TransformerException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    private static int hoursDifference(Date date1, Date date2) {
        final int MILLI_TO_HOUR = 1000 * 60 * 60;
        return (int) (date1.getTime() - date2.getTime()) / MILLI_TO_HOUR;
    }

    private boolean isFrequent (List<String> timeStampList, String name) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss a");
        Date firstTimestamp;
        Date secondTimestamp;
        int hourlyDifference;

        // Normalise here

        try {
        if (Math.abs(hoursDifference(dateFormat.parse(timeStampList.get(timeStampList.size()-1)), dateFormat.parse(getTimestamp()))) >= 36){
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(Environment.getExternalStorageDirectory() + File.separator + "data.xml"));
            deleteInstance(name, doc);
        }
        }
        catch (Exception er){}

       if (timeStampList.size() >= 5) {
            for (int i = 0; i < timeStampList.size() - 1; i++) {
                try {
                    firstTimestamp = dateFormat.parse(timeStampList.get(i));
                    secondTimestamp = dateFormat.parse(timeStampList.get(i+1));
                    hourlyDifference = Math.abs(hoursDifference(firstTimestamp, secondTimestamp));

                    if (!normalise(timeStampList)){
                        try {
                            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
                            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
                            Document doc = docBuilder.parse(new File(Environment.getExternalStorageDirectory() + File.separator + "data.xml"));
                            deleteInstance(name, doc);
                        }
                        catch (Exception e){
                            System.out.println(e);
                        }
                        return false;
                    }
                    else {
                        return true;
                    }
                }
                catch (ParseException pe){
                    System.out.println(pe.getStackTrace());
                }
            }
        }

        return false;
    }
}
