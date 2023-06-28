import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        String fileNameXML = "data.xml";
        String nameFileWrite = "data.json";
        String nameFileWrite2 = "data2.json";

        //----------CSV-JSON-------------------------------------
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, nameFileWrite);

        //----------XML-JSON-------------------------------------
        List<Employee> list2 = parseXML(fileNameXML);
        String json2 = listToJson(list2);
        writeString(json2, nameFileWrite2);

        //-----------JSON----------------------------------------
        String json3 = readString("data2.json");
        List<Employee> list3 = jsonToList(json3);
        System.out.println(list3);
    }

    public static List<Employee> jsonToList(String json) {

        List<Employee> employeeList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(json);
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            Gson gson = builder.create();

            for (int i = 0; i < jsonArray.size(); i++) {
                Employee employee = gson.fromJson(jsonArray.get(i).toString(), Employee.class);
                employeeList.add(employee);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return employeeList;
    }

    public static String readString(String filename) throws FileNotFoundException {

        InputStream inputStream = new FileInputStream(filename);
        String jsonString;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder rawJson = new StringBuilder();
            String readLine;
            while ((readLine = reader.readLine()) != null) {
                rawJson.append(readLine);
            }
            jsonString = rawJson.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonString;
    }

    public static List<Employee> parseXML(String fileNameXML) throws ParserConfigurationException, IOException, SAXException {

        List<Employee> employeeList = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(fileNameXML));

        Node root = doc.getDocumentElement();
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node_ = nodeList.item(i);

            if (Node.ELEMENT_NODE == node_.getNodeType()) {
                NodeList nodeListChild = node_.getChildNodes();
                long id = 0;
                String firstName = null;
                String lastName = null;
                String country = null;
                int age = 0;

                for (int j = 0; j < nodeListChild.getLength(); j++) {

                    Node node__ = nodeListChild.item(j);
                    if (Node.ELEMENT_NODE == node__.getNodeType()) {
                        Element element = (Element) node__;

                        Node childNode = element.getFirstChild();
                        if (Node.TEXT_NODE == childNode.getNodeType()) {

                            if (node__.getNodeName() == "id") {
                                id = Integer.valueOf(childNode.getNodeValue());
                            }
                            if (node__.getNodeName() == "firstName") {
                                firstName = childNode.getNodeValue();
                            }
                            if (node__.getNodeName() == "lastName") {
                                lastName = childNode.getNodeValue();
                            }
                            if (node__.getNodeName() == "country") {
                                country = childNode.getNodeValue();
                            }
                            if (node__.getNodeName() == "age") {
                                age = Integer.valueOf(childNode.getNodeValue());
                            }
                        }
                    }
                }
                employeeList.add(new Employee(id, firstName, lastName, country, age));
            }
        }
        return employeeList;
    }


    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {

        List<Employee> listStaff = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy =
                    new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);         // стратегия Mapping для класса Employee.class
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            listStaff = csv.parse();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return listStaff;
    }

    public static String listToJson(List<Employee> listStaff) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        String json = gson.toJson(listStaff, listType);
        return json;
    }

    public static void writeString(String jsonStr, String nameFileWrite) {

        try (FileWriter file = new FileWriter(nameFileWrite)) {
            file.write(jsonStr);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


