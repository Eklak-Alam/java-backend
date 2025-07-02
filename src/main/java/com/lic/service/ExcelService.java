package com.lic.service;

import com.lic.dto.StudentDTO;
import com.lic.entities.Student;
import com.lic.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    private static final List<String> DATE_FORMATS = Arrays.asList(
            "dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "MM/dd/yyyy"
    );

    private final StudentRepository studentRepository;

    public ExcelService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<StudentDTO> processFile(MultipartFile file) throws IOException {
        // Reset all lastUpload flags before processing new file
        try {
            studentRepository.resetLastUploadFlag();
        } catch (Exception e) {
            logger.warn("No records to update - proceeding with new upload");
        }

        validateFile(file);
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("File name not recognized");
        }

        try {
            List<StudentDTO> processedStudents;
            if (filename.endsWith(".xlsx")) {
                processedStudents = processExcelFile(file);
            } else if (filename.endsWith(".csv")) {
                processedStudents = processCsvFile(file);
            } else {
                throw new IllegalArgumentException("Unsupported file format");
            }

            // Save all processed students with lastUpload=true
            saveStudentsWithLastUploadFlag(processedStudents);

            return processedStudents;
        } catch (Exception e) {
            logger.error("Error processing file: {}", filename, e);
            throw new IOException("Failed to process file: " + e.getMessage(), e);
        }
    }

    private void saveStudentsWithLastUploadFlag(List<StudentDTO> studentDTOs) {
        List<Student> students = studentDTOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        studentRepository.saveAll(students);
    }

    private Student convertToEntity(StudentDTO dto) {
        return Student.builder()
                .srNo(dto.getSrNo())
                .name(dto.getName())
                .panNumber(dto.getPanNumber())
                .licRegdNumber(dto.getLicRegdNumber())
                .branch(dto.getBranch())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .lastUpload(true)
                .build();
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or null");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") &&
                        !contentType.equals("text/csv"))) {
            throw new IOException("Invalid file type");
        }
    }

    private List<StudentDTO> processExcelFile(MultipartFile file) throws IOException {
        List<StudentDTO> students = new ArrayList<>();
        Set<String> existingPanNumbers = getExistingPanNumbers();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (rows.hasNext()) rows.next(); // Skip header

            int rowNum = 1;
            while (rows.hasNext()) {
                rowNum++;
                try {
                    StudentDTO student = processExcelRow(rows.next(), existingPanNumbers);
                    if (student != null) {
                        students.add(student);
                        existingPanNumbers.add(student.getPanNumber());
                    }
                } catch (Exception e) {
                    logger.warn("Error processing row {}: {}", rowNum, e.getMessage());
                }
            }
        }
        return students;
    }

    private StudentDTO processExcelRow(Row row, Set<String> existingPanNumbers) {
        if (row == null) return null;

        String panNumber = getStringValue(row.getCell(2)).toUpperCase().trim();
        if (panNumber.isEmpty() || existingPanNumbers.contains(panNumber)) {
            return null;
        }

        StudentDTO student = new StudentDTO();
        student.setSrNo(getStringValue(row.getCell(0)));
        student.setName(normalizeName(getStringValue(row.getCell(1))));
        student.setPanNumber(panNumber);
        student.setLicRegdNumber(normalizeLicNumber(getStringValue(row.getCell(3))));
        student.setBranch(normalizeBranch(getStringValue(row.getCell(4))));
        student.setStartDate(parseDate(getStringValue(row.getCell(5))));
        student.setEndDate(parseDate(getStringValue(row.getCell(6))));

        return student;
    }

    private List<StudentDTO> processCsvFile(MultipartFile file) throws IOException {
        List<StudentDTO> students = new ArrayList<>();
        Set<String> existingPanNumbers = getExistingPanNumbers();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            br.readLine(); // Skip header

            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                try {
                    StudentDTO student = processCsvLine(line, existingPanNumbers);
                    if (student != null) {
                        students.add(student);
                        existingPanNumbers.add(student.getPanNumber());
                    }
                } catch (Exception e) {
                    logger.warn("Error processing CSV line {}: {}", lineNum, e.getMessage());
                }
            }
        }
        return students;
    }

    private StudentDTO processCsvLine(String line, Set<String> existingPanNumbers) {
        String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (values.length < 7) return null;

        String panNumber = cleanCsvValue(values[2]).toUpperCase().trim();
        if (panNumber.isEmpty() || existingPanNumbers.contains(panNumber)) {
            return null;
        }

        StudentDTO student = new StudentDTO();
        student.setSrNo(cleanCsvValue(values[0]));
        student.setName(normalizeName(cleanCsvValue(values[1])));
        student.setPanNumber(panNumber);
        student.setLicRegdNumber(normalizeLicNumber(cleanCsvValue(values[3])));
        student.setBranch(normalizeBranch(cleanCsvValue(values[4])));
        student.setStartDate(parseDate(cleanCsvValue(values[5])));
        student.setEndDate(parseDate(cleanCsvValue(values[6])));

        return student;
    }

    private String parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) return "";

        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return new SimpleDateFormat("dd-MM-yyyy").format(sdf.parse(dateString.trim()));
            } catch (ParseException ignored) {}
        }
        return dateString.trim();
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    private String normalizeLicNumber(String licNumber) {
        return licNumber == null ? "" : licNumber.trim().replaceAll("\\s+", "");
    }

    private String normalizeBranch(String branch) {
        return branch == null ? "" : branch.trim().replaceAll("\\s+", " ");
    }

    private Set<String> getExistingPanNumbers() {
        try {
            return new HashSet<>(studentRepository.findAllPanNumbers());
        } catch (Exception e) {
            logger.error("Error fetching PAN numbers", e);
            return new HashSet<>();
        }
    }

    private String cleanCsvValue(String value) {
        return value == null ? "" : value.trim().replaceAll("^\"|\"$", "");
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return "";

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    return DateUtil.isCellDateFormatted(cell)
                            ? new SimpleDateFormat("dd-MM-yyyy").format(cell.getDateCellValue())
                            : String.valueOf((long) cell.getNumericCellValue());
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    switch (cell.getCachedFormulaResultType()) {
                        case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
                        case STRING: return cell.getStringCellValue().trim();
                        default: return "";
                    }
                default: return "";
            }
        } catch (Exception e) {
            logger.debug("Error getting cell value", e);
            return "";
        }
    }
}