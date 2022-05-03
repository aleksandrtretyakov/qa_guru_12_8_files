package qa.guru;

import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Selenide;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selectors.byText;
import static org.assertj.core.api.Assertions.assertThat;

public class FilesParsingTest {
    ClassLoader cl = getClass().getClassLoader();

    @Test
    void parsePdfTest() throws IOException {

        Selenide.open("https://junit.org/junit5/docs/current/user-guide/");
        File pdfDownload = Selenide.$(byText("PDF download")).download();
        PDF pdf = new PDF(pdfDownload);
        assertThat(pdf.author).contains("Matthias Merdes");
    }

    @Test
    void parseXlsTest() throws FileNotFoundException {

        Selenide.open("https://ckmt.ru/price-download.html");
        File xlsDownload = Selenide.$("a[href*='Price.xls']").download();
        XLS xls = new XLS(xlsDownload);
        assertThat(xls.excel
                .getSheetAt(0)
                .getRow(9)
                .getCell(0)
                .getStringCellValue()).contains("Lincoln");
    }

    @Test
    void parseCsvTest() throws Exception {

        try (InputStream is = cl.getResourceAsStream("files/crash_catalonia.csv");
             CSVReader reader = new CSVReader(new InputStreamReader(is))) {
            List<String[]> content = reader.readAll();
            assertThat(content.get(0)).contains("Day of Week", "Number of Crashes");
        }
    }

    @Test
    void parseZipTest() throws Exception {

        try (InputStream is = cl.getResourceAsStream("files/sample-zip-file.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                assertThat(entry.getName()).isEqualTo("sample.txt");
            }
        }
    }

    @Test
    void zipTest() throws Exception {

        ZipFile zipFile = new ZipFile(new File(cl.getResource("sample-zip-file.zip").toURI()));
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {

            ZipEntry zipEntry = entries.nextElement();
            String fileName = zipEntry.getName();

            if (fileName.equals("TehresursPrice.xls")) {
                try (InputStream is = zipFile.getInputStream(zipEntry)) {
                    XLS xls = new XLS(is);
                    assertThat(xls.excel
                            .getSheetAt(0)
                            .getRow(9)
                            .getCell(0)
                            .getStringCellValue()).contains("Lincoln");
                }
            }

            if (fileName.equals("crash_catalonia.csv")) {
                try (InputStream is = zipFile.getInputStream(zipEntry)) {
                    CSVReader reader = new CSVReader(new InputStreamReader(is));
                    List<String[]> content = reader.readAll();
                    assertThat(content.get(0)).contains("Day of Week", "Number of Crashes");
                }
            }

            if (fileName.equals("junit-user-guide-5.8.2.pdf")) {
                try (InputStream is = zipFile.getInputStream(zipEntry)) {
                    PDF pdf = new PDF(is);
                    assertThat(pdf.author).contains("Matthias Merdes");

                }
            }

        }
    }
}

