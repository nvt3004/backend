package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "image_features")
public class ImageFeature implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private int imageId;

    @Column(name = "image_name", length = 255)
    private String imageName;

    @Column(name = "descriptors", columnDefinition = "JSON")
    private String descriptors;

    // Thêm cột productId
    @Column(name = "product_id")
    private int productId;

    public ImageFeature() {
    }

    public ImageFeature(String imageName, String descriptors, int productId) {
        this.imageName = imageName;
        this.descriptors = descriptors;
        this.productId = productId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(String descriptors) {
        this.descriptors = descriptors;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageFeature)) return false;
        ImageFeature that = (ImageFeature) o;
        return imageId == that.imageId &&
                productId == that.productId &&
                Objects.equals(imageName, that.imageName) &&
                Objects.equals(descriptors, that.descriptors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId, imageName, descriptors, productId);
    }

    @Override
    public String toString() {
        return "ImageFeature{" +
                "imageId=" + imageId +
                ", imageName='" + imageName + '\'' +
                ", descriptors='" + descriptors + '\'' +
                ", productId=" + productId +
                '}';
    }
}
