package com.example.taskworklife.controller;

import com.example.taskworklife.exception.images.ImageTypeNotAllowedException;
import com.example.taskworklife.exception.images.ImagesExceededLimit;
import com.example.taskworklife.exception.images.ImagesNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNaamIsLeegException;
import com.example.taskworklife.exception.kamer.KamerNaamNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNotFoundException;
import com.example.taskworklife.service.file.ImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping(path = "/images")
@CrossOrigin(origins = "http://localhost:3000")
public class ImagesController {
    ImagesService imagesService;

    @Autowired
    public ImagesController(ImagesService imagesService) {
        this.imagesService = imagesService;
    }

    @PostMapping(value = "/kamer/{naam}/upload/images", consumes = {"multipart/mixed", "multipart/form-data"})
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<Boolean> handelKamerImagePost(@RequestPart("files") MultipartFile[] files, @PathVariable String naam) throws KamerNotFoundException, ImageTypeNotAllowedException, ImagesExceededLimit, ImagesNotFoundException, IOException, KamerNaamIsLeegException, KamerNaamNotFoundException {
//        System.out.println(files);
        return new ResponseEntity<Boolean>(imagesService.saveKamerImage(naam, files),HttpStatus.OK);
    }


}
