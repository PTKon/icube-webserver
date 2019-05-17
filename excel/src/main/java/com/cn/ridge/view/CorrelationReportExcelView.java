package com.cn.ridge.view;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.web.servlet.view.document.AbstractXlsxStreamingView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/26
 */
public class CorrelationReportExcelView extends AbstractXlsxStreamingView {
    @SuppressWarnings("NullableProblems")
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        String excelName = "correlation-report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("uMMdd-HHmmss")) + ".xls";
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(excelName, "utf-8"));
        response.setContentType("application/ms-excel; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        //Excel workbook
        SXSSFWorkbook sxssfWorkbook = (SXSSFWorkbook) workbook;
        @SuppressWarnings("unchecked") List<Map<String, Object>> mapList = (List<Map<String, Object>>) model.get("mapList");
        //sheet
        Sheet sheet = sxssfWorkbook.createSheet("数据表单");
        //默认列宽
        sheet.setDefaultColumnWidth(20);
        //字体
        Font font = sxssfWorkbook.createFont();
        font.setCharSet(Font.DEFAULT_CHARSET);
        font.setFontName("Arial");
        font.setBold(true);
        //style
        CellStyle cellStyle = sxssfWorkbook.createCellStyle();
        cellStyle.setFont(font);
        //style1
        CellStyle cellStyle1 = sxssfWorkbook.createCellStyle();
        DataFormat dataFormat = sxssfWorkbook.createDataFormat();
        //首列
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("关键资源实例");
        header.getCell(0).setCellStyle(cellStyle);
        header.createCell(1).setCellValue("关键资源Kpi");
        header.getCell(1).setCellStyle(cellStyle);
        header.createCell(2).setCellValue("关键资源Instance");
        header.getCell(2).setCellStyle(cellStyle);
        header.createCell(3).setCellValue("关键资源样本数量");
        header.getCell(3).setCellStyle(cellStyle);
        header.createCell(4).setCellValue("相关资源实例");
        header.getCell(4).setCellStyle(cellStyle);
        header.createCell(5).setCellValue("相关资源Kpi");
        header.getCell(5).setCellStyle(cellStyle);
        header.createCell(6).setCellValue("相关资源Instance");
        header.getCell(6).setCellStyle(cellStyle);
        header.createCell(7).setCellValue("相关资源样本数量");
        header.getCell(7).setCellStyle(cellStyle);
        header.createCell(8).setCellValue("相关性系数");
        header.getCell(8).setCellStyle(cellStyle);
        if (CollectionUtils.isNotEmpty(mapList))//非空
            IntStream.range(0, mapList.size()).forEach(i -> {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> map = mapList.get(i);
                row.createCell(0).setCellValue((String) map.get("mainCiName"));
                row.createCell(1).setCellValue((String) map.get("mainKpiName"));
                row.createCell(2).setCellValue((String) map.get("mainInstanceId"));
                row.createCell(3).setCellValue(Long.valueOf(String.valueOf(map.get("mainSampleCount"))));
                row.createCell(4).setCellValue((String) map.get("subCiName"));
                row.createCell(5).setCellValue((String) map.get("subKpiName"));
                row.createCell(6).setCellValue((String) map.get("subInstanceId"));
                row.createCell(7).setCellValue(Long.valueOf(String.valueOf(map.get("subSampleCount"))));
                String result = String.valueOf(map.get("result"));
                row.createCell(8).setCellValue(new BigDecimal(result).floatValue());
                if (!"0".equals(result) && !"0.0".equals(result)) {
                    int point = result.indexOf(".");
                    if (point != -1) {
                        int length = result.substring(result.indexOf(".") + 1).length();
                        switch (length) {
                            case 1:
                                cellStyle1.setDataFormat(dataFormat.getFormat("0.0"));
                                row.getCell(8).setCellStyle(cellStyle1);
                                break;
                            case 2:
                                cellStyle1.setDataFormat(dataFormat.getFormat("0.00"));
                                row.getCell(8).setCellStyle(cellStyle1);
                                break;
                            case 3:
                                cellStyle1.setDataFormat(dataFormat.getFormat("0.000"));
                                row.getCell(8).setCellStyle(cellStyle1);
                                break;
                            case 4:
                                cellStyle1.setDataFormat(dataFormat.getFormat("0.0000"));
                                row.getCell(8).setCellStyle(cellStyle1);
                                break;
                            default:
                                break;
                        }
                    }
                }
            });
    }

}
