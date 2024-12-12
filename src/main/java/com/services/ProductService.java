package com.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.aot.generate.InMemoryGeneratedFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.entities.AttributeOption;
import com.entities.AttributeOptionsVersion;
import com.entities.Category;
import com.entities.Image;
import com.entities.Product;
import com.entities.ProductCategory;
import com.entities.ProductSale;
import com.entities.ProductVersion;
import com.models.CategoryDTO;
import com.models.OptionDTO;
import com.models.OrderDTO;
import com.models.ProductDTO;
import com.models.VersionDTO;
import com.repositories.AttributeOptionsVersionJPA;
import com.repositories.CategoryJPA;
import com.repositories.ImageJPA;
import com.repositories.ProductCategoryJPA;
import com.repositories.ProductCustomJPA;
import com.repositories.ProductJPA;
import com.repositories.ProductVersionJPA;
import com.responsedto.Attribute;
import com.responsedto.AttributeProductResponse;
import com.responsedto.ImageResponse;
import com.responsedto.PageCustom;
import com.responsedto.ProductDetailResponse;
import com.responsedto.ProductHomeResponse;
import com.responsedto.ProductResponse;
import com.responsedto.ProductVersionResponse;
import com.responsedto.Version;
import com.utils.UploadService;

import com.entities.Product;
import com.repositories.ProductJPA;

@Service
public class ProductService {

    @Autowired
    ProductCustomJPA productCustomJPA;

    @Autowired
    ProductJPA productJPA;

    @Autowired
    CategoryJPA categoryJPA;

    @Autowired
    ProductJPA productJpa;

    @Autowired
    ProductVersionJPA productVersionJPA;

    @Autowired
    ImageJPA imageJPA;

    @Autowired
    VersionService versionService;

    @Autowired
    AttributeOptionsVersionJPA attributeOptionsVersionJPA;

    @Autowired
    UploadService uploadService;

    @Autowired
    AttributeService attributeService;

    @Autowired
    ProductCategoryJPA productCategoryJPA;

    @Autowired
    VersionService vsService;

    public PageCustom<ProductHomeResponse> getProducts(int page, int size) {
        return productCustomJPA.getAllProducts(page, size);
    }

    public PageCustom<ProductHomeResponse> getProducts(int page, int size, int categoryId) {
        return productCustomJPA.getAllProductsByCategory(page, size, categoryId);
    }

    public PageImpl<ProductResponse> getProductsByKeyword(int page, int size, boolean status, String keyword) {
        Sort sort = Sort.by(Sort.Direction.DESC, "productId");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productJPA.getAllProductByKeyword(status, keyword, pageable);

        List<ProductResponse> productResponses = products.getContent().stream().map(this::createProductResponse)
                .toList();

        PageImpl<ProductResponse> result = new PageImpl<ProductResponse>(productResponses, pageable,
                products.getTotalElements());

        return result;
    }

    public List<ProductResponse> getAllProductsByKeyword(int page, int size, boolean status, String keyword) {
        List<Product> products = productJPA.getAllProductByKeyword(true, keyword);

        List<ProductResponse> productResponses = products.stream().map(this::createProductResponse)
                .toList();

        return productResponses;
    }

    public PageImpl<ProductResponse> getProductsByKeywordAndCategory(int page, int size, int idCat, boolean status,
                                                                     String keyword) {
        Sort sort = Sort.by(Sort.Direction.DESC, "productId");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productJPA.getAllProductByKeywordAndCategory(status, keyword, idCat, pageable);

        List<ProductResponse> productResponses = products.getContent().stream().map(this::createProductResponse)
                .toList();

        PageImpl<ProductResponse> result = new PageImpl<ProductResponse>(productResponses, pageable,
                products.getTotalElements());

        return result;
    }

    private ProductResponse createProductResponse(Product product) {
        ProductResponse response = new ProductResponse();

        response.setId(product.getProductId());
        response.setPrice(product.getProductPrice());
        response.setProductName(product.getProductName());
        response.setDiscription(product.getDescription());
        response.setDiscount(getDiscount(product));
        response.setImage(uploadService.getUrlImage(product.getProductImg()));
        response.setStatus(product.isStatus());

        response.setCategories(product.getProductCategories().stream().map(item -> {
            CategoryDTO cat = new CategoryDTO();

            cat.setId(item.getCategory().getCategoryId());
            cat.setName(item.getCategory().getCategoryName());

            return cat;
        }).toList());

        Integer stockTotal = 0;

        for (ProductVersion vs : product.getProductVersions()) {
            int stockQuantity = versionService.getTotalStockQuantityVersion(vs.getId());
            stockTotal += stockQuantity;
        }

        response.setTotalStock(stockTotal);

        List<ProductVersionResponse> versions = product.getProductVersions().stream().map(vs -> {
            ProductVersionResponse version = new ProductVersionResponse();
            int stockQuantity = versionService.getTotalStockQuantityVersion(vs.getId());
            version.setId(vs.getId());
            version.setVersionName(vs.getVersionName());
            version.setRetailPrice(vs.getRetailPrice());
            version.setImportPrice(vs.getImportPrice());
            version.setQuantity(stockQuantity);
            version.setActive(vs.isStatus() && product.isStatus());

            if (vs.getImage() != null) {
                Image img = vs.getImage();
                ImageResponse imgres = new ImageResponse();
                imgres.setId(img.getImageId());
                imgres.setName(uploadService.getUrlImage(img.getImageUrl()));

                version.setImage(imgres);
            }

            List<Attribute> attributes = getAllAttributeByVersion(vs);
            version.setAttributes(attributes);

            return version;
        }).toList();

        response.setVersions(versions);

        return response;
    }

