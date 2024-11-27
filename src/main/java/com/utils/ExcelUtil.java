package com.utils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

    public static ByteArrayOutputStream createExcelFile(String sheetName, String[] headers, Object[][] data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Tạo style cho header
            CellStyle headerStyle = createHeaderCellStyle(workbook);

            // Tạo style cho dữ liệu
            CellStyle dataStyle = createDataCellStyle(workbook);

            // Tạo hàng header
            createHeaderRow(sheet, headers, headerStyle);

            // Tạo các hàng dữ liệu
            createDataRows(sheet, data, dataStyle);

            // Tự động điều chỉnh độ rộng của các cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Excel file: " + e.getMessage(), e);
        }
    }

    private static CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THICK);
        headerStyle.setBorderTop(BorderStyle.THICK);
        headerStyle.setBorderRight(BorderStyle.THICK);
        headerStyle.setBorderLeft(BorderStyle.THICK);
        return headerStyle;
    }

    private static CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        return dataStyle;
    }

    private static void createHeaderRow(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 6000); // Độ rộng cột
        }
    }

    private static void createDataRows(Sheet sheet, Object[][] data, CellStyle dataStyle) {
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
                } else if (rowData[j] instanceof BigDecimal) {
                    cell.setCellValue(((BigDecimal) rowData[j]).doubleValue());
                }
                cell.setCellStyle(dataStyle);
            }
        }
    }
}
