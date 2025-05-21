package com.example.shopapp.configuration;

import com.example.shopapp.dtos.request.OrderDTO;
import com.example.shopapp.models.Order;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(m -> m.skip(Order::setId));  // skip ID

        return mapper;
    }
}

