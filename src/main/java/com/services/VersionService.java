package com.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.entities.*;
import com.repositories.SaleJPA;
import com.responsedto.SaleProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.models.OptionDTO;
import com.repositories.AttributeOptionsVersionJPA;
import com.repositories.ImageJPA;
import com.repositories.ProductVersionJPA;
import com.responsedto.Attribute;
import com.responsedto.ProductVersionResponse;
import com.responsedto.StockQuantityDTO;
import com.utils.UploadService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VersionService {
    @Autowired
    UploadService uploadService;

    @Autowired
    ProductVersionJPA versionJPA;

    @Autowired
    AttributeOptionsVersionJPA attributeOptionsVersionJPA;

    @Autowired
    ImageJPA imageJPA;
    

    public void addVersion(ProductVersionResponse versionModel, Product product) {
        ProductVersion versionEntity = new ProductVersion();

        versionEntity.setVersionName(versionModel.getVersionName());
        versionEntity.setRetailPrice(versionModel.getRetailPrice());
        versionEntity.setImportPrice(versionModel.getImportPrice());
        versionEntity.setStatus(true);
        versionEntity.setProduct(product);

        ProductVersion versionSaved = versionJPA.save(versionEntity);
        saveImageVersion(versionSaved, versionModel.getImage().getName());
        saveAttributeOptionVersion(versionSaved, versionModel.getAttributes());
    }

    public void updateVersion(ProductVersionResponse versionModel) {
        ProductVersion versionEntity = versionJPA.findById(versionModel.getId()).orElse(null);

        versionEntity.setVersionName(versionModel.getVersionName());
        versionEntity.setRetailPrice(versionModel.getRetailPrice());
        versionEntity.setImportPrice(versionModel.getImportPrice());

        ProductVersion versionSaved = versionJPA.save(versionEntity);

        String imageName = changeNewImage(versionModel, versionEntity);
        if (imageName != null) {
            Image imageEntity = new Image();

            imageEntity.setImageUrl(imageName);
            imageEntity.setProductVersion(versionSaved);

            imageJPA.save(imageEntity);
        }
    }

    private String changeNewImage(ProductVersionResponse versionModel, ProductVersion version) {
        String imageNameVersion = versionModel.getImage().getName();
        String fileName = null;

        if (imageNameVersion != null && !imageNameVersion.isBlank() && !imageNameVersion.isEmpty()) {
            uploadService.delete(version.getImage().getImageUrl(), "images");
            imageJPA.delete(version.getImage());
            fileName = uploadService.save(imageNameVersion, "images");
        }

        return fileName;
    }

    private void saveImageVersion(ProductVersion version, String img) {

        String fileName = uploadService.save(img, "images");

        Image imageEntity = new Image();

        imageEntity.setImageUrl(fileName);
        imageEntity.setProductVersion(version);

        imageJPA.save(imageEntity);
    }

    private void saveAttributeOptionVersion(ProductVersion version, List<Attribute> attribute) {
        for (Attribute op : attribute) {
            AttributeOptionsVersion mapping = new AttributeOptionsVersion();
            AttributeOption option = new AttributeOption();

            option.setId(op.getId());

            mapping.setProductVersion(version);
            mapping.setAttributeOption(option);

            attributeOptionsVersionJPA.save(mapping);
        }
    }

    public boolean isExitVersionInProduct(Product product, List<Attribute> attributeRes) {
        boolean reslut = false;
        for (ProductVersion vs : product.getProductVersions()) {
            List<AttributeOptionsVersion> opvs = vs.getAttributeOptionsVersions();
            int lengthOption = opvs.size();
            int countCheck = 0;

            for (AttributeOptionsVersion opv : opvs) {
                AttributeOption attributeOption = opv.getAttributeOption();
                for (Attribute pdres : attributeRes) {
                    if (attributeOption.getId() == pdres.getId()) {
                        countCheck += 1;
                    }
                }
            }

            if (lengthOption > 0 && countCheck == lengthOption) {
                reslut = true;
                break;
            }
        }

        return reslut;
    }

    @Transactional
    public Integer getTotalStockQuantityVersion(int versionId) {
        return versionJPA.getTotalStockQuantityVersion(versionId);
    }
}