    private float getDiscount(Product product) {
        List<ProductSale> sales = product.getProductSales();
        long now = new Date().getTime();

        if (sales.isEmpty()) {
            return 0;
        }

        ProductSale productSale = sales.get(0);
        if (now < productSale.getStartDate().getTime() || now > productSale.getEndDate().getTime()) {
            return 0;
        }

        return productSale.getDiscount();
    }

    public ProductDetailResponse getProductDetail(int idProduct) {
        Product product = productJPA.findById(idProduct).get();
        List<Version> versions = new ArrayList<>();
        List<AttributeProductResponse> productAttributes = attributeService.getAttributeProduct(idProduct);

        ProductHomeResponse productParrent = new ProductHomeResponse();
        List<ProductSale> sales = product.getProductSales();

        productParrent.setId(product.getProductId());
        productParrent.setProductName(product.getProductName());
        productParrent.setImage(uploadService.getUrlImage(product.getProductImg()));
        productParrent.setDiscount(sales.size() <= 0 ? 0 : sales.get(0).getDiscount());
        productParrent.setDescription(product.getDescription());
        productParrent.setActive(product.isStatus());

        int inStockProductParent = 0;
        BigDecimal minPrice = product.getProductVersions().get(0).getRetailPrice();
        BigDecimal maxPrice = product.getProductVersions().get(0).getRetailPrice();

        for (ProductVersion vs : product.getProductVersions()) {
            Version versionDto = new Version();

            // Tồn kho
            int stock = vsService.getTotalStockQuantityVersion(vs.getId());
            inStockProductParent += stock;

            // Min price
            if (vs.getRetailPrice().compareTo(minPrice) == -1) {
                minPrice = vs.getRetailPrice();
            }
            // Max price
            if (vs.getRetailPrice().compareTo(minPrice) == 1) {
                maxPrice = vs.getRetailPrice();
            }

            List<Attribute> attributes = getAllAttributeByVersion(vs);
            String imageUrl = null;
            if (vs.getImage() != null) {
                imageUrl = vs.getImage().getImageUrl();
            }

            versionDto.setId(vs.getId());
            versionDto.setVersionName((vs.getVersionName()));
            versionDto.setPrice(vs.getRetailPrice());
            versionDto.setQuantity(stock);
            versionDto.setActive(vs.isStatus() && product.isStatus());
            versionDto.setImage(imageUrl);
            versionDto.setAttributes(attributes);

            versions.add(versionDto);
        }
        productParrent.setMinPrice(minPrice);
        productParrent.setMaxPrice(maxPrice);
        productParrent.setInStock(Long.valueOf(inStockProductParent));

        return new ProductDetailResponse(productParrent, versions, productAttributes);
    }

    public Product getProductById(int id) {
        Product product = productJPA.findById(id).orElse(null);

        return product != null && product.isStatus() ? product : null;
    }

    private List<Attribute> getAllAttributeByVersion(ProductVersion version) {

        List<Attribute> list = new ArrayList<>();
        for (AttributeOptionsVersion options : version.getAttributeOptionsVersions()) {

            int id = options.getAttributeOption() != null ? options.getAttributeOption().getId() : null;

            String key = options.getAttributeOption() != null
                    ? options.getAttributeOption().getAttribute().getAttributeName()
                    : null;
            String value = options.getAttributeOption() != null ? options.getAttributeOption().getAttributeValue()
                    : null;

            list.add(new Attribute(id, key, value));
        }

        return list;
    }

    public Product saveProduct(Product product) {
        return productJpa.save(product);
    }

    public Product findProductById(Integer productId) {
        Optional<Product> product = productJpa.findById(productId);
        if (product.isPresent()) {
            return product.get();
        } else {
            throw new RuntimeException("Product not found with id: " + productId);
        }
    }

    public Product createProduct(ProductDTO productModel) {
        Product productEntity = setProduct(productModel);
        Product productSaved = productJpa.save(productEntity);

        saveProductCategory(productSaved, productModel.getCategories());
        saveProductVersion(productSaved, productModel.getVersions());

        return productJpa.findById(productSaved.getProductId()).orElse(null);
    }

    public Product updateProduct(ProductDTO productModel, Product product) {
        Product productEntity = setProduct(productModel);
        productEntity.setProductId(productModel.getId());

        // Nếu truyền lên ảnh thì cập nhật còn không thì để trống
        productEntity.setProductImg(changeNewImage(productModel));

        // Sản phẩm chính
        Product productSaved = productJPA.save(productEntity);
        // Danh mục sản phẩm
        removeCategoryProduct(productModel);
        saveCategoryProduct(productModel);

        return productSaved;
    }

