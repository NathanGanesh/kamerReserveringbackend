package com.example.taskworklife.controller;

import com.example.taskworklife.exception.GlobalExceptionHandler;
import com.example.taskworklife.exception.global.ChangeOnlyOwnUserException;
import com.example.taskworklife.exception.global.IoException;
import com.example.taskworklife.exception.images.ImageNotFoundException;
import com.example.taskworklife.exception.images.ImageTypeNotAllowedException;
import com.example.taskworklife.exception.images.ImagesExceededLimit;
import com.example.taskworklife.exception.images.ImagesNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNaamIsLeegException;
import com.example.taskworklife.exception.kamer.KamerNaamNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNotFoundException;
import com.example.taskworklife.exception.user.EmailNotFoundException;
import com.example.taskworklife.fileservice.FileService;
import com.example.taskworklife.models.user.UserPrincipal;
import com.example.taskworklife.service.file.ImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;

@Controller
@RequestMapping(path = "/images")
@CrossOrigin(origins = "http://localhost:3000")
public class ImagesController extends GlobalExceptionHandler {
    ImagesService imagesService;

    @Autowired
    public ImagesController(ImagesService imagesService) {
        this.imagesService = imagesService;
    }

    @PostMapping(value = "/kamer/{naam}/upload/images", consumes = {"multipart/mixed", "multipart/form-data"})
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<Boolean> handelKamerImage(@RequestPart("files") MultipartFile[] files, @PathVariable String naam) throws KamerNotFoundException, ImageTypeNotAllowedException, ImagesExceededLimit, ImagesNotFoundException, IOException, KamerNaamIsLeegException, KamerNaamNotFoundException, IoException {
        return new ResponseEntity<Boolean>(imagesService.saveKamerImage(naam, files),HttpStatus.OK);
    }

    @PostMapping(value = "/user/{email}/upload", consumes = {"multipart/mixed", "multipart/form-data"})
    @CrossOrigin(origins = "http://localhost:3000")
    public  ResponseEntity<Boolean> handleProfileImage(@RequestPart("file") MultipartFile file, Principal principal, @PathVariable("email") String email) throws EmailNotFoundException, ImageTypeNotAllowedException, IoException, IOException, ImageNotFoundException, ChangeOnlyOwnUserException {

        return new  ResponseEntity<Boolean>(imagesService.saveProfileImageUser(((UserPrincipal) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser().getEmail(), email,file), HttpStatus.OK);
    }

    @DeleteMapping(value = "/user/{email}/image/delete")
    @CrossOrigin(origins = "http://localhost:3000")
    public void deleteProfileImage(@PathVariable("email") String email, Principal principal) throws EmailNotFoundException, ChangeOnlyOwnUserException, ImageNotFoundException {
        imagesService.deleteProfileImageUser(((UserPrincipal) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser().getEmail(), email);
    }

}
