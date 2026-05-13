
package gradeflow.processor;

import java.io.*;
import java.util.zip.*;


public class ZipHandler {


    public File extract(File zipFile, File workDir) throws IOException {
        String studentId = zipFile.getName().replaceFirst("\\.zip$", "");
        File targetDir   = new File(workDir, studentId);
        targetDir.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File dest = new File(targetDir, entry.getName().replace('\\', '/'));

             
                if (!dest.toPath().normalize().startsWith(targetDir.toPath().normalize())) {
                    zis.closeEntry();
                    continue;
                }

                if (entry.isDirectory()) {
                    dest.mkdirs();
                } else {
                    dest.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(dest)) {
                        zis.transferTo(fos);
                    }
                }
                zis.closeEntry();
            }
        }
        return targetDir;
    }
}
