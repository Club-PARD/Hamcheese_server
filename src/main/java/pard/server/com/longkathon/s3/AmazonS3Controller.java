package pard.server.com.longkathon.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class AmazonS3Controller {

    private final AwsS3Service awsS3Service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestPart("multipartFile") MultipartFile multipartFile
    ) {
        System.out.println("[upload] file null? " + (multipartFile == null));
        if (multipartFile != null) {
            System.out.println("[upload] name=" + multipartFile.getOriginalFilename());
            System.out.println("[upload] size=" + multipartFile.getSize());
            System.out.println("[upload] contentType=" + multipartFile.getContentType());
        }

        String fileName = awsS3Service.uploadFile(multipartFile);
        System.out.println("[upload] saved fileName=" + fileName);

        return ResponseEntity.ok(fileName);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam String fileName){
        awsS3Service.deleteFile(fileName);
        return ResponseEntity.ok(fileName);
    }
}