/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decompostion.Dictionaries;






import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by faehndrich on 25.04.16.
 */
public class DictUtil {

    private static final int BUFFER_SIZE = 4096000;

    public static void unzip(String zipFilePath, String destDirectory) {
        ProgressBar bar = new ProgressBar();
        System.out.println("Extracting File : " + zipFilePath + " to " + destDirectory);

        Runnable updatethread = new Runnable() {
            public void run() {
                try {
//                    File destDir = new File(destDirectory);
//                    if (!destDir.exists()) {
//                        destDir.mkdir();
//                    }
                    File outfile = new File(destDirectory);
                    if (!outfile.getParentFile().exists()) {
                        outfile.getParentFile().mkdirs();
                    }
                    if (!outfile.exists()) {
                        outfile.createNewFile();
                    }
                    File infile = new File(zipFilePath);
                    if (!infile.getParentFile().exists()) {
                        infile.getParentFile().mkdirs();
                    }
                    if (!infile.exists()) {
                        infile.createNewFile();
                    }
                    FileInputStream fileInputStream = new FileInputStream(infile);

                    ZipInputStream zipIn = new ZipInputStream(fileInputStream);
                    ZipEntry entry = zipIn.getNextEntry();
                    long totalSize = infile.length();
                    long counter = 0;
                    // iterates over entries in the zip file
                    while (entry != null) {
                        counter += entry.getSize();
                        final long fcounter = counter;
                        String filePath = destDirectory + File.separator + entry.getName();
                        if (!entry.isDirectory()) {
                            // if the entry is a file, extracts it
                            counter += extractFile(zipIn, filePath);
                        } else {
                            // if the entry is a directory, make the directory
                            File dir = new File(filePath);
                            dir.mkdir();
                        }
                        zipIn.closeEntry();
                        entry = zipIn.getNextEntry();
                        // update progress bar

                        Thread t = new Thread(new Runnable() {
                            public void run() {
                                bar.update(fcounter, totalSize);
                            }
                        });
                        t.start();
                    }
                    zipIn.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        Thread downloadThrea = new Thread(updatethread);
        downloadThrea.start();
        try {
            downloadThrea.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void deleteFile(String path) {
        File file2delete = new File(path);
        if (file2delete.exists()) {
            file2delete.delete();
        }

    }

    private static long extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        long totalBytezWriten = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
            totalBytezWriten += read;
        }
        bos.close();

        return totalBytezWriten;
    }

    /**
     * This created the Word to vec ANN in the home folder under .decomposition/corpusLinguisticDictionary/ANN
     */
    public static void downloadFile(String source, String destination) throws IOException {
        File f = new File(destination);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        if (!f.exists()) {
            f.createNewFile();
        }
        URL sourceUrl = new URL(source);
        ReadableByteChannel rbc = Channels.newChannel(sourceUrl.openStream());
        FileOutputStream fos = new FileOutputStream(destination);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        //unpack file TODO: define how the extracted content is named
        //unzip(path2Vec + File.separator +corpusFileName,path2Vec + File.separator +corpusFileName+".txt");


    }

    /**
     * Doenload a file found at source to the destination. Showing a progress bar in the console.
     *
     * @param source      an path (URL) to geht the file form
     * @param destination a path to where the file should be writen.
     */
    public static void downloadFileParalell(String source, String destination) {
        ProgressBar bar = new ProgressBar();
        System.out.println("Starting download of: " + source);
        ExecutorService executor = Executors.newCachedThreadPool();
        Runnable updatethread = new Runnable() {
            public void run() {
                try {

                    URL url = new URL(source);
                    //HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                    URLConnection connection =  url.openConnection();
                    long completeFileSize = connection.getContentLength();
                    bar.update(0, completeFileSize);
                    BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                    File f = new File(destination);
                    if (!f.getParentFile().exists()) {
                        f.getParentFile().mkdirs();
                    }
                    if (!f.exists()) {
                        f.createNewFile();
                    }
                   FileOutputStream fos = new java.io.FileOutputStream(
                            destination);
                    BufferedOutputStream bout = new BufferedOutputStream(
                            fos, BUFFER_SIZE);
                    byte[] data = new byte[BUFFER_SIZE];
                    long downloadedFileSize = 0;
                    int x = 0;
                    while ((x = in.read(data, 0, BUFFER_SIZE)) >= 0) {
                        downloadedFileSize += x;
                        // calculate progress
                        final long currentDownload = downloadedFileSize;
                        bout.write(data, 0, x);
                        //bout.flush();

                        // update progress bar
                        executor.execute(() -> bar.update(currentDownload, completeFileSize));
                    }
                    bout.close();
                    in.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        };
        //pause the rest of the program until the download is complete
        Thread downloadThrea = new Thread(updatethread);
        //executor.execute(downloadThrea);
        downloadThrea.start();
        //executor.shutdown();
        try {
            downloadThrea.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Doenload a file found at source to the destination. Showing a progress bar in the console.
     *
     * @param source      an path (URL) to geht the file form
     * @param destination a path to where the file should be writen.
     */
    public static void downloadBigFile(String source, String destination) {
        ProgressBar bar = new ProgressBar();
        System.out.println("Starting download of: " + source);
        ExecutorService executor = Executors.newCachedThreadPool();
        Runnable updatethread = new Runnable() {
            public void run() {
//                try {
//                    long completeFileSize = httpConnection.getContentLength();
//                    bar.update(0, completeFileSize);
                    File dstFile = null;
                    // check the directory for existence.
                    String dstFolder = destination.substring(0, destination.lastIndexOf(File.separator));
                    if (!(dstFolder.endsWith(File.separator) || dstFolder.endsWith("/")))
                        dstFolder += File.separator;

                    // Creates the destination folder if doesn't not exists
                    dstFile = new File(dstFolder);
                    if (!dstFile.exists()) {
                        dstFile.mkdirs();
                    }
                    try {
                        URL url = new URL(source);
                        FileUtils.copyURLToFile(url, dstFile);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


//                        Thread t = new Thread(new Runnable() {
//                            public void run() {
//                                bar.update(currentDownload, completeFileSize);
//                            }
//                        });
//                        executor.execute(t);
//                        bout.write(data, 0, x);
//                    }catch (){
//
//                }

            }
        };
        Thread downloadThrea = new Thread(updatethread);
        downloadThrea.start();
        try {
            downloadThrea.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
