package com.example.shopapp.controllers;


import com.example.shopapp.dtos.responses.ResponseObject;
import com.example.shopapp.models.Role;
import com.example.shopapp.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController
{
    private final IRoleService roleService;

    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllRoles()
    {
        List<Role> roles= roleService.findAllRoles();
        return ResponseEntity.ok().body(ResponseObject.builder()
            .message("Get roles successfully")
            .status(HttpStatus.OK)
            .data(roles)
            .build());
    }

}
