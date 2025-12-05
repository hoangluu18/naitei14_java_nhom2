package vn.sun.membermanagementsystem.services.csv;

import java.io.IOException;
import java.io.OutputStream;

public interface CsvExportService<T> {

    void exportToCsv(OutputStream outputStream) throws IOException;

    String[] getExportHeaders();
}
