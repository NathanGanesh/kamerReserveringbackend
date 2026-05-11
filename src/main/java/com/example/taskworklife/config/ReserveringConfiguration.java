package com.example.taskworklife.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "reservering")
@Data
public class ReserveringConfiguration {
    String uploadPath;

    String profileImagesFolder = "profile";

    String kamerFolder = "kamer";
    String attachmentFolder = "attachment";

    public String getFullProfileImagesPath() {
        return this.uploadPath + "/" + this.profileImagesFolder;
    }

    public String getKamerFolder() {
        return this.uploadPath + "/" + this.kamerFolder;
    }

    public String getAttachmentFolder() {
        return this.uploadPath + "/" + this.attachmentFolder;
    }
}
