package com.iuniverse.service;

import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Material;
import com.iuniverse.model.Module;
import com.iuniverse.repository.MaterialRepository;
import com.iuniverse.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final ModuleRepository moduleRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public Material uploadAndLinkToModule(MultipartFile file, Long moduleId, String customTitle, String type) {
        // 1. Upload file lấy content_url
        String contentUrl = fileUploadService.uploadFile(file);

        // 2. Kiểm tra Module có tồn tại không
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module does not exist with ID: " + moduleId));

        // 3. Tạo mới Material
        Material material = Material.builder()
                .title(customTitle != null ? customTitle : file.getOriginalFilename())
                .type(type != null ? type : "PDF") // Mặc định là PDF nếu không truyền
                .contentUrl(contentUrl)
                .build();

        material = materialRepository.save(material);

        // 4. Thiết lập mối quan hệ Many-to-Many
        // Lưu ý: Trong Module.java bạn cần có: Set<Material> materials = new HashSet<>();
        module.getMaterials().add(material);
        moduleRepository.save(module);

        return material;
    }
}