package com.lic.service;

import com.lic.dto.StudentDTO;
import com.lic.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class ExcelService {

    private final StudentRepository studentRepository;

    public ExcelService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<StudentDTO> processFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("File name not recognized");
        }

        if (filename.endsWith(".xlsx")) {
            return processExcelFile(file);
        } else if (filename.endsWith(".csv")) {
            return processCsvFile(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Only Excel (.xlsx) and CSV (.csv) are supported");
        }
    }

    private List<StudentDTO> processExcelFile(MultipartFile file) throws IOException {
        List<StudentDTO> students = new ArrayList<>();
        Set<String> existingPanNumbers = getExistingPanNumbers();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip header row if exists
            if (rows.hasNext()) rows.next();

            while (rows.hasNext()) {
                Row row = rows.next();
                String panNumber = getStringValue(row.getCell(2)).toUpperCase();

                // Skip if PAN number already exists
                if (existingPanNumbers.contains(panNumber)) {
                    continue;
                }

                StudentDTO student = new StudentDTO();
                student.setSrNo(getIntValue(row.getCell(0)));
                student.setName(getStringValue(row.getCell(1)));
                student.setPanNumber(panNumber);
                student.setLicRegdNumber(getStringValue(row.getCell(3)));
                student.setBranch(getStringValue(row.getCell(4)));
                student.setStartDate(getStringValue(row.getCell(5)));
                student.setEndDate(getStringValue(row.getCell(6)));

                students.add(student);
                existingPanNumbers.add(panNumber); // Add to set to prevent duplicates in same file
            }
        }
        return students;
    }

    private List<StudentDTO> processCsvFile(MultipartFile file) throws IOException {
        List<StudentDTO> students = new ArrayList<>();
        Set<String> existingPanNumbers = getExistingPanNumbers();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Skip header line if exists
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (values.length < 7) continue;

                String panNumber = cleanCsvValue(values[2]).toUpperCase();

                // Skip if PAN number already exists
                if (existingPanNumbers.contains(panNumber)) {
                    continue;
                }

                StudentDTO student = new StudentDTO();
                student.setSrNo(parseIntSafe(values[0]));
                student.setName(cleanCsvValue(values[1]));
                student.setPanNumber(panNumber);
                student.setLicRegdNumber(cleanCsvValue(values[3]));
                student.setBranch(cleanCsvValue(values[4]));
                student.setStartDate(cleanCsvValue(values[5]));
                student.setEndDate(cleanCsvValue(values[6]));

                students.add(student);
                existingPanNumbers.add(panNumber); // Add to set to prevent duplicates in same file
            }
        }
        return students;
    }

    private Set<String> getExistingPanNumbers() {
        // Fetch all existing PAN numbers from database
        return new HashSet<>(studentRepository.findAllPanNumbers());
    }

    private String cleanCsvValue(String value) {
        return value == null ? "" : value.trim().replaceAll("^\"|\"$", "");
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(cleanCsvValue(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private int getIntValue(Cell cell) {
        if (cell == null) return 0;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                return parseIntSafe(cell.getStringCellValue());
            default:
                return 0;
        }
    }
}