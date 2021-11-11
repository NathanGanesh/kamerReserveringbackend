package com.example.taskworklife.repo;

import com.example.taskworklife.models.FileAttachment;
import com.example.taskworklife.models.Kamer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FileAttachmentRepo extends JpaRepository<FileAttachment, Long> {



}