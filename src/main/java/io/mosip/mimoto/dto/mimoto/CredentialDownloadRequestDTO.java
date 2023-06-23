package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CredentialDownloadRequestDTO {
    @NotNull
    private String individualId;
    @NotNull
    private String requestId;
}
