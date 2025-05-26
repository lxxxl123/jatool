package com.chen.jatool.common.utils.support.excel;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.entity.vo.BaseEntityTypeConstants;
import cn.afterturn.easypoi.excel.export.styler.ExcelExportStylerDefaultImpl;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.utils.SpelUtils;
import com.chen.jatool.common.utils.StringUtil;
import org.apache.poi.ss.usermodel.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenwh3
 */
public class QmsExportStyle extends ExcelExportStylerDefaultImpl {

    /**
     * 单元格格式-常规
     */
    private static final short GENERAL_FORMAT = (short) BuiltinFormats.getBuiltinFormat("General");

    private final CellStyle generalCellStyle;

    private final Map<String, CellStyle> STYLE_MAP = new HashMap<>();

    public QmsExportStyle(Workbook workbook) {
        super(workbook);
        generalCellStyle = generalStyle(workbook);
        STYLE_MAP.put("redFont", redFontStyle(workbook));
    }

    public CellStyle generalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(GENERAL_FORMAT);
        return style;
    }

    public CellStyle redFontStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        style.setDataFormat(GENERAL_FORMAT);
        return style;
    }

    @Override
    public CellStyle getStyles(Cell cell, int dataRow, ExcelExportEntity entity, Object obj, Object data) {
        if (entity instanceof QmsExcelExportEntity) {
            String styleType = ((QmsExcelExportEntity) entity).getStyleType();
            if (StrUtil.isNotBlank(styleType)) {
                Map<String, Object> map = new HashMap<>(2);
                map.put("cellValue", StringUtil.toNotNullStr(data));
                map.put("styleType", styleType);

                styleType = SpelUtils.parseStr(styleType, map);

                CellStyle res;
                if (StrUtil.isNotBlank(styleType) && (res = STYLE_MAP.get(styleType)) != null) {
                    return res;
                }
            }
        }


        return getStyles(dataRow % 2 == 1, entity);
    }

    @Override
    public CellStyle getStyles(boolean noneStyler, ExcelExportEntity entity) {
        if (entity instanceof QmsExcelExportEntity) {
            String styleType = ((QmsExcelExportEntity) entity).getStyleType();
            CellStyle res;
            if (StrUtil.isNotBlank(styleType) && (res = STYLE_MAP.get(styleType)) != null) {
                return res;
            }
        }

        if (entity != null && entity.getType() == BaseEntityTypeConstants.DOUBLE_TYPE) {
            return generalCellStyle;
        }

        // 默认是文本
        return super.getStyles(noneStyler, entity);
    }


}
