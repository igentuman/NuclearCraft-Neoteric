package igentuman.nc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import igentuman.nc.NuclearCraft;
import net.minecraftforge.fml.loading.FMLPaths;

public class FileExtractor {

    /**
     * Extracts all files from a specific folder inside the mod JAR and copies them to the config folder.
     *
     * @param sourceFolderPath The folder inside the JAR to extract.
     * @param targetFolderName The name of the folder to copy the files to in the config folder
     */
    public static void unpackFilesFromFolderToConfig(String sourceFolderPath, String targetFolderName) {
        // Get the Minecraft config directory
        Path configDir = FMLPaths.CONFIGDIR.get();

        // Define the target folder inside the config directory
        File targetFolder = new File(configDir.toFile(), targetFolderName);

        // Check if the target folder already exists
        if (targetFolder.exists()) {
            System.out.println("Folder " + targetFolderName + " already exists in config folder, skipping extraction.");
          //  return;
        }

        // If the folder doesn't exist, create it
        if (!targetFolder.mkdirs()) {
            System.err.println("Failed to create target folder: " + targetFolderName);
           // return;
        }
        // Find the JAR file where the resources are packaged
        String jarPath = FileExtractor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        jarPath = jarPath.replace("file:", "").replace("!/", ""); // Clean the path
        jarPath = jarPath.replaceAll("(.+\\.jar|/)(%23\\d+)?$", "$1");
        // Locate the JAR that contains the mod's resources
        try {

            File jarFile = new File(jarPath);
            NuclearCraft.LOGGER.error("JAR file path: " + jarFile.getAbsolutePath());
            // Open the JAR file and iterate through its entries
            try (ZipFile zipFile = new ZipFile(jarFile)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    // Check if the entry is inside the desired folder
                    if (entryName.startsWith(sourceFolderPath) && !entry.isDirectory()) {
                        // Calculate the relative path and target file name
                        String relativeFileName = entryName.substring(sourceFolderPath.length() + 1);
                        File targetFile = new File(targetFolder, relativeFileName);

                        // Ensure the parent directories exist
                        if (!targetFile.getParentFile().exists()) {
                            targetFile.getParentFile().mkdirs();
                        }

                        // Copy the file from the JAR to the config folder
                        try (InputStream inputStream = zipFile.getInputStream(entry)) {
                            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Extracted file " + relativeFileName + " to config folder.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            //if jarPath is directory then copy files from source folder to target folder
            if(new File(jarPath).isDirectory()) {
                try {
                    File sourceFolder = new File(jarPath + sourceFolderPath);
                    File[] files = sourceFolder.listFiles();
                    for (File file : files) {
                        File targetFile = new File(targetFolder, file.getName());
                        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Extracted file " + file.getName() + " to config folder.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
