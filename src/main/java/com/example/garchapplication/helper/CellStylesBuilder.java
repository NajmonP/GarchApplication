package com.example.garchapplication.helper;

import com.example.garchapplication.model.enums.CellStyleNames;
import org.apache.poi.ss.usermodel.*;

import java.util.HashMap;
import java.util.Map;

public final class CellStylesBuilder {
    public static Map<String, CellStyle> getCellStylesForConfiguration(Workbook workbook) {
        Map<String, CellStyle> styles = getHeaderCellStyle(workbook);

        CellStyle modelName = workbook.createCellStyle();
        modelName.setAlignment(HorizontalAlignment.CENTER);
        modelName.setVerticalAlignment(VerticalAlignment.CENTER);
        modelName.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.index);
        modelName.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styles.put(CellStyleNames.MODEL_NAME.toString(), modelName);

        CellStyle parameterName = workbook.createCellStyle();
        parameterName.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.index);
        parameterName.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styles.put(CellStyleNames.PARAMETER_NAME.toString(), parameterName);
        return styles;
    }

    public static Map<String, CellStyle> getCellStylesForTimeSeries(Workbook workbook) {
        Map<String, CellStyle> styles = getHeaderCellStyle(workbook);

        CellStyle parameterName = workbook.createCellStyle();
        parameterName.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.index);
        parameterName.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styles.put(CellStyleNames.PARAMETER_NAME.toString(), parameterName);
        return styles;
    }

    private static Map<String, CellStyle> getHeaderCellStyle(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        CellStyle header = workbook.createCellStyle();
        header.setAlignment(HorizontalAlignment.CENTER);
        header.setVerticalAlignment(VerticalAlignment.CENTER);
        header.setFillForegroundColor(IndexedColors.ORANGE.index);
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styles.put(CellStyleNames.HEADER.toString(), header);
        return styles;
    }
}
