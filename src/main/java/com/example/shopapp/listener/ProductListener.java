package com.example.shopapp.listener;

import com.example.shopapp.models.Product;
import com.example.shopapp.service.IProductRedisService;
import com.example.shopapp.utils.SpringContext;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductListener {

    private static final Logger logger = LoggerFactory.getLogger(ProductListener.class);

    private IProductRedisService getProductRedisService() {
        return SpringContext.getBean(IProductRedisService.class);
    }

    @PostPersist
    public void postPersist(Product product) {
        logger.info("postPersist");
        getProductRedisService().clear();
    }

    @PostUpdate
    public void postUpdate(Product product) {
        logger.info("postUpdate");
        getProductRedisService().clear();
    }

    @PostRemove
    public void postRemove(Product product) {
        logger.info("postRemove");
        getProductRedisService().clear();
    }
}
