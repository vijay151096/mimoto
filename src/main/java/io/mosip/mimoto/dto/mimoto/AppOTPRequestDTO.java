package io.mosip.mimoto.dto.mimoto;

import java.util.List;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

@Data
public class AppOTPRequestDTO {
    @NotNull
    private String individualId;
    @NotNull
    @Pattern(regexp = "UIN|VID")
    private String individualIdType;
    @NotEmpty
    @NotNull
    private List<String> otpChannel;
    @NotNull
    private String transactionID;
}