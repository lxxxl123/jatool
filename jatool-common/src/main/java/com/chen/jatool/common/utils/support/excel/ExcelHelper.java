package com.chen.jatool.common.utils.support.excel;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.export.ExcelExportService;
import cn.afterturn.easypoi.util.PoiMergeCellUtil;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import com.chen.jatool.common.exception.ServiceException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ExcelHelper {



    private Workbook workbook;

    private ExportParams exportParams;

    private Class<?> dataClass;

    public static ExcelHelper of() {
        ExcelHelper excelHelper = new ExcelHelper();
        return excelHelper;
    }

    /**
     * SXSSF > XSSF > HSSF(xls)`
     * 性能理论上最好
     */
    public static ExcelHelper ofSxss() {
        ExcelHelper excelHelper = new ExcelHelper();
        excelHelper.workbook = new SXSSFWorkbook();
        return excelHelper;
    }
    public static ExcelHelper ofXss() {
        ExcelHelper excelHelper = new ExcelHelper();
        excelHelper.workbook = new HSSFWorkbook();
        return excelHelper;
    }


    private ExportParams getExportParams() {
        if (exportParams == null) {
            exportParams = new ExportParams();
            exportParams.setStyle(QmsExportStyle.class);
        }
        return exportParams;
    }

    public ExcelHelper setExportParams(ExportParams exportParams) {
        this.exportParams = exportParams;
        return this;
    }

    public ExcelHelper dataClass(Class<?> clazz){
        if (clazz != null) {
            this.dataClass = clazz;
        }
        return this;
    }


    /**
     * 默认hssf
     * HSSF 97 xls , 最大65565行
     * XSSF 07 xlsx, 最大1048576行
     * SXSSF .xlsx
     */
    private ExcelHelper excelType(ExcelType type) {
        getExportParams().setType(type);
        return this;
    }

    public ExcelHelper title(String title) {
        ExportParams params = getExportParams();
        params.setTitle(title);
        if (title.endsWith(".xlsx")) {
            params.setType(ExcelType.XSSF);
        }
        return this;
    }


    public ExcelHelper setWorkbook(Workbook workbook) {
        this.workbook = workbook;
        return this;
    }

    private void tryBuildWorkBook() {
        if (workbook != null) {
            return;
        }
        // 性能上 SXSSF > XSSF > HSSF(xls)
        if (ExcelType.HSSF.equals(getExportParams().getType())) {
            this.workbook = new HSSFWorkbook();
        } else {
            this.workbook = new XSSFWorkbook();
        }
    }

    public ExcelHelper writeToWorkBook(String sheetName, Collection<?> data, List<? extends ExcelExportEntity> entityList) {
        tryBuildWorkBook();
        ExportParams param = getExportParams();
        param.setSheetName(sheetName);
        if (dataClass != null) {
            new ExcelExportService().createSheet(workbook, param, dataClass, data);
        } else {
            new QmsExportService().createSheetForMap(workbook, param, (List) entityList, data);
        }
        return this;
    }

    public void writeToOutputStream(OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
        if (workbook != null) {
            workbook.close();
        }
    }


    /**
     * 合并单元格
     */
    public ExcelHelper tryMerge(List<MergeEntity> mergeEntities, int sheetIndex) {
        if (CollUtil.isEmpty(mergeEntities)) {
            return this;
        }
        for (MergeEntity en : mergeEntities) {
            //由于标题和表头共占据两行 , row+2
            int row = en.getRow() + 2;
            PoiMergeCellUtil.addMergedRegion(workbook.getSheetAt(sheetIndex), row
                    , row + en.getRowspan() - 1
                    , en.getCol()
                    , en.getCol() + en.getColspan() - 1);

        }
        return this;
    }


}
