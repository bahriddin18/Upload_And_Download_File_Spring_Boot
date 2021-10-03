package com.example.demo.controller;
import com.example.demo.entity.ApiResponse;
import com.example.demo.entity.Attachment;
import com.example.demo.entity.AttachmentContent;
import com.example.demo.reposistory.AttachmentContentRepository;
import com.example.demo.reposistory.AttachmentRepository;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {
    final
    AttachmentRepository attachmentRepository;
    final
    AttachmentContentRepository attachmentContentRepository;
    private static final String uploadDirectorys = "files";

    public AttachmentController(AttachmentRepository attachmentRepository, AttachmentContentRepository attachmentContentRepository) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentContentRepository = attachmentContentRepository;
    }
 /*   upload database
    postgresql
    @PostMapping("/upload")

    public ApiResponse saveToDb(MultipartHttpServletRequest request) throws IOException {
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        if (file != null) {
            Attachment attachment = new Attachment();
            attachment.setName(file.getOriginalFilename());
            attachment.setSize(file.getSize());
            attachment.setType(file.getContentType());

            Attachment save = attachmentRepository.save(attachment);

            AttachmentContent attachmentContent = new AttachmentContent();
            attachmentContent.setAttachment(save);
            attachmentContent.setBytes(file.getBytes());

            attachmentContentRepository.save(attachmentContent);
            return new ApiResponse("Saved !", true, attachment);
            byte[] bytes = file.getBytes();
            String name = file.getName();
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
        }
        return new ApiResponse("Error upload file!", false);
    }
*/


/*    single upload

    @PostMapping("/uploadSystem")
    public String uploadFileSystem(MultipartHttpServletRequest request) throws IOException {
        Iterator<String> fileNames = request.getFileNames();

        while (fileNames.hasNext()) {
            MultipartFile file = request.getFile(fileNames.next());
            if (file != null) {
                String originalFilename = file.getOriginalFilename();

                Attachment attachment = new Attachment();
                attachment.setName(originalFilename);
                attachment.setSize(file.getSize());
                attachment.setType(file.getContentType());

                String[] split = originalFilename.split("\\.");
                String name = UUID.randomUUID().toString() + "." + split[split.length - 1];
                attachment.setName(name);
                attachmentRepository.save(attachment);

                Path path = Paths.get(uploadDirectorys + "/" + originalFilename);
                Files.copy(file.getInputStream(), path);
                return "SAVED" + attachment.getId();
            }
        }
        return "NOT SAVED";
    }*/

    // Multi upload
    @PostMapping("/uploadSystem")
    public boolean saveMultiple(MultipartHttpServletRequest request) throws IOException {
        Iterator<String> fileNames = request.getFileNames();
        while (fileNames.hasNext()) {
            MultipartFile file = request.getFile(fileNames.next());
            if (file != null) {
                String originalFilename = file.getOriginalFilename();
                Attachment attachment = new Attachment();
                attachment.setName(originalFilename);
                attachment.setSize(file.getSize());
                attachment.setType(file.getContentType());
                attachment.setKiritilganName(originalFilename);

                attachmentRepository.save(attachment);
                Path path = Paths.get(uploadDirectorys + "/" + originalFilename);
                Files.copy(file.getInputStream(), path);
            } else {
                return false;
            }
        }
        return true;
    }


    @GetMapping("/downloadSytem/{id}")
    public void getone(@PathVariable Integer id, HttpServletResponse response) throws IOException {

        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            response.setHeader("Content-Disposition", "attachment; filename=\"" + attachment.getName() + "\"");

            response.setContentType(attachment.getType());
            FileInputStream fileInputStream = new FileInputStream(uploadDirectorys + "/" + attachment.getName());
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        }
    }

    @GetMapping("/download/{id}")
    public void getFromDb(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            Optional<AttachmentContent> optionalAttachmentContent = attachmentContentRepository.findByAttachmentId(id);
            if (optionalAttachmentContent.isPresent()) {
                AttachmentContent attachmentContent = optionalAttachmentContent.get();
                response.setContentType(attachment.getType());
                response.setHeader("Content-Disposition", attachment.getName() + "/:" + attachment.getSize());
                FileCopyUtils.copy(attachmentContent.getBytes(), response.getOutputStream());
            }

        }
    }

    @GetMapping("/download")
    public void getAll(HttpServletResponse response) throws IOException {
        List<Attachment> all = attachmentRepository.findAll();
        for (Attachment attachment : all) {
            List<AttachmentContent> all1 = attachmentContentRepository.findAll();
            for (AttachmentContent attachmentContent : all1) {
                response.setContentType(attachment.getType());
                response.setHeader("Content-Disposition", attachment.getName() + "/:" + attachment.getSize());
                FileCopyUtils.copy(attachmentContent.getBytes(), response.getOutputStream());
            }
        }
    }

    @GetMapping("/info/{id}")
    public ApiResponse getOne(@PathVariable Integer id) {
        Optional<Attachment> byId = attachmentRepository.findById(id);
        return byId.map(attachment -> new ApiResponse("FOUND", true, attachment)).orElseGet(() -> new ApiResponse("NOT FOUND", false));
    }

    @GetMapping("/info")
    public ApiResponse getOne() {
        return new ApiResponse("FOUND", true, attachmentRepository.findAll());
    }
}
