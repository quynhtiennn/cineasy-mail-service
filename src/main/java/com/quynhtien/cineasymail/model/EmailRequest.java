package com.quynhtien.cineasymail.model;

import com.quynhtien.cineasymail.enums.RequestTypeEnum;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
public class EmailRequest {
    String recipient;
    UUID tokenId;
}
