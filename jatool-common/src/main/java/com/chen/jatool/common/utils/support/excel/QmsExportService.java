package com.chen.jatool.common.utils.support.excel;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.entity.vo.BaseEntityTypeConstants;
import cn.afterturn.easypoi.excel.entity.vo.PoiBaseConstants;
import cn.afterturn.easypoi.excel.export.ExcelExportService;
import cn.afterturn.easypoi.exception.excel.ExcelExportException;
import cn.afterturn.easypoi.exception.excel.enums.ExcelExportEnum;
import cn.afterturn.easypoi.util.PoiMergeCellUtil;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.poi.ss.usermodel.*;

import java.util.Collection;
import java.util.List;

/**
 * 可自定义样式
 * @author chenwh3
 */
public class QmsExportService extends ExcelExportService {

    protected int currentIndex;


    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }
    protected int createIndexCell(Row row, int index, ExcelExportEntity excelExportEntity) {
        if (excelExportEntity.getFormat() != null && excelExportEntity.getFormat().equals(PoiBaseConstants.IS_ADD_INDEX)) {
            createStringCell(row, 0, currentIndex + "",
                    index % 2 == 0 ? getStyles(false, null) : getStyles(true, null), null);
            currentIndex = currentIndex + 1;
            return 1;
        }
        return 0;
    }

    /**
     * 获取样式
     */
    public CellStyle getStyles(int dataRow, ExcelExportEntity entity, Object value) {
        return excelExportStyler.getStyles(null, dataRow, entity, null, value);
    }


    /**
     * 创建 最主要的 Cells
     */
    public int[] createCells(Drawing patriarch, int index, Object t,
                             List<ExcelExportEntity> excelParams, Sheet sheet, Workbook workbook,
                             short rowHeight, int cellNum) {
        try {
            ExcelExportEntity entity;
            Row row = sheet.getRow(index) == null ? sheet.createRow(index) : sheet.getRow(index);
            if (rowHeight != -1) {
                row.setHeight(rowHeight);
            }
            int maxHeight = 1, listMaxHeight = 1;
            // 合并需要合并的单元格
            int margeCellNum = cellNum;
            int indexKey     = 0;
            if (excelParams != null && !excelParams.isEmpty()) {
                indexKey = createIndexCell(row, index, excelParams.get(0));
            }
            cellNum += indexKey;
            for (int k = indexKey, paramSize = excelParams.size(); k < paramSize; k++) {
                entity = excelParams.get(k);
                //不论数据是否为空都应该把该列的数据跳过去
                if (entity.getList() != null) {
                    Collection<?> list          = getListCellValue(entity, t);
                    int           tmpListHeight = 0;
                    if (list != null && list.size() > 0) {
                        int tempCellNum = 0;
                        for (Object obj : list) {
                            int[] temp = createCells(patriarch, index + tmpListHeight, obj, entity.getList(), sheet, workbook, rowHeight, cellNum);
                            tempCellNum = temp[1];
                            tmpListHeight += temp[0];
                        }
                        cellNum = tempCellNum;
                        listMaxHeight = Math.max(listMaxHeight, tmpListHeight);
                    } else {
                        cellNum = cellNum + getListCellSize(entity.getList());
                    }
                } else {
                    Object value = getCellValue(entity, t);

                    if (entity.getType() == BaseEntityTypeConstants.STRING_TYPE) {
                        createStringCell(row, cellNum++, value == null ? "" : value.toString(),
                                getStyles(index, entity, value),
                                entity);

                    } else if (entity.getType() == BaseEntityTypeConstants.DOUBLE_TYPE) {
                        createDoubleCell(row, cellNum++, value == null ? "" : value.toString(),
                                getStyles(index, entity, value),
                                entity);
                    } else {
                        createImageCell(patriarch, entity, row, cellNum++,
                                value == null ? "" : value.toString(), t);
                    }
                    if (entity.isHyperlink()) {
                        row.getCell(cellNum - 1)
                                .setHyperlink(dataHandler.getHyperlink(
                                        row.getSheet().getWorkbook().getCreationHelper(), t,
                                        entity.getName(), value));
                    }
                }
            }
            maxHeight += listMaxHeight - 1;
            if (indexKey == 1 && excelParams.get(1).isNeedMerge()) {
                excelParams.get(0).setNeedMerge(true);
            }
            for (int k = indexKey, paramSize = excelParams.size(); k < paramSize; k++) {
                entity = excelParams.get(k);
                if (entity.getList() != null) {
                    margeCellNum += entity.getList().size();
                } else if (entity.isNeedMerge() && maxHeight > 1) {
                    for (int i = index + 1; i < index + maxHeight; i++) {
                        if (sheet.getRow(i) == null) {
                            sheet.createRow(i);
                        }
                        sheet.getRow(i).createCell(margeCellNum);
                        sheet.getRow(i).getCell(margeCellNum).setCellStyle(getStyles(false, entity));
                    }
                    PoiMergeCellUtil.addMergedRegion(sheet, index, index + maxHeight - 1, margeCellNum, margeCellNum);
                    margeCellNum++;
                }
            }
            return new int[]{maxHeight, cellNum};
        } catch (Exception e) {
            LOGGER.error("excel cell export error ,data is :{}", ReflectionToStringBuilder.toString(t));
            LOGGER.error(e.getMessage(), e);
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, e);
        }

    }


}
