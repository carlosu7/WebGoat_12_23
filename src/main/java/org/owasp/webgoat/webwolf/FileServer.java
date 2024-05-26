/*
 * This file is part of WebGoat, an Open Web Application Security Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2019 Bruce Mayhew
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * Getting Source ==============
 *
 * Source for this application is maintained at https://github.com/WebGoat/WebGoat, a repository for free software projects.
 */

package org.owasp.webgoat.webwolf;

import io.github.pixee.security.Filenames;
import static org.springframework.http.MediaType.ALL_VALUE;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.owasp.webgoat.webwolf.user.WebGoatUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/** Controller for uploading a file */
@Controller
@Slf4j
public class FileServer {

  @Value("${webwolf.fileserver.location}")
  private String fileLocation;

  @Value("${server.address}")
  private String server;

  @Value("${server.port}")
  private int port;

  @RequestMapping(
      path = "/file-server-location",
      consumes = ALL_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String getFileLocation() {
    return fileLocation;
  }

  @PostMapping(value = "/fileupload")
  public ModelAndView importFile(@RequestParam("file") MultipartFile myFile) throws IOException {
    var user = (WebGoatUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var destinationDir = new File(fileLocation, user.getUsername());
    destinationDir.mkdirs();
    myFile.transferTo(new File(destinationDir, Filenames.toSimpleFileName(myFile.getOriginalFilename())));
    log.debug("File saved to {}", new File(destinationDir, Filenames.toSimpleFileName(myFile.getOriginalFilename())));

    return new ModelAndView(
        new RedirectView("files", true),
        new ModelMap().addAttribute("uploadSuccess", "File uploaded successful"));
  }

  @AllArgsConstructor
  @Getter
  private class UploadedFile {
    private final String name;
    private final String size;
    private final String link;
  }

  @GetMapping(value = "/files")
  public ModelAndView getFiles(HttpServletRequest request) {
    WebGoatUser user =
        (WebGoatUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = user.getUsername();
    File destinationDir = new File(fileLocation, username);

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("files");
    File changeIndicatorFile = new File(destinationDir, user.getUsername() + "_changed");
    if (changeIndicatorFile.exists()) {
      modelAndView.addObject("uploadSuccess", request.getParameter("uploadSuccess"));
    }
    changeIndicatorFile.delete();

    var uploadedFiles = new ArrayList<>();
    File[] files = destinationDir.listFiles(File::isFile);
    if (files != null) {
      for (File file : files) {
        String size = FileUtils.byteCountToDisplaySize(file.length());
        String link = String.format("files/%s/%s", username, file.getName());
        uploadedFiles.add(new UploadedFile(file.getName(), size, link));
      }
    }

    modelAndView.addObject("files", uploadedFiles);
    modelAndView.addObject("webwolf_url", "http://" + server + ":" + port);
    return modelAndView;
  }

        public void print2() {
        String x = "test";
        if (x.equals("test")) {
            System.out.println("Hello, World!");
        }
    }

    public static class EncryptionExample {

        public byte[] encrypt(String text) throws Exception {
            int a;
            int b;
            int c;

            a = 2;
            b = 1;
            c = 3;
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024+a+b+c); // Weak key length
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            Key publicKey = keyPair.getPublic();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // Insecure padding
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(text.getBytes());
        }

        public static void main(String[] args) throws Exception {
            EncryptionExample example = new EncryptionExample();
            byte[] encrypted = example.encrypt("Sensitive Data");
            System.out.println("Encrypted: " + new String(encrypted));
        }
    }
}
