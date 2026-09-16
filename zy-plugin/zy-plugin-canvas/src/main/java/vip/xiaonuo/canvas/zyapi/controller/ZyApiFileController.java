package vip.xiaonuo.canvas.zyapi.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.dev.api.DevFileApi;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传Controller（画布C端）
 *
 * <p>上传图片接口：仅允许图片格式，内部调用 DevFileApi 动态上传文件返回url
 * （使用系统配置的默认文件引擎），返回带签名的公开下载地址，可直接在
 * {@code <img>} 等场景访问，无需登录。
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Tag(name = "文件上传")
@RestController
@RequestMapping("/files")
public class ZyApiFileController {

    /** 仅允许上传的图片扩展名 */
    private static final List<String> ALLOWED_IMAGE_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    @Resource
    private DevFileApi devFileApi;

    /**
     * 上传图片，返回文件信息与可访问URL
     *
     * @param file   图片文件
     * @param width  图片宽度（可选，前端直传元数据）
     * @param height 图片高度（可选，前端直传元数据）
     */
    @Operation(summary = "上传图片返回url")
    @PostMapping
    public CommonResult<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file,
                                                         @RequestParam(value = "width", required = false) Integer width,
                                                         @RequestParam(value = "height", required = false) Integer height) {
        if (file == null || file.isEmpty()) {
            throw new CommonException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = StrUtil.isBlank(originalFilename) ? "" : FileUtil.extName(originalFilename).toLowerCase();
        if (StrUtil.isEmpty(extension) || !ALLOWED_IMAGE_EXT.contains(extension)) {
            throw new CommonException("仅支持上传 jpg/jpeg/png/gif/bmp/webp 格式的图片");
        }

        // 动态上传文件返回url（使用系统配置的默认文件引擎），URL 形如：
        // {backend-url}/dev/file/download?id={fileId}&sign={sign}（公开、带签名、无需登录）
        String url = devFileApi.uploadDynamicReturnUrl(file);
        String fileId = StrUtil.subBetween(url, "id=", "&");
        if (StrUtil.isEmpty(fileId)) {
            fileId = StrUtil.subAfter(url, "id=", false);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", fileId);
        result.put("filename", originalFilename);
        result.put("storageKey", "devfile:" + fileId);
        result.put("mimeType", file.getContentType());
        result.put("bytes", file.getSize());
        result.put("url", url);
        if (width != null) {
            result.put("width", width);
        }
        if (height != null) {
            result.put("height", height);
        }
        return CommonResult.data(result);
    }
}
