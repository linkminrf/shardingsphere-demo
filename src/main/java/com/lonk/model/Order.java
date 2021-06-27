package com.lonk.model;

import java.io.Serializable;
import lombok.Data;

/**
 * t_order_0
 * @author 
 */
@Data
public class Order implements Serializable {

    private Long id;

    private Long orderId;

    private Long userId;

    private String username;

    private static final long serialVersionUID = 1L;

}