package com.example.taskworklife.service.file;

import com.example.taskworklife.config.ReserveringConfiguration;
import com.example.taskworklife.exception.images.ImageTypeNotAllowedException;
import com.example.taskworklife.exception.images.ImagesExceededLimit;
import com.example.taskworklife.exception.images.ImagesNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNaamIsLeegException;
import com.example.taskworklife.exception.kamer.KamerNaamNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNotFoundException;
import com.example.taskworklife.fileservice.FileService;
import com.example.taskworklife.models.FileAttachment;
import com.example.taskworklife.models.Kamer;
import com.example.taskworklife.repo.FileAttachmentRepo;
import com.example.taskworklife.repo.KamerRepo;
import com.example.taskworklife.service.kamer.KamerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ImagesServiceImpl implements ImagesService {

    FileService fileService;
    KamerService kamerService;
    ReserveringConfiguration reserveringConfiguration;
    FileAttachmentRepo fileAttachmentRepository;
    KamerRepo kamerRepo;
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    public ImagesServiceImpl(FileService fileService, KamerService kamerService, ReserveringConfiguration reserveringConfiguration,KamerRepo kamerRepo, FileAttachmentRepo fileAttachmentRepository) {
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileService = fileService;
        this.kamerRepo = kamerRepo;
        this.kamerService = kamerService;
        this.reserveringConfiguration = reserveringConfiguration;
    }


    public Kamer deleteAllKamerImages(Kamer kamer) throws IOException {
        List<FileAttachment> attachments = kamer.getAttachments();
        if (attachments.size() > 0) {
            kamer.setAttachments(new ArrayList<>());
        }
        Files.deleteIfExists(Paths.get(reserveringConfiguration.getKamerFolder() + "/" + kamer.getNaam()));
        LOGGER.info("alle kamer attachments zijn weggehaald voor kamer " + kamer.getNaam());
        return kamer;
    }

    public void deleteKamerImage(String naam) {
        try {
            FileUtils.deleteQuietly(new File(reserveringConfiguration.getKamerFolder() + "/" + naam));
            Files.deleteIfExists(Paths.get(reserveringConfiguration.getKamerFolder() + "/" + naam));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void maakDirectory(String naamVanDirectory){
        File directory = new File(reserveringConfiguration.getKamerFolder() + "/" + naamVanDirectory);
        if (!directory.exists()){
           directory.mkdir();
        }
    }

    @Override
    public boolean saveKamerImage(String kamerNaam, MultipartFile[] files) throws KamerNotFoundException, ImageTypeNotAllowedException, ImagesExceededLimit, ImagesNotFoundException, IOException, KamerNaamIsLeegException, KamerNaamNotFoundException {
        //check if files are not null
        if (files != null) {
            //check if length is not 0
            if (files.length != 0) {
                //check if naam is not empty
                if (StringUtils.isNotBlank(kamerNaam)) {
                    if (files.length > 8) {
                        throw new ImagesExceededLimit("Niet meer dan 8 images");
                    }
                    //check if naam exits in db
                    Kamer kamerByNaam = kamerService.getKamerByNaam(kamerNaam);
                    // detect if image
                    for (MultipartFile file : files) {
                        if (!fileService.detectIfImage(Objects.requireNonNull(file.getContentType()))) {
                            throw new ImageTypeNotAllowedException("Image type is niet toegestaan alleen png, jpg, jpeg");
                        }
                    }
                    Date date;
                    //delete all kamerimages
                    File kamerDirectory = new File(reserveringConfiguration.getKamerFolder() +"/"+kamerByNaam);
                    FileUtils.deleteQuietly(kamerDirectory);
                    maakDirectory(kamerNaam);
                    for (MultipartFile file : files) {
                        date = new Date();
                        FileAttachment fileAttachment = new FileAttachment();
                        fileAttachment.setDate(date);

                        try {
                            byte[] fileAsByte = file.getBytes();
//                            File target = new File
//                    (reserveringConfiguration.getKamerFolder() + "/"+ naam + "/"+ files[0].getOriginalFilename());
                            try{

                            File target = new File(reserveringConfiguration.getKamerFolder() + "/" + kamerNaam + "/" + file.getOriginalFilename());
                            FileUtils.writeByteArrayToFile(target, fileAsByte);
                            }catch (Exception e){
                                System.out.println(e.getMessage());
                            }

                            fileAttachment.setFileType(file.getContentType());
                            fileAttachment.setName(file.getOriginalFilename());
                            kamerByNaam.addFileAttachment(fileAttachment);
                            LOGGER.info("Saved image with name " + file.getOriginalFilename() + " for room " + kamerNaam);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                            LOGGER.error("io exception in image");
                            LOGGER.info("io exception in image");
                            e.printStackTrace();
                        }
                    }
//                    kamerByNaam.setAttachments(fileAttachments);
                    kamerRepo.save(kamerByNaam);
                    return true;
                } else {
                    throw new KamerNaamIsLeegException("Kamer niet gevonden met de naam " + kamerNaam);
                }
            } else {
                throw new ImagesNotFoundException("Geen images gevonden");
            }
        } else {
            throw new ImagesNotFoundException("Geen images gevonden");
        }
    }


}
