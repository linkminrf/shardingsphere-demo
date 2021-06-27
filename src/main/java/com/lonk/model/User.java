package com.lonk.model;

import java.io.Serializable;
import lombok.Data;

/**
 * t_user
 * @author 
 */
@Data
public class User implements Serializable {
    private Long id;

    private String username;

    private static final long serialVersionUID = 1L;
}