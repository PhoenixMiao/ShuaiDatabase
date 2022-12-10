package com.phoenix.shuaidatabase.test;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ExcelOutput {
    private static final String outputPath = "src\\main\\resources\\test\\output.xlsx";


    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;

    public ExcelOutput(String sheetName) throws Exception {
        FileInputStream inputStream = new FileInputStream(outputPath);
        workbook = new XSSFWorkbook(inputStream);
        sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
    }

    private int findRowNumber(String type) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) continue;
            XSSFCell cell = row.getCell(row.getFirstCellNum());
            if (type.equals(cell.getStringCellValue())) return i;
        }
        return sheet.getLastRowNum() + 1;
    }

    public void writeExcel(String[] information) {
        String commandType = information[0];
        int rowNumber = findRowNumber(commandType);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputPath);
            XSSFRow row = sheet.getRow(rowNumber);
            if (row == null) {
                row = sheet.createRow(rowNumber);
            }
            for (int i = 0; i < information.length; i++) {
                XSSFCell cell = row.createCell(i);
                cell.setCellValue(information[i]);

            }

            workbook.write(out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
