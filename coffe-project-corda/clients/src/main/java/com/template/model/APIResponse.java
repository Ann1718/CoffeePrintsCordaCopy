
package com.template.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.serialization.CordaSerializable;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse<T> {
    private int code;
    private String timeStamp;
    private String description;
    private T data;

    public static <T> APIResponse<T> success() {
        return new APIResponse<>(HttpStatus.OK.value(), new Timestamp(System.currentTimeMillis()).toString(), "success", null);
    }

    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>(HttpStatus.OK.value(), new Timestamp(System.currentTimeMillis()).toString(), "success", data);
    }

    public static <T> APIResponse<T> error(T message) {
        return new APIResponse<>(HttpStatus.BAD_REQUEST.value(), new Timestamp(System.currentTimeMillis()).toString(), "error", message);
    }
}

