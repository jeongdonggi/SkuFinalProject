package com.SecurityBoardEx.BoardEx.file.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceTest {
    @Autowired FileService fileService;

    private MockMultipartFile getmockUploadFile() throws IOException{
        return new MockMultipartFile("file","file.png","image/jpg",
                new FileInputStream("C:/Users/dkrha/Desktop/diary.png"));
    }

    @Test
    public void 파일저장_성공() throws Exception{
        // given, when
        String filePath = fileService.save(getmockUploadFile());

        // then
        File file = new File(filePath);
        assertThat(file.exists()).isTrue();

        //finally
        file.delete(); // 파일 삭제
    }

    @Test
    public void 파일삭제_성공() throws Exception{
        // given, when
        String filePath = fileService.save(getmockUploadFile());
        fileService.delete(filePath);

        // then
        File file = new File(filePath);
        assertThat(file.exists()).isFalse();
    }
}