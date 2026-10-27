package com.graddu.oa.common.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.graddu.oa.common.api.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Tag(name = "通用文件上传与访问接口", description = "支持单据附件、材料扫描件本地存储与在线读取")
@RestController
@RequestMapping("/common")
public class FileUploadController {

    private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "单据附件文件上传")
    public R<String> uploadFile(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("上传文件不能为空");
        }

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String ext = FileUtil.extName(originalFilename);
            String newFileName = IdUtil.simpleUUID() + (ext.isEmpty() ? "" : "." + ext);
            File dest = new File(dir, newFileName);
            file.transferTo(dest);

            String fileUrl = "/api/common/file/" + newFileName;
            log.info("文件上传成功: original={}, stored={}, url={}", originalFilename, newFileName, fileUrl);
            return R.ok("上传成功", fileUrl);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    @SaIgnore
    @GetMapping("/file/{fileName}")
    @Operation(summary = "文件在线预览与下载")
    public void getFile(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        File file = new File(uploadDir + fileName);
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "inline; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件失败", e);
        }
    }
}
