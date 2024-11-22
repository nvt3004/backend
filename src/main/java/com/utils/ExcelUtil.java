package com.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;

public class ExcelUtil {

    public static ByteArrayOutputStream createExcelFile(String sheetName, String[] headers, Object[][] data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Tạo style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Tạo hàng header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000); // Thiết lập độ rộng cột (có thể tùy chỉnh)
            }

            // Tạo các hàng dữ liệu
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            for (int i = 0; i < data.length; i++) {
                Row dataRow = sheet.createRow(i + 1);
                Object[] rowData = data[i];
                for (int j = 0; j < rowData.length; j++) {
                    Cell cell = dataRow.createCell(j);
                    if (rowData[j] instanceof String) {
                        cell.setCellValue((String) rowData[j]);
                    } else if (rowData[j] instanceof Double) {
                        cell.setCellValue((Double) rowData[j]);
                    } else if (rowData[j] instanceof Integer) {
                        cell.setCellValue((Integer) rowData[j]);
                    }
                    cell.setCellStyle(dataStyle);
                }
            }

            workbook.write(outputStream);
            return outputStream;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Excel file: " + e.getMessage(), e);
        }
    }
}