    private void removeCategoryProduct(ProductDTO productModel) {
        List<ProductCategory> productCategories = productCategoryJPA
                .getAllProductCategoryByProductId(productModel.getId());

        for (ProductCategory catEntity : productCategories) {
            boolean check = false;

            for (CategoryDTO cat : productModel.getCategories()) {
                if (catEntity.getCategory().getCategoryId() == cat.getId()) {
                    check = true;
                    break;
                }
            }

            if (!check) {
                int idProductCategory = catEntity.getPrdCatId();
                productCategoryJPA.deleteById(idProductCategory);
            }
        }
    }

    private void saveCategoryProduct(ProductDTO productModel) {
        List<ProductCategory> productCategories = productCategoryJPA
                .getAllProductCategoryByProductId(productModel.getId());

        for (CategoryDTO catDTO : productModel.getCategories()) {
            boolean check = false;

            for (ProductCategory catEntity : productCategories) {
                if (catDTO.getId() == catEntity.getCategory().getCategoryId()) {
                    check = true;
                    break;
                }
            }

            if (!check) {
                ProductCategory productCategory = createProductCategory(productModel.getId(), catDTO.getId());
                productCategoryJPA.save(productCategory);
            }

        }
    }

    private ProductCategory createProductCategory(int idProduct, int idCategory) {
        ProductCategory productCat = new ProductCategory();
        Product product = new Product();
        Category cat = new Category();
        cat.setCategoryId(idCategory);
        product.setProductId(idProduct);

        productCat.setCategory(cat);
        productCat.setProduct(product);

        return productCat;
    }

    private Product setProduct(ProductDTO productModel) {
        Product product = new Product();

        product.setProductName(productModel.getName());
        product.setProductPrice(productModel.getPrice());
        product.setDescription(productModel.getDescription());
        if (productModel.getImage() != null && !productModel.getImage().isEmpty()
                && !productModel.getImage().isBlank()) {
            product.setProductImg(uploadService.save(productModel.getImage(), "images"));
        }
        product.setStatus(true);

        return product;
    }

    private String changeNewImage(ProductDTO productModel) {
        Product product = productJPA.findById(productModel.getId()).orElse(null);
        String fileName = "";

        if (productModel.getImage() != null && !productModel.getImage().isBlank()
                && !productModel.getImage().isEmpty()) {
            uploadService.delete(product.getProductImg(), "images");
            fileName = uploadService.save(productModel.getImage(), "images");
            System.out.println("Vô đây -----------------------------------------");
        } else {
            fileName = product.getProductImg();
            System.out.println("Đéo ------------------------------------------------");
        }

        return fileName;
    }

    private void saveProductCategory(Product product, List<CategoryDTO> productCategories) {
        for (CategoryDTO cat : productCategories) {
            ProductCategory productCategoryEntity = new ProductCategory();
            Category category = new Category();
            category.setCategoryId(cat.getId());

            productCategoryEntity.setCategory(category);
            productCategoryEntity.setProduct(product);

            productCategoryJPA.save(productCategoryEntity);
        }
    }

    private void saveProductVersion(Product product, List<VersionDTO> versions) {

        for (VersionDTO vs : versions) {
            ProductVersion version = new ProductVersion();

            version.setProduct(product);
            version.setVersionName(vs.getVersionName());
            version.setRetailPrice(vs.getRetalPrice());
            version.setImportPrice(vs.getImportPrice());
            version.setStatus(true);

            ProductVersion versionSaved = productVersionJPA.save(version);

            saveAttributeOptionVersion(versionSaved, vs.getAttributes());
            saveImageVersion(versionSaved, vs.getImages());
        }
    }

    private void saveAttributeOptionVersion(ProductVersion version, List<OptionDTO> attribute) {
        for (OptionDTO op : attribute) {
            AttributeOptionsVersion mapping = new AttributeOptionsVersion();
            AttributeOption option = new AttributeOption();

            option.setId(op.getId());

            mapping.setProductVersion(version);
            mapping.setAttributeOption(option);

            attributeOptionsVersionJPA.save(mapping);
        }
    }

    private void saveImageVersion(ProductVersion version, List<String> images) {
        for (String img : images) {
            String fileName = uploadService.save(img, "images");

            Image imageEntity = new Image();

            imageEntity.setImageUrl(fileName);
            imageEntity.setProductVersion(version);

            imageJPA.save(imageEntity);
        }
    }

    public boolean removeProduct(int id) {
        try {
            Product product = productJPA.findById(id).get();

            product.setStatus(false);

            productJPA.save(product);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ProductResponse refreshSelectedProduct(int id) {
        return productJPA.findById(id).map(this::createProductResponse) // Chuyển đổi Product sang ProductResponse
                .orElse(null); // Trả về null nếu không tìm thấy Product
    }

}
