package com.example.Rabobank.services;

import com.example.Rabobank.model.Record;
import com.example.Rabobank.model.ValidateRecords;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

@Service
public class UploadServiceImp implements UploadService {

    @Override
    public ResponseEntity validateCSV(MultipartFile file) {

        List<Record> recordList = generatePojoFromCSV(file);

        return validateData(recordList);
    }

    @Override
    public ResponseEntity validateXML(MultipartFile file)  {

        List<Record> recordList = generatePojoFromXML(file);

        return validateData(recordList);
    }


    private List<Record> generatePojoFromCSV(MultipartFile file){
        List<Record> recordList = new ArrayList<Record>();
        Map<String, String> mapping = new HashMap<>();
        mapping.put("Reference", "reference");
        mapping.put("AccountNumber", "accountNumber");
        mapping.put("Description", "description");
        mapping.put("Start Balance", "startBalance");
        mapping.put("Mutation", "mutation");
        mapping.put("End Balance", "endBalance");

        HeaderColumnNameTranslateMappingStrategy<Record> strategy = new HeaderColumnNameTranslateMappingStrategy<Record>();
        strategy.setType(Record.class);
        strategy.setColumnMapping(mapping);

        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader(generatePath(file)));
            CsvToBean csvToBean = new CsvToBean();
            csvToBean.setCsvReader(csvReader);
            csvToBean.setMappingStrategy(strategy);
            recordList = csvToBean.parse();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return recordList;
    }

    private List<Record> generatePojoFromXML(MultipartFile file) {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        List<Record> recordList = new ArrayList<Record>();
        try {
            File xmlFile = new File(generatePath(file));
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("record");

            for (int i = 0; i < nodeList.getLength(); i++) {
                recordList.add(generateRecord(nodeList.item(i)));
            }

        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
        return recordList;
    }

    private Record generateRecord(Node nNode) {

        Record record = new Record();
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;

            record.setReference(Integer.parseInt(eElement.getAttribute("reference")));
            record.setAccountNumber(eElement.getElementsByTagName("accountNumber").item(0).getTextContent());
            record.setDescription(eElement.getElementsByTagName("description").item(0).getTextContent());
            record.setStartBalance(Double.parseDouble(eElement.getElementsByTagName("startBalance").item(0).getTextContent()));
            record.setMutation(Double.parseDouble(eElement.getElementsByTagName("mutation").item(0).getTextContent()));
            record.setEndBalance(Double.parseDouble(eElement.getElementsByTagName("endBalance").item(0).getTextContent()));

        }
        return record;
    }

    private ResponseEntity validateData(List<Record> recordList){
        boolean flag = false;
        java.util.HashSet unique = new HashSet();
        List<ValidateRecords> validateList = new ArrayList<>();
        for (Record record: recordList) {

            if(!unique.add(record.getReference()) || !validateEndBalance(record)){
                ValidateRecords validateData = new ValidateRecords();
                validateData.setReference(record.getReference());
                validateData.setDescription(record.getDescription());
                validateList.add(validateData);
                flag=true;
            }
        }
        if(flag) {
            return new ResponseEntity(validateList, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity(recordList, HttpStatus.OK);
        }
    }

    private String generatePath(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String fileName = file.getOriginalFilename();
        Path path = Paths.get(fileName);
        Files.write(path, bytes);
        return fileName;
    }

    private boolean validateEndBalance(Record record){
        DecimalFormat format = new DecimalFormat("##.00");
        Double endBalance = record.getEndBalance();
        Double startBalance = record.getStartBalance();
        Double mutation = record.getMutation();

        Double originalBalance = startBalance + mutation;

        if(format.format(originalBalance).equals(format.format(endBalance))){
            return true;
        }
        return false;
    }
}
